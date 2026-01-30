/**
 * Currency Service Tests for myRC application.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-23
 * @license MIT
 */
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CurrencyService } from './currency.service';
import { Currency, DEFAULT_CURRENCY } from '../models/currency.model';

describe('CurrencyService', () => {
  let service: CurrencyService;
  let httpMock: HttpTestingController;

  const mockCurrencies: Currency[] = [
    { code: 'CAD', name: 'Canadian Dollar', symbol: '$', isDefault: true },
    { code: 'GBP', name: 'Pound Sterling', symbol: '£', isDefault: false },
    { code: 'USD', name: 'US Dollar', symbol: '$', isDefault: false },
    { code: 'EUR', name: 'Euro', symbol: '€', isDefault: false },
    { code: 'AUD', name: 'Australian Dollar', symbol: 'A$', isDefault: false },
    { code: 'NZD', name: 'New Zealand Dollar', symbol: 'NZ$', isDefault: false }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CurrencyService]
    });

    service = TestBed.inject(CurrencyService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getCurrencies', () => {
    it('should fetch currencies from the API', () => {
      service.getCurrencies().subscribe(currencies => {
        expect(currencies).toEqual(mockCurrencies);
        expect(currencies.length).toBe(6);
      });

      const req = httpMock.expectOne('/api/currencies');
      expect(req.request.method).toBe('GET');
      expect(req.request.withCredentials).toBe(true);
      req.flush(mockCurrencies);
    });

    it('should cache currencies after first fetch', () => {
      service.getCurrencies().subscribe();
      const req1 = httpMock.expectOne('/api/currencies');
      req1.flush(mockCurrencies);

      // Second call should use cache
      service.getCurrencies().subscribe(currencies => {
        expect(currencies).toEqual(mockCurrencies);
      });

      // No additional HTTP request should be made
      httpMock.expectNone('/api/currencies');
    });

    it('should update currenciesSubject after fetch', () => {
      service.getCurrencies().subscribe();
      const req = httpMock.expectOne('/api/currencies');
      req.flush(mockCurrencies);

      expect(service.getCurrentCurrencies()).toEqual(mockCurrencies);
    });

    it('should handle errors gracefully', () => {
      service.getCurrencies().subscribe({
        error: (error) => {
          expect(error.message).toContain('Internal server error');
        }
      });

      const req = httpMock.expectOne('/api/currencies');
      req.flush({ message: 'Internal server error' }, { status: 500, statusText: 'Internal Server Error' });
    });
  });

  describe('getDefaultCurrency', () => {
    it('should fetch default currency from the API', () => {
      const defaultCurrency: Currency = {
        code: 'CAD',
        name: 'Canadian Dollar',
        symbol: '$',
        isDefault: true
      };

      service.getDefaultCurrency().subscribe(currency => {
        expect(currency).toEqual(defaultCurrency);
        expect(currency.isDefault).toBe(true);
      });

      const req = httpMock.expectOne('/api/currencies/default');
      expect(req.request.method).toBe('GET');
      req.flush(defaultCurrency);
    });
  });

  describe('getCurrencyByCode', () => {
    it('should return currency by code from cached list', () => {
      service.getCurrencies().subscribe();
      const req = httpMock.expectOne('/api/currencies');
      req.flush(mockCurrencies);

      const currency = service.getCurrencyByCode('GBP');
      expect(currency).toBeDefined();
      expect(currency?.name).toBe('Pound Sterling');
      expect(currency?.symbol).toBe('£');
    });

    it('should return undefined for unknown currency code', () => {
      service.getCurrencies().subscribe();
      const req = httpMock.expectOne('/api/currencies');
      req.flush(mockCurrencies);

      const currency = service.getCurrencyByCode('INVALID');
      expect(currency).toBeUndefined();
    });
  });

  describe('isDefaultCurrency', () => {
    it('should return true for CAD before currencies are loaded', () => {
      expect(service.isDefaultCurrency('CAD')).toBe(true);
    });

    it('should return false for non-CAD before currencies are loaded', () => {
      expect(service.isDefaultCurrency('GBP')).toBe(false);
    });

    it('should use loaded default currency', () => {
      service.getCurrencies().subscribe();
      const req = httpMock.expectOne('/api/currencies');
      req.flush(mockCurrencies);

      expect(service.isDefaultCurrency('CAD')).toBe(true);
      expect(service.isDefaultCurrency('GBP')).toBe(false);
    });
  });

  describe('isExchangeRateRequired', () => {
    it('should return true for non-CAD currencies', () => {
      expect(service.isExchangeRateRequired('GBP')).toBe(true);
      expect(service.isExchangeRateRequired('USD')).toBe(true);
      expect(service.isExchangeRateRequired('EUR')).toBe(true);
    });

    it('should return false for CAD', () => {
      expect(service.isExchangeRateRequired('CAD')).toBe(false);
    });
  });

  describe('clearCache', () => {
    it('should clear the cache and allow fresh fetch', () => {
      // First fetch
      service.getCurrencies().subscribe();
      const req1 = httpMock.expectOne('/api/currencies');
      req1.flush(mockCurrencies);

      // Clear cache
      service.clearCache();

      // Second fetch should make a new request
      service.getCurrencies().subscribe();
      const req2 = httpMock.expectOne('/api/currencies');
      req2.flush(mockCurrencies);
    });
  });

  describe('currencies$ observable', () => {
    it('should emit currencies when loaded', (done) => {
      service.currencies$.subscribe(currencies => {
        if (currencies.length > 0) {
          expect(currencies).toEqual(mockCurrencies);
          done();
        }
      });

      service.getCurrencies().subscribe();
      const req = httpMock.expectOne('/api/currencies');
      req.flush(mockCurrencies);
    });
  });

  describe('defaultCurrency$ observable', () => {
    it('should emit default currency when loaded', (done) => {
      service.defaultCurrency$.subscribe(defaultCurrency => {
        if (defaultCurrency) {
          expect(defaultCurrency.code).toBe('CAD');
          expect(defaultCurrency.isDefault).toBe(true);
          done();
        }
      });

      service.getCurrencies().subscribe();
      const req = httpMock.expectOne('/api/currencies');
      req.flush(mockCurrencies);
    });
  });
});
