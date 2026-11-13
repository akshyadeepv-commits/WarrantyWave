import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DashboardService } from '../../core/services/dashboard.service';
import { DashboardKpi } from '../../core/models/dashboard.model';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="dashboard-page">
      <!-- Header -->
      <div class="page-header">
        <div>
          <h1 class="page-title">Executive Operations Dashboard</h1>
          <p class="page-subtitle">Real-time metrics, risk exposure, and portfolio health across automotive warranty & financing</p>
        </div>
        <div class="header-actions">
          <a routerLink="/calculator" class="btn btn-secondary">
            <span>🧮</span> EMI Calculator
          </a>
          <a routerLink="/claims" class="btn btn-primary">
            <span>⚙️</span> Adjudicate Claims
          </a>
        </div>
      </div>

      <!-- Loading / Error States -->
      <div *ngIf="isLoading" class="state-card">
        <div class="spinner"></div>
        <p>Loading enterprise analytics and Oracle telemetry...</p>
      </div>

      <div *ngIf="errorMessage" class="state-card error">
        <p>{{ errorMessage }}</p>
        <button class="btn btn-secondary btn-sm" (click)="loadKpis()">Retry</button>
      </div>

      <div *ngIf="!isLoading && kpiData" class="dashboard-content">
        <!-- Top Metric KPI Grid -->
        <div class="kpi-grid">
          <div class="kpi-card">
            <div class="kpi-icon blue">🛡️</div>
            <div class="kpi-info">
              <span class="kpi-label">Active Contracts</span>
              <span class="kpi-value">{{ kpiData.activeContracts }}</span>
              <span class="kpi-sub">Across {{ kpiData.totalVehicles }} covered vehicles</span>
            </div>
          </div>

          <div class="kpi-card">
            <div class="kpi-icon green">💳</div>
            <div class="kpi-info">
              <span class="kpi-label">Approved Financing</span>
              <span class="kpi-value">\${{ kpiData.totalFinancingVolume | number:'1.0-0' }}</span>
              <span class="kpi-sub">{{ kpiData.pendingFinancingCount }} pending approval</span>
            </div>
          </div>

          <div class="kpi-card">
            <div class="kpi-icon amber">⚙️</div>
            <div class="kpi-info">
              <span class="kpi-label">Claims Settled</span>
              <span class="kpi-value">\${{ kpiData.totalApprovedClaimsAmount | number:'1.0-0' }}</span>
              <span class="kpi-sub">{{ kpiData.pendingClaimsCount }} claims awaiting review</span>
            </div>
          </div>

          <div class="kpi-card">
            <div class="kpi-icon purple">📈</div>
            <div class="kpi-info">
              <span class="kpi-label">Monthly Premium ARR</span>
              <span class="kpi-value">\${{ kpiData.monthlyRevenue | number:'1.2-2' }}</span>
              <span class="kpi-sub">\${{ (kpiData.monthlyRevenue * 12) | number:'1.0-0' }} Annual Run-Rate</span>
            </div>
          </div>

          <div class="kpi-card">
            <div class="kpi-icon cyan">📊</div>
            <div class="kpi-info">
              <span class="kpi-label">Claims Loss Ratio</span>
              <span class="kpi-value" [ngClass]="getLossRatioColor(kpiData.claimsLossRatio)">
                {{ kpiData.claimsLossRatio | number:'1.1-1' }}%
              </span>
              <span class="kpi-sub">{{ getLossRatioHealth(kpiData.claimsLossRatio) }}</span>
            </div>
          </div>

          <div class="kpi-card">
            <div class="kpi-icon teal">👥</div>
            <div class="kpi-info">
              <span class="kpi-label">Customer Base</span>
              <span class="kpi-value">{{ kpiData.totalCustomers }}</span>
              <span class="kpi-sub">Total enrolled automotive owners</span>
            </div>
          </div>
        </div>

        <!-- Plan Breakdown Section -->
        <div class="card plan-breakdown-card">
          <div class="card-header">
            <h3>Warranty Product Portfolio Distribution</h3>
            <span class="header-tag">Live Policy Shares</span>
          </div>
          <div class="plans-bars">
            <div *ngFor="let plan of kpiData.planDistribution" class="plan-bar-item">
              <div class="plan-bar-meta">
                <span class="plan-bar-name">{{ plan.planName }}</span>
                <span class="plan-bar-stat">{{ plan.activeContractsCount }} contracts ({{ plan.percentage }}%)</span>
              </div>
              <div class="progress-track">
                <div class="progress-fill" [style.width.%]="plan.percentage"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Two Columns: Recent Claims & Recent Financing -->
        <div class="recent-grid">
          <!-- Recent Claims -->
          <div class="card">
            <div class="card-header">
              <h3>Recent Warranty Claims</h3>
              <a routerLink="/claims" class="view-all-link">View All &rarr;</a>
            </div>
            <div class="table-container">
              <table class="enterprise-table">
                <thead>
                  <tr>
                    <th>Claim #</th>
                    <th>Customer</th>
                    <th>Estimate</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let claim of kpiData.recentClaims">
                    <td>
                      <strong>{{ claim.claimNumber }}</strong>
                      <div class="sub-text">{{ claim.repairShopName }}</div>
                    </td>
                    <td>
                      <div>{{ claim.customerName }}</div>
                      <div class="sub-text">{{ claim.vehicleName }}</div>
                    </td>
                    <td>
                      <span class="amount-highlight">\${{ claim.repairEstimate | number:'1.2-2' }}</span>
                      <div class="sub-text" *ngIf="claim.approvedAmount">Appr: \${{ claim.approvedAmount | number:'1.0-0' }}</div>
                    </td>
                    <td>
                      <span class="badge" [ngClass]="'badge-' + claim.status.toLowerCase()">
                        {{ claim.status }}
                      </span>
                    </td>
                  </tr>
                  <tr *ngIf="!kpiData.recentClaims || kpiData.recentClaims.length === 0">
                    <td colspan="4" class="empty-state">No recent claims filed</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Recent Financing Applications -->
          <div class="card">
            <div class="card-header">
              <h3>Recent Financing Applications</h3>
              <a routerLink="/financing" class="view-all-link">View All &rarr;</a>
            </div>
            <div class="table-container">
              <table class="enterprise-table">
                <thead>
                  <tr>
                    <th>App #</th>
                    <th>Customer</th>
                    <th>Loan / EMI</th>
                    <th>Tier / Status</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let fin of kpiData.recentFinancing">
                    <td>
                      <strong>{{ fin.applicationNumber }}</strong>
                      <div class="sub-text">{{ fin.loanTermMonths }} Mo &#64; {{ fin.interestRate }}%</div>
                    </td>
                    <td>
                      <div>{{ fin.customerName }}</div>
                      <div class="sub-text">{{ fin.vehicleName }}</div>
                    </td>
                    <td>
                      <div><strong>\${{ fin.loanAmount | number:'1.0-0' }}</strong></div>
                      <div class="amount-highlight">\${{ fin.calculatedEmi | number:'1.2-2' }}/mo</div>
                    </td>
                    <td>
                      <div>
                        <span class="badge" [ngClass]="'badge-' + fin.status.toLowerCase()">
                          {{ fin.status }}
                        </span>
                      </div>
                      <div class="sub-text">{{ formatCreditTier(fin.creditTier) }}</div>
                    </td>
                  </tr>
                  <tr *ngIf="!kpiData.recentFinancing || kpiData.recentFinancing.length === 0">
                    <td colspan="4" class="empty-state">No recent financing applications</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-page {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 1rem;
    }
    .page-title {
      font-size: 1.65rem;
      font-weight: 800;
      color: #ffffff;
      letter-spacing: -0.02em;
    }
    .page-subtitle {
      font-size: 0.85rem;
      color: var(--text-secondary);
      margin-top: 0.25rem;
    }
    .header-actions {
      display: flex;
      gap: 0.75rem;
    }
    .kpi-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1rem;
    }
    .kpi-card {
      background: var(--bg-card);
      border: 1px solid var(--border-subtle);
      border-radius: 0.75rem;
      padding: 1.25rem;
      display: flex;
      align-items: center;
      gap: 1rem;
      transition: all 0.2s ease;
    }
    .kpi-card:hover {
      border-color: #3b82f6;
      transform: translateY(-2px);
    }
    .kpi-icon {
      width: 44px;
      height: 44px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.25rem;
    }
    .kpi-icon.blue { background: rgba(37, 99, 235, 0.15); color: #60a5fa; }
    .kpi-icon.green { background: rgba(16, 185, 129, 0.15); color: #34d399; }
    .kpi-icon.amber { background: rgba(245, 158, 11, 0.15); color: #fbbf24; }
    .kpi-icon.purple { background: rgba(168, 85, 247, 0.15); color: #c084fc; }
    .kpi-icon.cyan { background: rgba(6, 182, 212, 0.15); color: #38bdf8; }
    .kpi-icon.teal { background: rgba(20, 184, 166, 0.15); color: #2dd4bf; }
    .kpi-info {
      display: flex;
      flex-direction: column;
    }
    .kpi-label {
      font-size: 0.75rem;
      font-weight: 700;
      color: var(--text-muted);
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }
    .kpi-value {
      font-size: 1.4rem;
      font-weight: 800;
      color: #ffffff;
      line-height: 1.2;
      margin: 0.15rem 0;
    }
    .kpi-sub {
      font-size: 0.7rem;
      color: var(--text-secondary);
    }
    .plan-breakdown-card {
      margin-top: 0.5rem;
    }
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.25rem;
    }
    .card-header h3 {
      font-size: 1.1rem;
      font-weight: 700;
      color: #ffffff;
    }
    .header-tag {
      font-size: 0.725rem;
      color: var(--text-muted);
      background: #111827;
      padding: 0.2rem 0.5rem;
      border-radius: 4px;
      border: 1px solid var(--border-subtle);
    }
    .plans-bars {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }
    .plan-bar-meta {
      display: flex;
      justify-content: space-between;
      font-size: 0.825rem;
      margin-bottom: 0.35rem;
    }
    .plan-bar-name {
      font-weight: 600;
      color: var(--text-main);
    }
    .plan-bar-stat {
      color: var(--text-secondary);
    }
    .progress-track {
      height: 8px;
      background: #0d131f;
      border-radius: 9999px;
      overflow: hidden;
      border: 1px solid var(--border-subtle);
    }
    .progress-fill {
      height: 100%;
      background: linear-gradient(90deg, #2563eb, #06b6d4);
      border-radius: 9999px;
      transition: width 0.6s ease;
    }
    .recent-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1.5rem;
      margin-top: 0.5rem;
    }
    @media (max-width: 900px) {
      .recent-grid {
        grid-template-columns: 1fr;
      }
    }
    .view-all-link {
      color: #38bdf8;
      font-size: 0.8rem;
      font-weight: 600;
      text-decoration: none;
    }
    .view-all-link:hover {
      text-decoration: underline;
    }
    .sub-text {
      font-size: 0.725rem;
      color: var(--text-muted);
    }
    .empty-state {
      text-align: center;
      padding: 2rem !important;
      color: var(--text-muted);
    }
    .state-card {
      background: var(--bg-card);
      border: 1px solid var(--border-subtle);
      border-radius: 0.75rem;
      padding: 3rem 1.5rem;
      text-align: center;
      color: var(--text-secondary);
    }
    .spinner {
      width: 36px;
      height: 36px;
      border: 3px solid rgba(59, 130, 246, 0.2);
      border-top-color: #3b82f6;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin: 0 auto 1rem auto;
    }
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
    .loss-safe { color: #34d399 !important; }
    .loss-warning { color: #fbbf24 !important; }
    .loss-danger { color: #f87171 !important; }
  `]
})
export class DashboardComponent implements OnInit {
  private dashboardService = inject(DashboardService);
  authService = inject(AuthService);

  kpiData: DashboardKpi | null = null;
  isLoading = true;
  errorMessage = '';

  ngOnInit(): void {
    this.loadKpis();
  }

  loadKpis(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.dashboardService.getKpis().subscribe({
      next: (data) => {
        this.kpiData = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Failed to load executive KPIs from backend API.';
      }
    });
  }

  getLossRatioColor(ratio: number): string {
    if (ratio < 40) return 'loss-safe';
    if (ratio <= 65) return 'loss-warning';
    return 'loss-danger';
  }

  getLossRatioHealth(ratio: number): string {
    if (ratio < 40) return 'Optimal Underwriting Margin';
    if (ratio <= 65) return 'Healthy Risk Threshold';
    return 'Elevated Loss Frequency';
  }

  formatCreditTier(tier: string): string {
    switch (tier) {
      case 'TIER_1_EXCELLENT': return 'Tier 1 (750+)';
      case 'TIER_2_GOOD': return 'Tier 2 (670-749)';
      case 'TIER_3_FAIR': return 'Tier 3 (580-669)';
      case 'HIGH_RISK': return 'High Risk (<580)';
      default: return tier;
    }
  }
}
