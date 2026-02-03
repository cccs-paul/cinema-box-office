/**
 * myRC - Currency Input Directive Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Component, DebugElement } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { CurrencyInputDirective } from './currency-input.directive';

/**
 * Test host component for the currency input directive.
 */
@Component({
  template: `<input currencyInput [(ngModel)]="value" [disabled]="isDisabled" />`,
  standalone: true,
  imports: [CurrencyInputDirective, FormsModule]
})
class TestHostComponent {
  value: number | null = null;
  isDisabled = false;
}

describe('CurrencyInputDirective', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let component: TestHostComponent;
  let inputEl: DebugElement;
  let inputNative: HTMLInputElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    component = fixture.componentInstance;
    inputEl = fixture.debugElement.query(By.directive(CurrencyInputDirective));
    inputNative = inputEl.nativeElement;
    fixture.detectChanges();
  });

  describe('Initialization', () => {
    it('should create the directive', () => {
      expect(inputEl).toBeTruthy();
    });

    it('should set inputmode to decimal on init', () => {
      expect(inputNative.getAttribute('inputmode')).toBe('decimal');
    });

    it('should have empty value initially', () => {
      expect(inputNative.value).toBe('');
    });
  });

  describe('writeValue (model to view)', () => {
    it('should format numeric value with thousand separators', async () => {
      component.value = 1234567;
      fixture.detectChanges();
      await fixture.whenStable();
      expect(inputNative.value).toBe('1,234,567');
    });

    it('should format value with decimals', async () => {
      component.value = 1234.56;
      fixture.detectChanges();
      await fixture.whenStable();
      expect(inputNative.value).toBe('1,234.56');
    });

    it('should handle zero', async () => {
      component.value = 0;
      fixture.detectChanges();
      await fixture.whenStable();
      expect(inputNative.value).toBe('0');
    });

    it('should clear input when value is null', async () => {
      component.value = 1000;
      fixture.detectChanges();
      await fixture.whenStable();
      
      component.value = null;
      fixture.detectChanges();
      await fixture.whenStable();
      expect(inputNative.value).toBe('');
    });
  });

  describe('Input parsing (view to model)', () => {
    it('should parse plain number', async () => {
      inputNative.value = '1234';
      inputNative.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.value).toBe(1234);
    });

    it('should parse number with commas', async () => {
      inputNative.value = '1,234,567';
      inputNative.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.value).toBe(1234567);
    });

    it('should parse number with decimals', async () => {
      inputNative.value = '1234.56';
      inputNative.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.value).toBe(1234.56);
    });

    it('should parse number with commas and decimals', async () => {
      inputNative.value = '2,198,957.89';
      inputNative.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.value).toBe(2198957.89);
    });

    it('should remove dollar sign', async () => {
      inputNative.value = '$1,234';
      inputNative.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.value).toBe(1234);
    });

    it('should remove euro sign', async () => {
      inputNative.value = '€500';
      inputNative.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.value).toBe(500);
    });

    it('should remove pound sign', async () => {
      inputNative.value = '£100';
      inputNative.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.value).toBe(100);
    });

    it('should remove yen sign', async () => {
      inputNative.value = '¥1000';
      inputNative.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.value).toBe(1000);
    });

    it('should remove rupee sign', async () => {
      inputNative.value = '₹5000';
      inputNative.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.value).toBe(5000);
    });

    it('should remove spaces', async () => {
      inputNative.value = '1 234 567';
      inputNative.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.value).toBe(1234567);
    });

    it('should return null for empty string', async () => {
      component.value = 1000;
      fixture.detectChanges();
      await fixture.whenStable();

      inputNative.value = '';
      inputNative.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.value).toBeNull();
    });

    it('should return null for whitespace only', async () => {
      inputNative.value = '   ';
      inputNative.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.value).toBeNull();
    });

    it('should return null for invalid input', async () => {
      inputNative.value = 'abc';
      inputNative.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.value).toBeNull();
    });
  });

  describe('Blur behavior', () => {
    it('should format value on blur', async () => {
      inputNative.value = '1234567';
      inputNative.dispatchEvent(new Event('input'));
      inputNative.dispatchEvent(new Event('blur'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(inputNative.value).toBe('1,234,567');
    });

    it('should format value with decimals on blur', async () => {
      inputNative.value = '1234.56';
      inputNative.dispatchEvent(new Event('input'));
      inputNative.dispatchEvent(new Event('blur'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(inputNative.value).toBe('1,234.56');
    });

    it('should not change empty value on blur', async () => {
      inputNative.value = '';
      inputNative.dispatchEvent(new Event('blur'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(inputNative.value).toBe('');
    });
  });

  describe('Focus behavior', () => {
    it('should show unformatted value on focus', async () => {
      component.value = 1234567;
      fixture.detectChanges();
      await fixture.whenStable();
      expect(inputNative.value).toBe('1,234,567');

      inputNative.dispatchEvent(new Event('focus'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(inputNative.value).toBe('1234567');
    });

    it('should keep decimals on focus', async () => {
      component.value = 1234.56;
      fixture.detectChanges();
      await fixture.whenStable();
      
      inputNative.dispatchEvent(new Event('focus'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(inputNative.value).toBe('1234.56');
    });
  });

  describe('Disabled state', () => {
    it('should handle disabled state', async () => {
      component.isDisabled = true;
      fixture.detectChanges();
      await fixture.whenStable();
      expect(inputNative.disabled).toBeTrue();
    });

    it('should handle enabled state', async () => {
      component.isDisabled = false;
      fixture.detectChanges();
      await fixture.whenStable();
      expect(inputNative.disabled).toBeFalse();
    });
  });

  describe('Edge cases', () => {
    it('should handle very large numbers', async () => {
      inputNative.value = '999,999,999,999.99';
      inputNative.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.value).toBe(999999999999.99);
    });

    it('should handle small decimal values', async () => {
      inputNative.value = '0.01';
      inputNative.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.value).toBe(0.01);
    });

    it('should handle negative values', async () => {
      inputNative.value = '-1234';
      inputNative.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.value).toBe(-1234);
    });

    it('should handle mixed formatting', async () => {
      inputNative.value = '$ 1,234,567.89';
      inputNative.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.value).toBe(1234567.89);
    });
  });
});
