/*
 * myRC - App Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * Root application component.
 * Simple router outlet for route-based layout switching.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-16
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent {
  title = 'myRC';
}
