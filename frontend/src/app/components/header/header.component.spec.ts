/**
 * myRC - Header Component Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router } from '@angular/router';
import { BehaviorSubject, Subject } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { HeaderComponent } from './header.component';
import { AuthService } from '../../services/auth.service';
import { ThemeService, Theme } from '../../services/theme.service';
import { LanguageService, Language } from '../../services/language.service';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let themeService: jasmine.SpyObj<ThemeService>;
  let languageService: jasmine.SpyObj<LanguageService>;
  let router: jasmine.SpyObj<Router>;
  
  let currentUserSubject: BehaviorSubject<any>;
  let currentThemeSubject: BehaviorSubject<Theme>;
  let currentLanguageSubject: BehaviorSubject<Language>;

  const mockUser = {
    username: 'testuser',
    displayName: 'Test User',
    email: 'test@example.com'
  };

  beforeEach(async () => {
    currentUserSubject = new BehaviorSubject<any>(null);
    currentThemeSubject = new BehaviorSubject<Theme>('light');
    currentLanguageSubject = new BehaviorSubject<Language>('en');

    authService = jasmine.createSpyObj('AuthService', ['logout'], {
      currentUser$: currentUserSubject.asObservable()
    });

    themeService = jasmine.createSpyObj('ThemeService', ['setTheme'], {
      currentTheme$: currentThemeSubject.asObservable()
    });

    languageService = jasmine.createSpyObj('LanguageService', 
      ['toggleLanguage', 'getOtherLanguageNativeName'], {
      currentLanguage$: currentLanguageSubject.asObservable()
    });
    languageService.getOtherLanguageNativeName.and.returnValue('Français');

    router = jasmine.createSpyObj('Router', ['navigate'], {
      events: new Subject(),
      routerState: { root: {} }
    });
    router.navigate.and.returnValue(Promise.resolve(true));

    await TestBed.configureTestingModule({
      imports: [HeaderComponent, TranslateModule.forRoot()]
    })
    .overrideProvider(AuthService, { useValue: authService })
    .overrideProvider(ThemeService, { useValue: themeService })
    .overrideProvider(LanguageService, { useValue: languageService })
    .overrideProvider(Router, { useValue: router })
    .compileComponents();

    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('Initialization', () => {
    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize with default values', () => {
      expect(component.isUserMenuOpen).toBeFalse();
      expect(component.isLoggingOut).toBeFalse();
      expect(component.showLogo).toBeFalse();
    });

    it('should not be logged in initially', () => {
      expect(component.isLoggedIn).toBeFalse();
      expect(component.currentUser).toBeNull();
    });
  });

  describe('User subscription', () => {
    it('should update currentUser when user changes', () => {
      currentUserSubject.next(mockUser);
      expect(component.currentUser).toEqual(mockUser);
    });

    it('should set isLoggedIn to true when user exists', () => {
      currentUserSubject.next(mockUser);
      expect(component.isLoggedIn).toBeTrue();
    });

    it('should set isLoggedIn to false when user is null', () => {
      currentUserSubject.next(mockUser);
      currentUserSubject.next(null);
      expect(component.isLoggedIn).toBeFalse();
    });
  });

  describe('Theme subscription', () => {
    it('should update currentTheme when theme changes', () => {
      currentThemeSubject.next('dark');
      expect(component.currentTheme).toBe('dark');
    });

    it('should have light theme initially', () => {
      expect(component.currentTheme).toBe('light');
    });
  });

  describe('Language subscription', () => {
    it('should update currentLanguage when language changes', () => {
      currentLanguageSubject.next('fr');
      expect(component.currentLanguage).toBe('fr');
    });

    it('should have English as initial language', () => {
      expect(component.currentLanguage).toBe('en');
    });
  });

  describe('User menu interactions', () => {
    it('should toggle user menu on toggleUserMenu', () => {
      expect(component.isUserMenuOpen).toBeFalse();
      component.toggleUserMenu();
      expect(component.isUserMenuOpen).toBeTrue();
      component.toggleUserMenu();
      expect(component.isUserMenuOpen).toBeFalse();
    });

    it('should close user menu on closeUserMenu', () => {
      component.isUserMenuOpen = true;
      component.closeUserMenu();
      expect(component.isUserMenuOpen).toBeFalse();
    });

    it('should close menu on document click outside', () => {
      component.isUserMenuOpen = true;
      const event = new MouseEvent('click');
      Object.defineProperty(event, 'target', { value: document.body });
      component.onDocumentClick(event);
      expect(component.isUserMenuOpen).toBeFalse();
    });

    it('should not close menu when clicking inside user-menu-container', () => {
      component.isUserMenuOpen = true;
      
      const container = document.createElement('div');
      container.className = 'user-menu-container';
      const target = document.createElement('span');
      container.appendChild(target);
      document.body.appendChild(container);
      
      const event = new MouseEvent('click');
      Object.defineProperty(event, 'target', { value: target });
      component.onDocumentClick(event);
      
      expect(component.isUserMenuOpen).toBeTrue();
      document.body.removeChild(container);
    });
  });

  describe('Theme functionality', () => {
    it('should toggle theme from light to dark', () => {
      component.currentTheme = 'light';
      component.toggleTheme();
      expect(themeService.setTheme).toHaveBeenCalledWith('dark');
    });

    it('should toggle theme from dark to light', () => {
      component.currentTheme = 'dark';
      component.toggleTheme();
      expect(themeService.setTheme).toHaveBeenCalledWith('light');
    });

    it('should close user menu after theme toggle', () => {
      component.isUserMenuOpen = true;
      component.toggleTheme();
      expect(component.isUserMenuOpen).toBeFalse();
    });

    it('should return moon emoji for light theme', () => {
      component.currentTheme = 'light';
      expect(component.getThemeIcon()).toBe('🌙');
    });

    it('should return sun emoji for dark theme', () => {
      component.currentTheme = 'dark';
      expect(component.getThemeIcon()).toBe('☀️');
    });
  });

  describe('Language functionality', () => {
    it('should call languageService.toggleLanguage', () => {
      component.toggleLanguage();
      expect(languageService.toggleLanguage).toHaveBeenCalled();
    });

    it('should get other language label', () => {
      const label = component.getOtherLanguageLabel();
      expect(languageService.getOtherLanguageNativeName).toHaveBeenCalled();
      expect(label).toBe('Français');
    });
  });

  describe('API Docs', () => {
    it('should open Swagger UI in new tab', () => {
      spyOn(window, 'open');
      component.openApiDocs();
      expect(window.open).toHaveBeenCalledWith('http://localhost:8080/api/swagger-ui.html', '_blank');
    });

    it('should close user menu after opening API docs', () => {
      spyOn(window, 'open');
      component.isUserMenuOpen = true;
      component.openApiDocs();
      expect(component.isUserMenuOpen).toBeFalse();
    });
  });

  describe('Logout', () => {
    it('should set isLoggingOut to true during logout', fakeAsync(() => {
      component.logout();
      expect(component.isLoggingOut).toBeTrue();
      tick();
    }));

    it('should call authService.logout', fakeAsync(() => {
      component.logout();
      tick();
      expect(authService.logout).toHaveBeenCalled();
    }));

    it('should navigate to login page', fakeAsync(() => {
      component.logout();
      tick();
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
    }));

    it('should set isLoggingOut to false after logout', fakeAsync(() => {
      component.logout();
      tick();
      expect(component.isLoggingOut).toBeFalse();
    }));

    it('should handle logout errors gracefully', fakeAsync(() => {
      router.navigate.and.returnValue(Promise.reject('Navigation error'));
      spyOn(console, 'error');
      
      component.logout();
      tick();
      
      expect(console.error).toHaveBeenCalled();
      expect(component.isLoggingOut).toBeFalse();
    }));
  });

  describe('openPreferences', () => {
    it('should navigate to /preferences', () => {
      component.openPreferences();
      expect(router.navigate).toHaveBeenCalledWith(['/preferences']);
    });

    it('should close the user menu', () => {
      component.isUserMenuOpen = true;
      component.openPreferences();
      expect(component.isUserMenuOpen).toBeFalse();
    });
  });

  describe('ngOnDestroy', () => {
    it('should complete destroy$ subject', () => {
      const nextSpy = spyOn(component['destroy$'], 'next');
      const completeSpy = spyOn(component['destroy$'], 'complete');
      
      component.ngOnDestroy();
      
      expect(nextSpy).toHaveBeenCalled();
      expect(completeSpy).toHaveBeenCalled();
    });
  });
});
