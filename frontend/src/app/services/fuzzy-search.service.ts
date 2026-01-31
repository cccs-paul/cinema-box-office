/*
 * myRC - Fuzzy Search Service
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Provides semantic/fuzzy text search capabilities for filtering
 * data in Funding, Spending, and Procurement views.
 */

import { Injectable } from '@angular/core';

/**
 * Configuration options for fuzzy search.
 */
export interface FuzzySearchOptions {
  /**
   * Minimum score threshold (0-1) for a match to be considered valid.
   * Default: 0.3
   */
  threshold?: number;

  /**
   * Whether to ignore case when matching.
   * Default: true
   */
  ignoreCase?: boolean;

  /**
   * Whether to trim whitespace from search terms.
   * Default: true
   */
  trimWhitespace?: boolean;
}

/**
 * Result of a fuzzy search operation.
 */
export interface FuzzySearchResult<T> {
  /**
   * The matched item.
   */
  item: T;

  /**
   * The match score (0-1), where 1 is a perfect match.
   */
  score: number;

  /**
   * The field(s) that matched.
   */
  matchedFields: string[];
}

/**
 * Service for performing fuzzy/semantic text searches.
 * Implements multiple matching strategies for flexible text filtering.
 *
 * @author myRC Team
 * @version 1.0.0
 */
@Injectable({
  providedIn: 'root'
})
export class FuzzySearchService {
  private readonly defaultOptions: FuzzySearchOptions = {
    threshold: 0.3,
    ignoreCase: true,
    trimWhitespace: true
  };

  /**
   * Search through a collection of items using fuzzy matching.
   *
   * @param items The array of items to search through
   * @param searchTerm The search query
   * @param fieldExtractor Function to extract searchable text from each item
   * @param options Search configuration options
   * @returns Array of matching items sorted by relevance (highest first)
   */
  search<T>(
    items: T[],
    searchTerm: string,
    fieldExtractor: (item: T) => { [field: string]: string | null | undefined },
    options?: FuzzySearchOptions
  ): FuzzySearchResult<T>[] {
    const opts = { ...this.defaultOptions, ...options };

    if (!searchTerm || (opts.trimWhitespace && !searchTerm.trim())) {
      // Return all items with perfect score if no search term
      return items.map(item => ({
        item,
        score: 1,
        matchedFields: []
      }));
    }

    const normalizedSearch = this.normalizeText(searchTerm, opts);
    const searchTokens = this.tokenize(normalizedSearch);

    const results: FuzzySearchResult<T>[] = [];

    for (const item of items) {
      const fields = fieldExtractor(item);
      let maxScore = 0;
      const matchedFields: string[] = [];

      for (const [fieldName, fieldValue] of Object.entries(fields)) {
        if (fieldValue == null) {
          continue;
        }

        const normalizedValue = this.normalizeText(fieldValue, opts);
        const score = this.calculateMatchScore(normalizedSearch, searchTokens, normalizedValue);

        if (score > maxScore) {
          maxScore = score;
        }

        if (score >= opts.threshold!) {
          matchedFields.push(fieldName);
        }
      }

      if (maxScore >= opts.threshold!) {
        results.push({
          item,
          score: maxScore,
          matchedFields
        });
      }
    }

    // Sort by score descending (best matches first)
    return results.sort((a, b) => b.score - a.score);
  }

  /**
   * Simple filter that returns just the items (not wrapped in result objects).
   *
   * @param items The array of items to filter
   * @param searchTerm The search query
   * @param fieldExtractor Function to extract searchable text from each item
   * @param options Search configuration options
   * @returns Array of matching items sorted by relevance
   */
  filter<T>(
    items: T[],
    searchTerm: string,
    fieldExtractor: (item: T) => { [field: string]: string | null | undefined },
    options?: FuzzySearchOptions
  ): T[] {
    return this.search(items, searchTerm, fieldExtractor, options).map(result => result.item);
  }

  /**
   * Calculate match score between search term and target text.
   * Combines multiple matching strategies:
   * 1. Exact substring match (highest score)
   * 2. Word boundary match (high score)
   * 3. Token-based match (medium score)
   * 4. Levenshtein distance for fuzzy matching (lower score)
   *
   * @param searchTerm Normalized search term
   * @param searchTokens Tokenized search terms
   * @param targetText Normalized target text to search in
   * @returns Match score between 0 and 1
   */
  private calculateMatchScore(searchTerm: string, searchTokens: string[], targetText: string): number {
    if (!targetText) {
      return 0;
    }

    // Strategy 1: Exact substring match - highest score
    if (targetText.includes(searchTerm)) {
      // Bonus for matching at word boundary
      const wordBoundaryRegex = new RegExp(`\\b${this.escapeRegex(searchTerm)}`, 'i');
      if (wordBoundaryRegex.test(targetText)) {
        return 1.0;
      }
      return 0.95;
    }

    // Strategy 2: All tokens present (in any order)
    const targetTokens = this.tokenize(targetText);
    const allTokensMatch = searchTokens.every(token =>
      targetTokens.some(targetToken =>
        targetToken.includes(token) || token.includes(targetToken)
      )
    );

    if (allTokensMatch && searchTokens.length > 0) {
      // Score based on how many of the target's words matched
      const matchedCount = searchTokens.filter(token =>
        targetTokens.some(targetToken => targetToken.includes(token))
      ).length;
      return 0.7 + (0.2 * matchedCount / searchTokens.length);
    }

    // Strategy 3: Partial token match
    const partialMatches = searchTokens.filter(token =>
      targetTokens.some(targetToken =>
        targetToken.includes(token) || token.includes(targetToken)
      )
    );

    if (partialMatches.length > 0) {
      return 0.4 + (0.3 * partialMatches.length / searchTokens.length);
    }

    // Strategy 4: Fuzzy matching using Levenshtein distance
    // Check each token against each target token
    let bestFuzzyScore = 0;
    for (const searchToken of searchTokens) {
      for (const targetToken of targetTokens) {
        const distance = this.levenshteinDistance(searchToken, targetToken);
        const maxLen = Math.max(searchToken.length, targetToken.length);
        const similarity = 1 - distance / maxLen;

        // Only consider fuzzy matches if similarity is reasonable
        if (similarity > 0.6 && similarity > bestFuzzyScore) {
          bestFuzzyScore = similarity * 0.5; // Scale down fuzzy scores
        }
      }
    }

    return bestFuzzyScore;
  }

  /**
   * Normalize text for comparison.
   *
   * @param text Text to normalize
   * @param options Normalization options
   * @returns Normalized text
   */
  private normalizeText(text: string, options: FuzzySearchOptions): string {
    let result = text;

    if (options.trimWhitespace) {
      result = result.trim();
    }

    if (options.ignoreCase) {
      result = result.toLowerCase();
    }

    // Normalize multiple spaces to single space
    result = result.replace(/\s+/g, ' ');

    return result;
  }

  /**
   * Split text into tokens (words).
   *
   * @param text Text to tokenize
   * @returns Array of tokens
   */
  private tokenize(text: string): string[] {
    return text
      .split(/[\s\-_.,;:!?]+/)
      .filter(token => token.length > 0);
  }

  /**
   * Calculate Levenshtein distance between two strings.
   * This is the minimum number of single-character edits
   * (insertions, deletions, substitutions) needed to change
   * one string into the other.
   *
   * @param a First string
   * @param b Second string
   * @returns Edit distance
   */
  private levenshteinDistance(a: string, b: string): number {
    if (a.length === 0) return b.length;
    if (b.length === 0) return a.length;

    const matrix: number[][] = [];

    // Initialize first column
    for (let i = 0; i <= b.length; i++) {
      matrix[i] = [i];
    }

    // Initialize first row
    for (let j = 0; j <= a.length; j++) {
      matrix[0][j] = j;
    }

    // Fill in the rest of the matrix
    for (let i = 1; i <= b.length; i++) {
      for (let j = 1; j <= a.length; j++) {
        const cost = a[j - 1] === b[i - 1] ? 0 : 1;
        matrix[i][j] = Math.min(
          matrix[i - 1][j] + 1,     // deletion
          matrix[i][j - 1] + 1,     // insertion
          matrix[i - 1][j - 1] + cost // substitution
        );
      }
    }

    return matrix[b.length][a.length];
  }

  /**
   * Escape special regex characters in a string.
   *
   * @param text Text to escape
   * @returns Escaped text safe for use in RegExp
   */
  private escapeRegex(text: string): string {
    return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }
}
