import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule],
  template: `
    <header class="app-header">
      <div class="header-brand">
        <div class="brand-symbol">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
            <path d="m9 12 2 2 4-4"/>
          </svg>
        </div>
        <div class="brand-text">
          <span class="brand-title">WARRANTY<span class="highlight">WAVE</span></span>
          <span class="brand-subtitle">Auto Warranty & Financing OS</span>
        </div>
      </div>

      <div class="header-actions">
        <div class="user-pill" *ngIf="authService.currentUser() as user">
          <div class="user-avatar">
            {{ user.fullName.charAt(0) }}
          </div>
          <div class="user-meta">
            <span class="user-name">{{ user.fullName }}</span>
            <span class="user-role-badge" [ngClass]="getRoleClass(user.role)">
              {{ formatRole(user.role) }}
            </span>
          </div>
          <button class="logout-btn" (click)="logout()" title="Sign Out">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
              <polyline points="16 17 21 12 16 7"/>
              <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
          </button>
        </div>
      </div>
    </header>
  `,
  styles: [`
    .app-header {
      height: 64px;
      background: #0d121f;
      border-bottom: 1px solid var(--border-subtle);
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 1.5rem;
      position: sticky;
      top: 0;
      z-index: 100;
    }
    .header-brand {
      display: flex;
      align-items: center;
      gap: 0.75rem;
    }
    .brand-symbol {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      background: linear-gradient(135deg, #1d4ed8 0%, #06b6d4 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #ffffff;
      box-shadow: 0 4px 12px rgba(6, 182, 212, 0.25);
    }
    .brand-title {
      font-size: 1.15rem;
      font-weight: 800;
      letter-spacing: -0.02em;
      color: #ffffff;
    }
    .brand-title .highlight {
      color: #38bdf8;
    }
    .brand-subtitle {
      display: block;
      font-size: 0.675rem;
      color: var(--text-muted);
      text-transform: uppercase;
      letter-spacing: 0.08em;
      font-weight: 600;
    }
    .user-pill {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      background: #141c2c;
      padding: 0.35rem 0.6rem 0.35rem 0.5rem;
      border-radius: 30px;
      border: 1px solid var(--border-subtle);
    }
    .user-avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: #2563eb;
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
      font-size: 0.875rem;
    }
    .user-meta {
      display: flex;
      flex-direction: column;
      line-height: 1.2;
    }
    .user-name {
      font-size: 0.8rem;
      font-weight: 600;
      color: var(--text-main);
    }
    .user-role-badge {
      font-size: 0.65rem;
      font-weight: 700;
      text-transform: uppercase;
    }
    .role-admin { color: #f59e0b; }
    .role-claims { color: #38bdf8; }
    .role-finance { color: #34d399; }
    .logout-btn {
      background: transparent;
      border: none;
      color: var(--text-muted);
      cursor: pointer;
      display: flex;
      align-items: center;
      padding: 0.3rem;
      border-radius: 6px;
      transition: color 0.2s ease;
    }
    .logout-btn:hover {
      color: #f87171;
    }
  `]
})
export class NavbarComponent {
  authService = inject(AuthService);
  private router = inject(Router);

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  formatRole(role: string): string {
    switch (role) {
      case 'ROLE_ADMIN': return 'Administrator';
      case 'ROLE_CLAIMS_OFFICER': return 'Claims Officer';
      case 'ROLE_FINANCE_OFFICER': return 'Finance Officer';
      default: return 'User';
    }
  }

  getRoleClass(role: string): string {
    switch (role) {
      case 'ROLE_ADMIN': return 'role-admin';
      case 'ROLE_CLAIMS_OFFICER': return 'role-claims';
      case 'ROLE_FINANCE_OFFICER': return 'role-finance';
      default: return '';
    }
  }
}
