/*
 * myRC - Layout Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Component, HostListener } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from '../header/header.component';
import { SidebarComponent } from '../sidebar/sidebar.component';

/**
 * Layout component that wraps authenticated pages with header and sidebar.
 * Provides consistent layout for all authenticated routes.
 * Centrally tracks scroll position to synchronize header and sidebar visibility.
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-01-20
 */
@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, SidebarComponent],
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],
})
export class LayoutComponent {
  /** Whether the header is currently visible (controls header and sidebar simultaneously). */
  isHeaderVisible = true;
  private lastScrollPosition = 0;
  private readonly scrollThreshold = 50;

  /**
   * Track window scroll to determine header visibility.
   * Hides header when scrolling down past threshold, shows when scrolling up.
   * This is the single source of truth passed to both header and sidebar.
   */
  @HostListener('window:scroll', [])
  onWindowScroll(): void {
    const currentScrollPosition = window.pageYOffset || document.documentElement.scrollTop;
    if (currentScrollPosition > this.scrollThreshold) {
      this.isHeaderVisible = currentScrollPosition < this.lastScrollPosition;
    } else {
      this.isHeaderVisible = true;
    }
    this.lastScrollPosition = currentScrollPosition;
  }
}
