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
 * The sidebar is static and always visible. The header and page content
 * scroll together within the main content area.
 *
 * @author myRC Team
 * @version 2.0.0
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
