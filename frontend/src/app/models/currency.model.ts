/**
 * Currency model for myRC application.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-23
 * @license MIT
 */

/**
 * Currency interface representing a supported currency in the application.
 */
export interface Currency {
  /** ISO 4217 currency code */
  code: string;

  /** Full name of the currency */
  name: string;

  /** Currency symbol */
  symbol: string;

  /** Whether this is the default currency */
  isDefault: boolean;
}

/**
 * Default currency code (Canadian Dollar).
 */
export const DEFAULT_CURRENCY = 'CAD';

/**
 * Get display name for a currency.
 */
export function getCurrencyDisplayName(currency: Currency | undefined): string {
  if (!currency) {
    return 'Unknown';
  }
  return `${currency.name} (${currency.symbol})`;
}

/**
 * Format amount with currency symbol.
 */
export function formatAmountWithCurrency(
  amount: number | null,
  currencyCode: string,
  currencies: Currency[]
): string {
  if (amount === null || amount === undefined) {
    return '-';
  }

  const currency = currencies.find(c => c.code === currencyCode);
  const symbol = currency?.symbol || '$';
  const locale = getLocaleForCurrency(currencyCode);

  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency: currencyCode
  }).format(amount);
}

/**
 * Get the appropriate locale for a currency.
 */
function getLocaleForCurrency(currencyCode: string): string {
  switch (currencyCode) {
    case 'CAD':
      return 'en-CA';
    case 'USD':
      return 'en-US';
    case 'GBP':
      return 'en-GB';
    case 'EUR':
      return 'de-DE';
    case 'AUD':
      return 'en-AU';
    case 'NZD':
      return 'en-NZ';
    default:
      return 'en-CA';
  }
}

/**
 * Get the flag emoji for a currency code.
 * Uses ISO 3166-1 alpha-2 country codes converted to Regional Indicator Symbols.
 *
 * @param currencyCode ISO 4217 currency code
 * @returns Flag emoji string representing the currency's country/region
 */
export function getCurrencyFlag(currencyCode: string): string {
  switch (currencyCode) {
    case 'CAD':
      return '🇨🇦'; // Canada
    case 'USD':
      return '🇺🇸'; // United States
    case 'GBP':
      return '🇬🇧'; // United Kingdom
    case 'EUR':
      return '🇪🇺'; // European Union
    case 'AUD':
      return '🇦🇺'; // Australia
    case 'NZD':
      return '🇳🇿'; // New Zealand
    case 'JPY':
      return '🇯🇵'; // Japan
    case 'CHF':
      return '🇨🇭'; // Switzerland
    case 'CNY':
      return '🇨🇳'; // China
    case 'INR':
      return '🇮🇳'; // India
    case 'MXN':
      return '🇲🇽'; // Mexico
    case 'BRL':
      return '🇧🇷'; // Brazil
    case 'KRW':
      return '🇰🇷'; // South Korea
    case 'SEK':
      return '🇸🇪'; // Sweden
    case 'NOK':
      return '🇳🇴'; // Norway
    case 'DKK':
      return '🇩🇰'; // Denmark
    case 'SGD':
      return '🇸🇬'; // Singapore
    case 'HKD':
      return '🇭🇰'; // Hong Kong
    default:
      return '🏳️'; // Default/Unknown flag
  }
}

/**
 * Get the display text for a currency with its flag.
 *
 * @param currencyCode ISO 4217 currency code
 * @param includeName Whether to include the currency name
 * @returns Formatted string with flag and currency code
 */
export function getCurrencyWithFlag(currencyCode: string, includeName = false): string {
  const flag = getCurrencyFlag(currencyCode);
  return `${flag} ${currencyCode}`;
}
