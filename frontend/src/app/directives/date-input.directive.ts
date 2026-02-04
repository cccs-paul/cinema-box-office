/**
 * myRC - Date Input Directive
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Custom date input directive that displays dates in dd/mm/yyyy format
 * while storing values in ISO format (yyyy-mm-dd) for the model.
 * 
 * Usage: <input type="text" dateInput [(ngModel)]="dateValue" />
 * 
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-03
 */
import { Directive, ElementRef, HostListener, forwardRef, OnInit, Input } from '@angular/core';
import { NG_VALUE_ACCESSOR, ControlValueAccessor } from '@angular/forms';

@Directive({
  selector: 'input[dateInput]',
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DateInputDirective),
      multi: true
    }
  ]
})
export class DateInputDirective implements ControlValueAccessor, OnInit {
  /** Placeholder text when no date is entered */
  @Input() placeholder = 'dd/mm/yyyy';

  private onChange: (value: string | null) => void = () => {};
  private onTouched: () => void = () => {};
  private internalValue: string | null = null;

  constructor(private el: ElementRef<HTMLInputElement>) {}

  ngOnInit(): void {
    // Set appropriate attributes for the input
    this.el.nativeElement.setAttribute('inputmode', 'numeric');
    this.el.nativeElement.setAttribute('autocomplete', 'off');
    if (!this.el.nativeElement.placeholder) {
      this.el.nativeElement.placeholder = this.placeholder;
    }
  }

  @HostListener('input', ['$event.target.value'])
  onInput(value: string): void {
    // Auto-format as user types
    const formatted = this.autoFormat(value);
    this.el.nativeElement.value = formatted;

    // Parse to ISO format for the model
    const isoValue = this.parseToIso(formatted);
    this.internalValue = isoValue;
    this.onChange(isoValue);
  }

  @HostListener('blur')
  onBlur(): void {
    this.onTouched();
    // Validate and reformat on blur
    const currentValue = this.el.nativeElement.value;
    if (currentValue) {
      const isoValue = this.parseToIso(currentValue);
      if (isoValue) {
        this.el.nativeElement.value = this.formatFromIso(isoValue);
      }
    }
  }

  @HostListener('keydown', ['$event'])
  onKeydown(event: KeyboardEvent): void {
    // Allow navigation keys, backspace, delete, tab
    const allowedKeys = ['Backspace', 'Delete', 'Tab', 'ArrowLeft', 'ArrowRight', 'Home', 'End'];
    if (allowedKeys.includes(event.key)) {
      return;
    }

    // Allow Ctrl/Cmd combinations (copy, paste, etc.)
    if (event.ctrlKey || event.metaKey) {
      return;
    }

    // Only allow digits and slash
    if (!/^[0-9/]$/.test(event.key)) {
      event.preventDefault();
    }
  }

  @HostListener('paste', ['$event'])
  onPaste(event: ClipboardEvent): void {
    event.preventDefault();
    const pastedText = event.clipboardData?.getData('text') || '';
    
    // Try to parse various date formats from pasted text
    const isoValue = this.parsePastedDate(pastedText);
    if (isoValue) {
      this.el.nativeElement.value = this.formatFromIso(isoValue);
      this.internalValue = isoValue;
      this.onChange(isoValue);
    }
  }

  writeValue(value: string | null): void {
    this.internalValue = value;
    if (value) {
      this.el.nativeElement.value = this.formatFromIso(value);
    } else {
      this.el.nativeElement.value = '';
    }
  }

  registerOnChange(fn: (value: string | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.el.nativeElement.disabled = isDisabled;
  }

  /**
   * Auto-format the input as the user types.
   * Inserts slashes automatically after day and month.
   */
  private autoFormat(value: string): string {
    // Remove all non-digit characters
    let digits = value.replace(/\D/g, '');

    // Limit to 8 digits (ddmmyyyy)
    digits = digits.substring(0, 8);

    // Build formatted string
    let formatted = '';
    if (digits.length > 0) {
      // Day (max 2 digits)
      formatted = digits.substring(0, Math.min(2, digits.length));
    }
    if (digits.length > 2) {
      // Month (max 2 digits)
      formatted += '/' + digits.substring(2, Math.min(4, digits.length));
    }
    if (digits.length > 4) {
      // Year (max 4 digits)
      formatted += '/' + digits.substring(4, 8);
    }

    return formatted;
  }

  /**
   * Parse dd/mm/yyyy format to ISO yyyy-mm-dd format.
   * Returns null if the date is invalid or incomplete.
   */
  private parseToIso(value: string): string | null {
    if (!value) {
      return null;
    }

    // Match dd/mm/yyyy pattern
    const match = value.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})$/);
    if (!match) {
      return null;
    }

    const day = parseInt(match[1], 10);
    const month = parseInt(match[2], 10);
    const year = parseInt(match[3], 10);

    // Validate date components
    if (!this.isValidDate(day, month, year)) {
      return null;
    }

    // Format as ISO date
    const dayStr = day.toString().padStart(2, '0');
    const monthStr = month.toString().padStart(2, '0');

    return `${year}-${monthStr}-${dayStr}`;
  }

  /**
   * Format ISO yyyy-mm-dd to display format dd/mm/yyyy.
   */
  private formatFromIso(isoDate: string): string {
    if (!isoDate) {
      return '';
    }

    // Match yyyy-mm-dd pattern
    const match = isoDate.match(/^(\d{4})-(\d{2})-(\d{2})/);
    if (!match) {
      return isoDate;
    }

    const year = match[1];
    const month = match[2];
    const day = match[3];

    return `${day}/${month}/${year}`;
  }

  /**
   * Parse various date formats from pasted text.
   */
  private parsePastedDate(text: string): string | null {
    const trimmed = text.trim();

    // Try ISO format first (yyyy-mm-dd)
    let match = trimmed.match(/^(\d{4})-(\d{2})-(\d{2})/);
    if (match) {
      const year = parseInt(match[1], 10);
      const month = parseInt(match[2], 10);
      const day = parseInt(match[3], 10);
      if (this.isValidDate(day, month, year)) {
        return `${year}-${match[2]}-${match[3]}`;
      }
    }

    // Try dd/mm/yyyy format
    match = trimmed.match(/^(\d{1,2})[\/\-\.](\d{1,2})[\/\-\.](\d{4})$/);
    if (match) {
      const day = parseInt(match[1], 10);
      const month = parseInt(match[2], 10);
      const year = parseInt(match[3], 10);
      if (this.isValidDate(day, month, year)) {
        const dayStr = day.toString().padStart(2, '0');
        const monthStr = month.toString().padStart(2, '0');
        return `${year}-${monthStr}-${dayStr}`;
      }
    }

    // Try mm/dd/yyyy format (US format) - convert to dd/mm/yyyy
    match = trimmed.match(/^(\d{1,2})[\/\-\.](\d{1,2})[\/\-\.](\d{4})$/);
    if (match) {
      // Assume mm/dd/yyyy if day > 12 (must be month position)
      const first = parseInt(match[1], 10);
      const second = parseInt(match[2], 10);
      const year = parseInt(match[3], 10);

      // If first number > 12, it's definitely dd/mm/yyyy
      // If second number > 12, it's mm/dd/yyyy
      if (second > 12 && first <= 12) {
        // mm/dd/yyyy
        const month = first;
        const day = second;
        if (this.isValidDate(day, month, year)) {
          const dayStr = day.toString().padStart(2, '0');
          const monthStr = month.toString().padStart(2, '0');
          return `${year}-${monthStr}-${dayStr}`;
        }
      }
    }

    return null;
  }

  /**
   * Validate date components.
   */
  private isValidDate(day: number, month: number, year: number): boolean {
    // Basic range checks
    if (month < 1 || month > 12) {
      return false;
    }
    if (day < 1 || day > 31) {
      return false;
    }
    if (year < 1900 || year > 2100) {
      return false;
    }

    // Check days in month
    const daysInMonth = new Date(year, month, 0).getDate();
    if (day > daysInMonth) {
      return false;
    }

    return true;
  }
}
