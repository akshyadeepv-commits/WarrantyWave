import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './layout/navbar/navbar.component';
import { SidebarComponent } from './layout/sidebar/sidebar.component';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, NavbarComponent, SidebarComponent],
  template: `
    <div class="app-layout" [class.no-auth]="!authService.isAuthenticated()">
      <app-navbar *ngIf="authService.isAuthenticated()"></app-navbar>
      <div class="main-body" *ngIf="authService.isAuthenticated(); else unauthenticated">
        <app-sidebar></app-sidebar>
        <main class="page-content">
          <router-outlet></router-outlet>
        </main>
      </div>
      <ng-template #unauthenticated>
        <main class="auth-content">
          <router-outlet></router-outlet>
        </main>
      </ng-template>
    </div>
  `,
  styles: [`
    .app-layout {
      display: flex;
      flex-direction: column;
      min-height: 100vh;
      background: var(--bg-dark);
    }
    .main-body {
      display: flex;
      flex: 1;
    }
    .page-content {
      flex: 1;
      padding: 2rem;
      max-width: 1400px;
      margin: 0 auto;
      width: 100%;
      overflow-y: auto;
    }
    .auth-content {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
    }
  `]
})
export class App {
  authService = inject(AuthService);
}
