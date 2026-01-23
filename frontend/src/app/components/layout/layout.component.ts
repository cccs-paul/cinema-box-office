/*
 * myRC - Layout Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from '../header/header.component';
import { SidebarComponent } from '../sidebar/sidebar.component';

/**
 * Layout component that wraps authenticated pages with header and sidebar.
 * Provides consistent layout for all authenticated routes.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-20
 */
@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, SidebarComponent],
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],
})
export class LayoutComponent {}
