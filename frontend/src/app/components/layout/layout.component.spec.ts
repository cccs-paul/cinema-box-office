/**
 * myRC - Layout Component Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Component, Input } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterOutlet } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { LayoutComponent } from './layout.component';
import { HeaderComponent } from '../header/header.component';
import { SidebarComponent } from '../sidebar/sidebar.component';

/**
 * Stub component for router-outlet.
 */
@Component({
  standalone: true,
  selector: 'router-outlet',
  template: ''
})
class RouterOutletStubComponent {}

/**
 * Stub component for app-header.
 */
@Component({
  standalone: true,
  selector: 'app-header',
  template: '<div>Header Stub</div>'
})
class HeaderStubComponent {
  @Input() isHeaderVisible = true;
}

/**
 * Stub component for app-sidebar.
 */
@Component({
  standalone: true,
  selector: 'app-sidebar',
  template: '<div>Sidebar Stub</div>'
})
class SidebarStubComponent {
  @Input() isHeaderVisible = true;
}

describe('LayoutComponent', () => {
  let component: LayoutComponent;
  let fixture: ComponentFixture<LayoutComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        LayoutComponent,
        TranslateModule.forRoot()
      ]
    })
    .overrideComponent(LayoutComponent, {
      remove: { imports: [RouterOutlet, HeaderComponent, SidebarComponent] },
      add: { imports: [RouterOutletStubComponent, HeaderStubComponent, SidebarStubComponent] }
    })
    .compileComponents();

    fixture = TestBed.createComponent(LayoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should contain app-header component', () => {
    const header = fixture.nativeElement.querySelector('app-header');
    expect(header).toBeTruthy();
  });

  it('should contain app-sidebar component', () => {
    const sidebar = fixture.nativeElement.querySelector('app-sidebar');
    expect(sidebar).toBeTruthy();
  });

  it('should contain router-outlet', () => {
    const outlet = fixture.nativeElement.querySelector('router-outlet');
    expect(outlet).toBeTruthy();
  });

  describe('Scroll behavior', () => {
    it('should initialize with header visible', () => {
      expect(component.isHeaderVisible).toBeTrue();
    });

    it('should show header when near top of page', () => {
      Object.defineProperty(window, 'pageYOffset', { value: 10, configurable: true });
      component.onWindowScroll();
      expect(component.isHeaderVisible).toBeTrue();
    });

    it('should hide header when scrolling down past threshold', () => {
      // First scroll - establish last position
      Object.defineProperty(window, 'pageYOffset', { value: 100, configurable: true });
      component.onWindowScroll();

      // Second scroll - scrolling down
      Object.defineProperty(window, 'pageYOffset', { value: 200, configurable: true });
      component.onWindowScroll();

      expect(component.isHeaderVisible).toBeFalse();
    });

    it('should show header when scrolling up', () => {
      // First scroll down
      Object.defineProperty(window, 'pageYOffset', { value: 200, configurable: true });
      component.onWindowScroll();

      // Then scroll up
      Object.defineProperty(window, 'pageYOffset', { value: 100, configurable: true });
      component.onWindowScroll();

      expect(component.isHeaderVisible).toBeTrue();
    });

    it('should always show header when below scroll threshold', () => {
      // First scroll past threshold
      Object.defineProperty(window, 'pageYOffset', { value: 100, configurable: true });
      component.onWindowScroll();

      // Scroll down to hide
      Object.defineProperty(window, 'pageYOffset', { value: 200, configurable: true });
      component.onWindowScroll();
      expect(component.isHeaderVisible).toBeFalse();

      // Scroll back below threshold
      Object.defineProperty(window, 'pageYOffset', { value: 30, configurable: true });
      component.onWindowScroll();
      expect(component.isHeaderVisible).toBeTrue();
    });
  });
});
