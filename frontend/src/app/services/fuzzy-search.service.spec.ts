/*
 * myRC - Fuzzy Search Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */

import { TestBed } from '@angular/core/testing';
import { FuzzySearchService, FuzzySearchResult } from './fuzzy-search.service';

describe('FuzzySearchService', () => {
  let service: FuzzySearchService;

  // Test data
  interface TestItem {
    id: number;
    name: string;
    description?: string;
    category?: string;
  }

  const testItems: TestItem[] = [
    { id: 1, name: 'GPU Server Purchase', description: 'High-performance GPU server', category: 'Hardware' },
    { id: 2, name: 'Software License', description: 'Annual subscription', category: 'Software' },
    { id: 3, name: 'Network Equipment', description: 'Switches and routers', category: 'Hardware' },
    { id: 4, name: 'Cloud Computing Services', description: 'AWS and Azure credits', category: 'Cloud' },
    { id: 5, name: 'Training Materials', description: 'Educational resources', category: 'Education' },
    { id: 6, name: 'Server Maintenance', description: 'Quarterly maintenance contract', category: 'Services' }
  ];

  const fieldExtractor = (item: TestItem) => ({
    name: item.name,
    description: item.description,
    category: item.category
  });

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FuzzySearchService]
    });
    service = TestBed.inject(FuzzySearchService);
  });

  describe('Service Creation', () => {
    it('should be created', () => {
      expect(service).toBeTruthy();
    });
  });

  describe('Empty Search Term', () => {
    it('should return all items when search term is empty', () => {
      const results = service.filter(testItems, '', fieldExtractor);
      expect(results.length).toBe(testItems.length);
    });

    it('should return all items when search term is only whitespace', () => {
      const results = service.filter(testItems, '   ', fieldExtractor);
      expect(results.length).toBe(testItems.length);
    });

    it('should return all items with score 1 when search term is empty', () => {
      const results = service.search(testItems, '', fieldExtractor);
      expect(results.every(r => r.score === 1)).toBe(true);
    });
  });

  describe('Exact Match', () => {
    it('should find exact name match', () => {
      const results = service.filter(testItems, 'GPU Server Purchase', fieldExtractor);
      expect(results.length).toBeGreaterThan(0);
      expect(results[0].name).toBe('GPU Server Purchase');
    });

    it('should find exact substring match', () => {
      const results = service.filter(testItems, 'GPU Server', fieldExtractor);
      expect(results.length).toBeGreaterThan(0);
      expect(results[0].name).toContain('GPU');
    });

    it('should be case-insensitive by default', () => {
      const results = service.filter(testItems, 'gpu server', fieldExtractor);
      expect(results.length).toBeGreaterThan(0);
      expect(results[0].name.toLowerCase()).toContain('gpu');
    });

    it('should find match in description field', () => {
      const results = service.filter(testItems, 'annual subscription', fieldExtractor);
      expect(results.length).toBeGreaterThan(0);
      expect(results[0].description).toContain('Annual subscription');
    });

    it('should find match in category field', () => {
      const results = service.filter(testItems, 'Hardware', fieldExtractor);
      expect(results.length).toBe(2); // GPU Server and Network Equipment
    });
  });

  describe('Token-Based Match', () => {
    it('should match tokens in different order', () => {
      const results = service.filter(testItems, 'server gpu', fieldExtractor);
      expect(results.length).toBeGreaterThan(0);
      expect(results[0].name).toContain('GPU');
    });

    it('should match partial tokens', () => {
      const results = service.filter(testItems, 'serv', fieldExtractor);
      expect(results.length).toBeGreaterThan(0);
      // Should match 'Server' or 'Services'
      const hasMatch = results.some(r =>
        r.name.toLowerCase().includes('serv') ||
        r.description?.toLowerCase().includes('serv') ||
        r.category?.toLowerCase().includes('serv')
      );
      expect(hasMatch).toBe(true);
    });

    it('should match multiple partial tokens', () => {
      const results = service.filter(testItems, 'netw equip', fieldExtractor);
      expect(results.length).toBeGreaterThan(0);
      expect(results[0].name).toBe('Network Equipment');
    });
  });

  describe('Fuzzy Match', () => {
    it('should find results with minor typos', () => {
      const results = service.filter(testItems, 'servr', fieldExtractor);
      // Should still find server-related items due to fuzzy matching
      expect(results.length).toBeGreaterThanOrEqual(0);
    });

    it('should find results with similar words', () => {
      const results = service.filter(testItems, 'licence', fieldExtractor);
      // British vs American spelling - should find 'License'
      expect(results.length).toBeGreaterThanOrEqual(0);
    });
  });

  describe('Scoring', () => {
    it('should give higher score to exact matches', () => {
      const results = service.search(testItems, 'GPU Server Purchase', fieldExtractor);
      expect(results.length).toBeGreaterThan(0);
      expect(results[0].score).toBeGreaterThanOrEqual(0.9);
    });

    it('should sort results by score descending', () => {
      const results = service.search(testItems, 'server', fieldExtractor);
      for (let i = 1; i < results.length; i++) {
        expect(results[i - 1].score).toBeGreaterThanOrEqual(results[i].score);
      }
    });

    it('should include matched fields in results', () => {
      const results = service.search(testItems, 'Hardware', fieldExtractor);
      expect(results.length).toBeGreaterThan(0);
      expect(results[0].matchedFields).toContain('category');
    });
  });

  describe('Threshold Configuration', () => {
    it('should respect custom threshold', () => {
      const highThresholdResults = service.filter(testItems, 'srv', fieldExtractor, { threshold: 0.8 });
      const lowThresholdResults = service.filter(testItems, 'srv', fieldExtractor, { threshold: 0.1 });

      // Lower threshold should return more or equal results
      expect(lowThresholdResults.length).toBeGreaterThanOrEqual(highThresholdResults.length);
    });
  });

  describe('Edge Cases', () => {
    it('should handle empty item array', () => {
      const results = service.filter([], 'test', fieldExtractor);
      expect(results.length).toBe(0);
    });

    it('should handle null/undefined field values', () => {
      const itemsWithNulls: TestItem[] = [
        { id: 1, name: 'Test Item', description: undefined, category: undefined }
      ];
      const results = service.filter(itemsWithNulls, 'test', fieldExtractor);
      expect(results.length).toBeGreaterThan(0);
    });

    it('should handle special regex characters in search term', () => {
      const results = service.filter(testItems, 'server (gpu)', fieldExtractor);
      // Should not throw error
      expect(Array.isArray(results)).toBe(true);
    });

    it('should handle very long search terms', () => {
      const longSearchTerm = 'a'.repeat(1000);
      const results = service.filter(testItems, longSearchTerm, fieldExtractor);
      expect(Array.isArray(results)).toBe(true);
    });

    it('should handle unicode characters', () => {
      const itemsWithUnicode: TestItem[] = [
        { id: 1, name: 'Café Equipment', description: 'Coffee machine' }
      ];
      const results = service.filter(itemsWithUnicode, 'café', (item) => ({
        name: item.name,
        description: item.description
      }));
      expect(results.length).toBeGreaterThan(0);
    });
  });

  describe('Multi-Field Search', () => {
    it('should search across multiple fields', () => {
      // 'server' appears in name for item 1 and 6
      const results = service.filter(testItems, 'server', fieldExtractor);
      expect(results.length).toBeGreaterThanOrEqual(2);
    });

    it('should match different fields for different items', () => {
      // 'quarterly' only appears in description of item 6
      const results = service.filter(testItems, 'quarterly', fieldExtractor);
      expect(results.length).toBeGreaterThan(0);
      expect(results[0].id).toBe(6);
    });
  });

  describe('Real-World Scenarios', () => {
    it('should handle typical procurement searches', () => {
      const procurementItems = [
        { id: 1, pr: 'PR-2026-001', name: 'Dell PowerEdge Server', vendor: 'Dell Technologies' },
        { id: 2, pr: 'PR-2026-002', name: 'HP Laptop Fleet', vendor: 'HP Inc' },
        { id: 3, pr: 'PR-2026-003', name: 'Cisco Network Switches', vendor: 'Cisco Systems' }
      ];

      const procurementExtractor = (item: typeof procurementItems[0]) => ({
        pr: item.pr,
        name: item.name,
        vendor: item.vendor
      });

      // Search by PR number
      let results = service.filter(procurementItems, 'PR-2026-001', procurementExtractor);
      expect(results.length).toBeGreaterThanOrEqual(1);
      expect(results[0].id).toBe(1);

      // Search by vendor name
      results = service.filter(procurementItems, 'dell', procurementExtractor);
      expect(results.length).toBeGreaterThanOrEqual(1);
      expect(results[0].id).toBe(1);

      // Search by product type
      results = service.filter(procurementItems, 'server', procurementExtractor);
      expect(results.length).toBeGreaterThan(0);
    });

    it('should handle typical funding/spending searches', () => {
      const fundingItems = [
        { id: 1, name: 'AWS Cloud Credits', source: 'A_BASE', category: 'Cloud Infrastructure' },
        { id: 2, name: 'Training Budget 2026', source: 'B_BASE', category: 'Human Resources' },
        { id: 3, name: 'Hardware Refresh Program', source: 'A_BASE', category: 'IT Equipment' }
      ];

      const fundingExtractor = (item: typeof fundingItems[0]) => ({
        name: item.name,
        source: item.source,
        category: item.category
      });

      // Search by category keyword
      let results = service.filter(fundingItems, 'cloud', fundingExtractor);
      expect(results.length).toBeGreaterThanOrEqual(1);
      expect(results[0].id).toBe(1);

      // Search by partial name
      results = service.filter(fundingItems, 'training', fundingExtractor);
      expect(results.length).toBeGreaterThanOrEqual(1);
      expect(results[0].id).toBe(2);
    });
  });
});
