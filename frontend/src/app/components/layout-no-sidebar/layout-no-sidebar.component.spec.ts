/*
 * myRC - Layout Without Sidebar Component Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { LayoutNoSidebarComponent } from './layout-no-sidebar.component';
import { HeaderComponent } from '../header/header.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';

describe('LayoutNoSidebarComponent', () => {
  let component: LayoutNoSidebarComponent;
  let fixture: ComponentFixture<LayoutNoSidebarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        LayoutNoSidebarComponent,
        RouterTestingModule,
        HttpClientTestingModule,
        TranslateModule.forRoot()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LayoutNoSidebarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render the header component', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const header = compiled.querySelector('app-header');
    expect(header).toBeTruthy();
  });

  it('should render the main content area without sidebar', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const main = compiled.querySelector('.app-main-no-sidebar');
    expect(main).toBeTruthy();
  });

  it('should not render sidebar', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const sidebar = compiled.querySelector('app-sidebar');
    expect(sidebar).toBeFalsy();
  });

  it('should contain router outlet', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const routerOutlet = compiled.querySelector('router-outlet');
    expect(routerOutlet).toBeTruthy();
  });
});
