/*
 * myRC - Layout Without Sidebar Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from '../header/header.component';

/**
 * Layout component that wraps authenticated pages with header only (no sidebar).
 * Used for RC selection page where sidebar is not needed.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-23
 */
@Component({
  selector: 'app-layout-no-sidebar',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent],
  templateUrl: './layout-no-sidebar.component.html',
  styleUrls: ['./layout-no-sidebar.component.scss'],
})
export class LayoutNoSidebarComponent {}
