/*
 * Cinema Box Office - App Component
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './components/header/header.component';

/**
 * Root application component.
 * Provides global header and router outlet for view rendering.
 *
 * @author Box Office Team
 * @version 1.0.0
 * @since 2026-01-16
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent {
  title = 'Cinema Box Office';
}
