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
