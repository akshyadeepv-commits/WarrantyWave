import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-wrapper">
      <div class="login-card">
        <div class="brand-header">
          <div class="brand-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
              <path d="m9 12 2 2 4-4"/>
            </svg>
          </div>
          <h2>WARRANTY<span class="highlight">WAVE</span></h2>
          <p class="tagline">Enterprise Automotive Warranty & Financing OS</p>
        </div>

        <div class="alert alert-danger" *ngIf="errorMessage">
          {{ errorMessage }}
        </div>

        <form (ngSubmit)="onSubmit()" class="login-form">
          <div class="form-group">
            <label class="form-label">Username</label>
            <input
              type="text"
              name="username"
              [(ngModel)]="username"
              required
              class="form-control"
              placeholder="e.g. admin"
              [disabled]="isLoading"
            />
          </div>

          <div class="form-group">
            <label class="form-label">Password</label>
            <input
              type="password"
              name="password"
              [(ngModel)]="password"
              required
              class="form-control"
              placeholder="••••••••••••"
              [disabled]="isLoading"
            />
          </div>

          <button type="submit" class="btn btn-primary submit-btn" [disabled]="isLoading || !username || !password">
            <span *ngIf="isLoading">Authenticating...</span>
            <span *ngIf="!isLoading">Sign In to Console &rarr;</span>
          </button>
        </form>

        <div class="demo-accounts">
          <div class="demo-title">⚡ Quick Demo Login (Select Role):</div>
          <div class="demo-buttons">
            <button type="button" class="demo-btn admin" (click)="setCredentials('admin', 'Password@123')">
              <span class="role-icon">👑</span>
              <span class="role-desc">
                <strong>Admin</strong>
                <small>Alexander Wright</small>
              </span>
            </button>
            <button type="button" class="demo-btn claims" (click)="setCredentials('claims_officer', 'Password@123')">
              <span class="role-icon">🛡️</span>
              <span class="role-desc">
                <strong>Claims Officer</strong>
                <small>Samantha Reed</small>
              </span>
            </button>
            <button type="button" class="demo-btn finance" (click)="setCredentials('finance_officer', 'Password@123')">
              <span class="role-icon">💳</span>
              <span class="role-desc">
                <strong>Finance Officer</strong>
                <small>Marcus Chen</small>
              </span>
            </button>
          </div>
        </div>

        <div class="db-badge">
          <span>Backend: Java 21 Spring Boot</span>
          <span>•</span>
          <span>Database: Oracle Database 23c</span>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-wrapper {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      padding: 1.5rem;
    }
    .login-card {
      width: 100%;
      max-width: 440px;
      background: #111726;
      border: 1px solid var(--border-subtle);
      border-radius: 1rem;
      padding: 2.25rem 2rem;
      box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.7);
    }
    .brand-header {
      text-align: center;
      margin-bottom: 2rem;
    }
    .brand-icon {
      width: 54px;
      height: 54px;
      border-radius: 12px;
      background: linear-gradient(135deg, #1d4ed8 0%, #06b6d4 100%);
      color: white;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 0.75rem;
      box-shadow: 0 8px 20px rgba(6, 182, 212, 0.3);
    }
    .brand-header h2 {
      font-size: 1.5rem;
      font-weight: 800;
      color: #ffffff;
      letter-spacing: -0.02em;
    }
    .highlight {
      color: #38bdf8;
    }
    .tagline {
      font-size: 0.8rem;
      color: var(--text-muted);
      margin-top: 0.25rem;
    }
    .alert {
      padding: 0.75rem 1rem;
      border-radius: 0.5rem;
      font-size: 0.85rem;
      margin-bottom: 1.25rem;
    }
    .alert-danger {
      background: rgba(239, 68, 68, 0.15);
      border: 1px solid rgba(239, 68, 68, 0.3);
      color: #f87171;
    }
    .submit-btn {
      width: 100%;
      padding: 0.75rem;
      font-size: 0.95rem;
      margin-top: 0.5rem;
    }
    .demo-accounts {
      margin-top: 1.75rem;
      padding-top: 1.25rem;
      border-top: 1px solid var(--border-subtle);
    }
    .demo-title {
      font-size: 0.75rem;
      font-weight: 700;
      color: var(--text-secondary);
      margin-bottom: 0.75rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    .demo-buttons {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }
    .demo-btn {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.5rem 0.75rem;
      border-radius: 0.5rem;
      background: #172033;
      border: 1px solid var(--border-subtle);
      color: var(--text-main);
      cursor: pointer;
      text-align: left;
      transition: all 0.2s ease;
    }
    .demo-btn:hover {
      background: #1f2b45;
      border-color: #3b82f6;
      transform: translateX(3px);
    }
    .role-icon {
      font-size: 1.25rem;
    }
    .role-desc {
      display: flex;
      flex-direction: column;
      line-height: 1.2;
    }
    .role-desc strong {
      font-size: 0.85rem;
    }
    .role-desc small {
      font-size: 0.7rem;
      color: var(--text-muted);
    }
    .db-badge {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 0.5rem;
      margin-top: 1.5rem;
      font-size: 0.7rem;
      color: var(--text-muted);
    }
  `]
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  username = 'admin';
  password = 'Password@123';
  isLoading = false;
  errorMessage = '';

  setCredentials(u: string, p: string): void {
    this.username = u;
    this.password = p;
    this.errorMessage = '';
    this.onSubmit();
  }

  onSubmit(): void {
    if (!this.username || !this.password) return;

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login({ username: this.username, password: this.password }).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err?.error?.message || 'Invalid username or credentials. Please check your credentials.';
      }
    });
  }
}
