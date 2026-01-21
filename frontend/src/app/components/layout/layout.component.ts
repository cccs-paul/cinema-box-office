/*
 * Cinema Box Office - Layout Component
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from '../header/header.component';

/**
 * Layout component that wraps authenticated pages with header.
 * Provides consistent layout for all authenticated routes.
 *
 * @author Box Office Team
 * @version 1.0.0
 * @since 2026-01-20
 */
@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent],
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],
})
export class LayoutComponent {}
