/**
 * myRC - Layout Component Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { BehaviorSubject, of } from 'rxjs';
import { LayoutComponent } from './layout.component';
import { HeaderComponent } from '../header/header.component';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { AuthService } from '../../services/auth.service';
import { ThemeService, Theme } from '../../services/theme.service';
import { LanguageService, Language } from '../../services/language.service';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';

describe('LayoutComponent', () => {
  let component: LayoutComponent;
  let fixture: ComponentFixture<LayoutComponent>;

  beforeEach(async () => {
    const authServiceMock = {
      currentUser$: new BehaviorSubject(null)
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

    const rcServiceMock = {
      selectedRC$: new BehaviorSubject<number | null>(null),
      selectedFY$: new BehaviorSubject<number | null>(null),
      getResponsibilityCentre: jasmine.createSpy().and.returnValue(of(null))
    };

    const fyServiceMock = {
      getFiscalYear: jasmine.createSpy().and.returnValue(of(null))
    };

    await TestBed.configureTestingModule({
      imports: [
        LayoutComponent,
        RouterTestingModule,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: ThemeService, useValue: themeServiceMock },
        { provide: LanguageService, useValue: languageServiceMock },
        { provide: ResponsibilityCentreService, useValue: rcServiceMock },
        { provide: FiscalYearService, useValue: fyServiceMock }
      ]
    }).compileComponents();

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
});
