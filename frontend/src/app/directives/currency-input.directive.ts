/**
 * myRC - Currency Input Directive
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Allows currency inputs to accept values with commas for thousands separators
 * (e.g., $2,198,957.89) and converts them to numeric values.
 */
import { Directive, ElementRef, HostListener, forwardRef, OnInit } from '@angular/core';
import { NG_VALUE_ACCESSOR, ControlValueAccessor } from '@angular/forms';

@Directive({
  selector: 'input[currencyInput]',
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CurrencyInputDirective),
      multi: true
    }
  ]
})
export class CurrencyInputDirective implements ControlValueAccessor, OnInit {
  private onChange: (value: number | null) => void = () => {};
  private onTouched: () => void = () => {};

  constructor(private el: ElementRef<HTMLInputElement>) {}

  ngOnInit(): void {
    // Set inputmode to decimal for better mobile keyboard
    this.el.nativeElement.setAttribute('inputmode', 'decimal');
  }

  @HostListener('input', ['$event.target.value'])
  onInput(value: string): void {
    const numericValue = this.parseValue(value);
    this.onChange(numericValue);
  }

  @HostListener('blur')
  onBlur(): void {
    this.onTouched();
    // Format the displayed value on blur
    const currentValue = this.el.nativeElement.value;
    const numericValue = this.parseValue(currentValue);
    if (numericValue !== null) {
      this.el.nativeElement.value = this.formatValue(numericValue);
    }
  }

  @HostListener('focus')
  onFocus(): void {
    // Remove formatting on focus for easier editing
    const currentValue = this.el.nativeElement.value;
    const numericValue = this.parseValue(currentValue);
    if (numericValue !== null) {
      // Show unformatted value for editing (keep decimal places)
      this.el.nativeElement.value = numericValue.toString();
    }
  }

  writeValue(value: number | null): void {
    if (value !== null && value !== undefined) {
      this.el.nativeElement.value = this.formatValue(value);
    } else {
      this.el.nativeElement.value = '';
    }
  }

  registerOnChange(fn: (value: number | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.el.nativeElement.disabled = isDisabled;
  }

  /**
   * Parse a string value that may contain commas, spaces, or currency symbols
   * into a numeric value.
   */
  private parseValue(value: string): number | null {
    if (!value || value.trim() === '') {
      return null;
    }

    // Remove currency symbols, spaces, and thousand separators (commas)
    const cleanedValue = value
      .replace(/[$€£¥₹]/g, '')  // Remove common currency symbols
      .replace(/\s/g, '')        // Remove spaces
      .replace(/,/g, '');        // Remove commas (thousand separators)

    const numericValue = parseFloat(cleanedValue);

    if (isNaN(numericValue)) {
      return null;
    }

    return numericValue;
  }

  /**
   * Format a numeric value with thousand separators for display.
   */
  private formatValue(value: number): string {
    // Format with up to 2 decimal places
    return value.toLocaleString('en-US', {
      minimumFractionDigits: 0,
      maximumFractionDigits: 2
    });
  }
}
