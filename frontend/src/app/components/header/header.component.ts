import { Component } from '@angular/core';

@Component({
  selector: 'app-header',
  standalone: true,
  template: '<header><h1>Cinema Box Office</h1></header>',
  styles: ['header { background: #333; color: white; padding: 1rem; }']
})
export class HeaderComponent {}
