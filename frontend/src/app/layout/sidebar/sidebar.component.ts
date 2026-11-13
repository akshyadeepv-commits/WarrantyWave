import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

interface NavItem {
  label: string;
  route: string;
  icon: string;
  adminOnly?: boolean;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <aside class="app-sidebar">
      <nav class="nav-menu">
        <div class="nav-section-title">PLATFORM CORE</div>
        
        <a routerLink="/dashboard" routerLinkActive="active" class="nav-link">
          <span class="nav-icon">📊</span>
          <span class="nav-text">Executive Dashboard</span>
        </a>

        <a routerLink="/customers" routerLinkActive="active" class="nav-link">
          <span class="nav-icon">👥</span>
          <span class="nav-text">Customers</span>
        </a>

        <a routerLink="/vehicles" routerLinkActive="active" class="nav-link">
          <span class="nav-icon">🚗</span>
          <span class="nav-text">Vehicle Fleet</span>
        </a>

        <div class="nav-section-title">WARRANTY & SERVICING</div>

        <a routerLink="/warranty-plans" routerLinkActive="active" class="nav-link">
          <span class="nav-icon">🛡️</span>
          <span class="nav-text">Warranty Plans</span>
        </a>

        <a routerLink="/contracts" routerLinkActive="active" class="nav-link">
          <span class="nav-icon">📜</span>
          <span class="nav-text">Contracts & Renewals</span>
        </a>

        <a routerLink="/claims" routerLinkActive="active" class="nav-link">
          <span class="nav-icon">⚙️</span>
          <span class="nav-text">Claims Adjudication</span>
        </a>

        <div class="nav-section-title">FINANCE & UNDERWRITING</div>

        <a routerLink="/financing" routerLinkActive="active" class="nav-link">
          <span class="nav-icon">💳</span>
          <span class="nav-text">Auto Loans & Leasing</span>
        </a>

        <a routerLink="/calculator" routerLinkActive="active" class="nav-link">
          <span class="nav-icon">🧮</span>
          <span class="nav-text">EMI Calculator</span>
        </a>

        <div class="nav-section-title" *ngIf="authService.isAdmin()">SYSTEM COMPLIANCE</div>

        <a routerLink="/audit-logs" routerLinkActive="active" class="nav-link" *ngIf="authService.isAdmin()">
          <span class="nav-icon">🔒</span>
          <span class="nav-text">Audit Trail</span>
        </a>
      </nav>

      <div class="sidebar-footer">
        <div class="oracle-status-pill">
          <span class="status-indicator"></span>
          <span class="status-text">Oracle DB 23c Ready</span>
        </div>
      </div>
    </aside>
  `,
  styles: [`
    .app-sidebar {
      width: 240px;
      background: #0d121f;
      border-right: 1px solid var(--border-subtle);
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      height: calc(100vh - 64px);
      position: sticky;
      top: 64px;
      padding: 1.25rem 0.75rem;
    }
    .nav-menu {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }
    .nav-section-title {
      font-size: 0.65rem;
      font-weight: 700;
      color: var(--text-muted);
      letter-spacing: 0.08em;
      text-transform: uppercase;
      padding: 1rem 0.75rem 0.35rem 0.75rem;
    }
    .nav-link {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.65rem 0.75rem;
      border-radius: 0.5rem;
      color: var(--text-secondary);
      text-decoration: none;
      font-size: 0.85rem;
      font-weight: 600;
      transition: all 0.15s ease-in-out;
    }
    .nav-link:hover {
      background: rgba(255, 255, 255, 0.04);
      color: var(--text-main);
    }
    .nav-link.active {
      background: linear-gradient(90deg, rgba(37, 99, 235, 0.18) 0%, rgba(37, 99, 235, 0.04) 100%);
      color: #60a5fa;
      border-left: 3px solid #3b82f6;
    }
    .nav-icon {
      font-size: 1rem;
    }
    .sidebar-footer {
      padding: 0.75rem;
      border-top: 1px solid var(--border-subtle);
    }
    .oracle-status-pill {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      background: rgba(16, 185, 129, 0.08);
      border: 1px solid rgba(16, 185, 129, 0.2);
      padding: 0.35rem 0.6rem;
      border-radius: 6px;
    }
    .status-indicator {
      width: 7px;
      height: 7px;
      border-radius: 50%;
      background: #10b981;
      box-shadow: 0 0 8px #10b981;
    }
    .status-text {
      font-size: 0.7rem;
      font-weight: 600;
      color: #34d399;
    }
  `]
})
export class SidebarComponent {
  authService = inject(AuthService);
}
