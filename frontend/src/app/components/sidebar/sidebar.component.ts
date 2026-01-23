/*
 * Cinema Box Office - Sidebar Component
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';

/**
 * Sidebar navigation component for authenticated pages.
 * Provides main navigation menu for the application.
 *
 * @author Box Office Team
 * @version 1.0.0
 * @since 2026-01-21
 */
@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss'],
})
export class SidebarComponent implements OnInit {
  menuItems = [
    {
      label: 'RC Selection',
      icon: '📋',
      route: '/rc-selection',
      badge: null,
    },
    {
      label: 'Dashboard',
      icon: '📊',
      route: '/app/dashboard',
      badge: null,
    },
    {
      label: 'Developer Tools',
      icon: '🛠️',
      route: '/app/developer-tools',
      badge: null,
    },
  ];

  ngOnInit(): void {}
}
