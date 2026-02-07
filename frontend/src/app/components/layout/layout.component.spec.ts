/**
 * myRC - Layout Component Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Component } from '@angular/core';
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
class HeaderStubComponent {}

/**
 * Stub component for app-sidebar.
 */
@Component({
  standalone: true,
  selector: 'app-sidebar',
  template: '<div>Sidebar Stub</div>'
})
class SidebarStubComponent {}

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

  it('should have header inside the main content area', () => {
    const main = fixture.nativeElement.querySelector('.app-main');
    const headerInMain = main.querySelector('app-header');
    expect(headerInMain).toBeTruthy();
  });

  it('should have sidebar outside the main content area', () => {
    const main = fixture.nativeElement.querySelector('.app-main');
    const sidebarInMain = main.querySelector('app-sidebar');
    expect(sidebarInMain).toBeNull();
  });

  it('should have app-content wrapper for router-outlet', () => {
    const content = fixture.nativeElement.querySelector('.app-content');
    expect(content).toBeTruthy();
    const outlet = content.querySelector('router-outlet');
    expect(outlet).toBeTruthy();
  });
});
