/**
 * myRC - Date Input Directive Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-03
 */
import { Component } from '@angular/core';
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { DateInputDirective } from './date-input.directive';

@Component({
  template: `<input type="text" dateInput [(ngModel)]="dateValue" />`,
  standalone: true,
  imports: [FormsModule, DateInputDirective]
})
class TestHostComponent {
  dateValue: string | null = null;
}

describe('DateInputDirective', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let component: TestHostComponent;
  let inputElement: HTMLInputElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    component = fixture.componentInstance;
    inputElement = fixture.nativeElement.querySelector('input');
    fixture.detectChanges();
  });

  describe('Initialization', () => {
    it('should create the directive', () => {
      expect(inputElement).toBeTruthy();
    });

    it('should set placeholder to dd/mm/yyyy', () => {
      expect(inputElement.placeholder).toBe('dd/mm/yyyy');
    });

    it('should set inputmode to numeric', () => {
      expect(inputElement.getAttribute('inputmode')).toBe('numeric');
    });
  });

  describe('Input Formatting', () => {
    it('should auto-format input as dd/mm/yyyy', fakeAsync(() => {
      inputElement.value = '15022026';
      inputElement.dispatchEvent(new Event('input'));
      tick();
      fixture.detectChanges();

      expect(inputElement.value).toBe('15/02/2026');
    }));

    it('should add slashes automatically', fakeAsync(() => {
      inputElement.value = '1502';
      inputElement.dispatchEvent(new Event('input'));
      tick();
      fixture.detectChanges();

      expect(inputElement.value).toBe('15/02');
    }));

    it('should limit to 8 digits', fakeAsync(() => {
      inputElement.value = '150220261234';
      inputElement.dispatchEvent(new Event('input'));
      tick();
      fixture.detectChanges();

      expect(inputElement.value).toBe('15/02/2026');
    }));

    it('should remove non-digit characters', fakeAsync(() => {
      inputElement.value = '15-02-2026';
      inputElement.dispatchEvent(new Event('input'));
      tick();
      fixture.detectChanges();

      expect(inputElement.value).toBe('15/02/2026');
    }));
  });

  describe('Model Binding', () => {
    it('should update model with ISO format', fakeAsync(() => {
      inputElement.value = '15022026';
      inputElement.dispatchEvent(new Event('input'));
      tick();
      fixture.detectChanges();

      expect(component.dateValue).toBe('2026-02-15');
    }));

    it('should display model value in dd/mm/yyyy format', fakeAsync(() => {
      component.dateValue = '2026-03-20';
      fixture.detectChanges();
      tick();

      expect(inputElement.value).toBe('20/03/2026');
    }));

    it('should handle null model value', fakeAsync(() => {
      component.dateValue = null;
      fixture.detectChanges();
      tick();

      expect(inputElement.value).toBe('');
    }));

    it('should return null for incomplete date', fakeAsync(() => {
      inputElement.value = '15/02';
      inputElement.dispatchEvent(new Event('input'));
      tick();
      fixture.detectChanges();

      expect(component.dateValue).toBeNull();
    }));
  });

  describe('Date Validation', () => {
    it('should reject invalid day (32)', fakeAsync(() => {
      inputElement.value = '32/01/2026';
      inputElement.dispatchEvent(new Event('input'));
      tick();
      fixture.detectChanges();

      expect(component.dateValue).toBeNull();
    }));

    it('should reject invalid month (13)', fakeAsync(() => {
      inputElement.value = '15/13/2026';
      inputElement.dispatchEvent(new Event('input'));
      tick();
      fixture.detectChanges();

      expect(component.dateValue).toBeNull();
    }));

    it('should reject invalid February date (30)', fakeAsync(() => {
      inputElement.value = '30/02/2026';
      inputElement.dispatchEvent(new Event('input'));
      tick();
      fixture.detectChanges();

      expect(component.dateValue).toBeNull();
    }));

    it('should accept valid leap year date (29/02)', fakeAsync(() => {
      inputElement.value = '29/02/2024';
      inputElement.dispatchEvent(new Event('input'));
      tick();
      fixture.detectChanges();

      expect(component.dateValue).toBe('2024-02-29');
    }));

    it('should reject invalid leap year date (29/02)', fakeAsync(() => {
      inputElement.value = '29/02/2025';
      inputElement.dispatchEvent(new Event('input'));
      tick();
      fixture.detectChanges();

      expect(component.dateValue).toBeNull();
    }));
  });

  describe('Paste Handling', () => {
    it('should parse pasted ISO format', fakeAsync(() => {
      const pasteEvent = new ClipboardEvent('paste', {
        clipboardData: new DataTransfer()
      });
      pasteEvent.clipboardData?.setData('text', '2026-04-25');
      
      inputElement.dispatchEvent(pasteEvent);
      tick();
      fixture.detectChanges();

      expect(inputElement.value).toBe('25/04/2026');
      expect(component.dateValue).toBe('2026-04-25');
    }));
  });

  describe('Blur Handling', () => {
    it('should reformat on blur', fakeAsync(() => {
      component.dateValue = '2026-05-10';
      fixture.detectChanges();
      tick();

      inputElement.dispatchEvent(new Event('blur'));
      tick();
      fixture.detectChanges();

      expect(inputElement.value).toBe('10/05/2026');
    }));
  });
});
