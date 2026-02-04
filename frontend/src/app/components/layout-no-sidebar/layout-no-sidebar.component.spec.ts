/*
 * myRC - Layout Without Sidebar Component Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Component, Directive, Input } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, RouterOutlet } from '@angular/router';
import { LayoutNoSidebarComponent } from './layout-no-sidebar.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';
import { BehaviorSubject, Subject } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { ThemeService, Theme } from '../../services/theme.service';
import { LanguageService, Language } from '../../services/language.service';

/**
 * Stub component for router-outlet.
 */
@Component({
  standalone: true,
  selector: 'router-outlet',
  template: ''
})
class RouterOutletStubComponent {}

describe('LayoutNoSidebarComponent', () => {
  let component: LayoutNoSidebarComponent;
  let fixture: ComponentFixture<LayoutNoSidebarComponent>;

  beforeEach(async () => {
    const authServiceMock = {
      currentUser$: new BehaviorSubject(null),
      logout: jasmine.createSpy('logout')
    };

    const themeServiceMock = {
      currentTheme$: new BehaviorSubject<Theme>('light'),
      setTheme: jasmine.createSpy('setTheme')
    };

    const languageServiceMock = {
      currentLanguage$: new BehaviorSubject<Language>('en'),
      toggleLanguage: jasmine.createSpy('toggleLanguage'),
      getOtherLanguageNativeName: jasmine.createSpy().and.returnValue('Français')
    };

    const routerMock = {
      navigate: jasmine.createSpy('navigate').and.returnValue(Promise.resolve(true)),
      events: new Subject(),
      routerState: { root: {} }
    };

    await TestBed.configureTestingModule({
      imports: [
        LayoutNoSidebarComponent,
        HttpClientTestingModule,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: ThemeService, useValue: themeServiceMock },
        { provide: LanguageService, useValue: languageServiceMock },
        { provide: Router, useValue: routerMock }
      ]
    })
    .overrideComponent(LayoutNoSidebarComponent, {
      remove: { imports: [RouterOutlet] },
      add: { imports: [RouterOutletStubComponent] }
    })
    .compileComponents();

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
