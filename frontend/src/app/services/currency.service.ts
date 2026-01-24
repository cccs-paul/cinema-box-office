/**
 * Currency Service for myRC application.
 * Handles fetching currency configuration from the backend.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-23
 * @license MIT
 */
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, tap, shareReplay } from 'rxjs/operators';
import { Currency, DEFAULT_CURRENCY } from '../models/currency.model';

/**
 * Service for managing currency data from the backend.
 * Currencies are cached after initial fetch since they are read-only configuration.
 */
@Injectable({
  providedIn: 'root'
})
export class CurrencyService {
  private readonly baseUrl = '/api/currencies';

  /** Cached currencies observable */
  private currenciesCache$: Observable<Currency[]> | null = null;

  /** BehaviorSubject to hold the loaded currencies */
  private currenciesSubject = new BehaviorSubject<Currency[]>([]);

  /** Observable of currencies for components to subscribe to */
  currencies$ = this.currenciesSubject.asObservable();

  /** Default currency subject */
  private defaultCurrencySubject = new BehaviorSubject<Currency | null>(null);

  /** Observable of default currency */
  defaultCurrency$ = this.defaultCurrencySubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Get all supported currencies.
   * Results are cached after the first fetch.
   *
   * @returns Observable of currencies array
   */
  getCurrencies(): Observable<Currency[]> {
    if (!this.currenciesCache$) {
      this.currenciesCache$ = this.http.get<Currency[]>(this.baseUrl, { withCredentials: true })
        .pipe(
          tap(currencies => {
            this.currenciesSubject.next(currencies);
            const defaultCurrency = currencies.find(c => c.isDefault);
            this.defaultCurrencySubject.next(defaultCurrency || null);
          }),
          shareReplay(1),
          catchError(this.handleError)
        );
    }
    return this.currenciesCache$;
  }

  /**
   * Get the default currency.
   *
   * @returns Observable of the default currency
   */
  getDefaultCurrency(): Observable<Currency> {
    return this.http.get<Currency>(`${this.baseUrl}/default`, { withCredentials: true })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get a currency by code from the cached list.
   *
   * @param code The currency code
   * @returns The currency or undefined if not found
   */
  getCurrencyByCode(code: string): Currency | undefined {
    return this.currenciesSubject.value.find(c => c.code === code);
  }

  /**
   * Get the current list of currencies synchronously.
   *
   * @returns Array of currencies (may be empty if not loaded yet)
   */
  getCurrentCurrencies(): Currency[] {
    return this.currenciesSubject.value;
  }

  /**
   * Check if a currency code is the default currency.
   *
   * @param code The currency code to check
   * @returns true if the code is the default currency
   */
  isDefaultCurrency(code: string): boolean {
    const defaultCurrency = this.defaultCurrencySubject.value;
    return defaultCurrency ? defaultCurrency.code === code : code === DEFAULT_CURRENCY;
  }

  /**
   * Check if exchange rate is required for a currency.
   *
   * @param currencyCode The currency code
   * @returns true if exchange rate is required (non-CAD currencies)
   */
  isExchangeRateRequired(currencyCode: string): boolean {
    return currencyCode !== DEFAULT_CURRENCY;
  }

  /**
   * Clear the cache to force a refresh on next request.
   */
  clearCache(): void {
    this.currenciesCache$ = null;
  }

  /**
   * Handle HTTP errors.
   *
   * @param error The HTTP error response
   * @returns Observable that throws an error
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unknown error occurred';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Client error: ${error.error.message}`;
    } else {
      // Server-side error
      switch (error.status) {
        case 500:
          errorMessage = 'Server error: Unable to load currency data';
          break;
        default:
          errorMessage = `Server error: ${error.status} - ${error.statusText}`;
      }
    }

    console.error('CurrencyService error:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
