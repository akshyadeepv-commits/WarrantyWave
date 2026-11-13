import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FinancingService } from '../../core/services/financing.service';
import { EmiCalculationResponse } from '../../core/models/financing.model';

@Component({
  selector: 'app-calculator',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="calculator-page">
      <div class="page-header">
        <div>
          <h1 class="page-title">Automotive Loan & EMI Calculator</h1>
          <p class="page-subtitle">Interactive financial modeling with risk-adjusted credit tiers and monthly amortized payment schedules</p>
        </div>
      </div>

      <div class="calc-grid">
        <!-- Controls & Sliders Card -->
        <div class="card calc-controls">
          <div class="section-badge">FINANCING PARAMETERS</div>

          <!-- Vehicle Price -->
          <div class="form-group">
            <div class="slider-header">
              <label class="form-label">Vehicle Purchase Price</label>
              <span class="slider-val amount-highlight">\${{ vehiclePrice | number:'1.0-0' }}</span>
            </div>
            <input
              type="range"
              min="5000"
              max="150000"
              step="1000"
              [(ngModel)]="vehiclePrice"
              (input)="recalculate()"
              class="range-slider"
            />
            <div class="slider-limits">
              <span>\$5,000</span>
              <span>\$150,000</span>
            </div>
          </div>

          <!-- Down Payment -->
          <div class="form-group">
            <div class="slider-header">
              <label class="form-label">Down Payment Amount</label>
              <span class="slider-val amount-highlight">\${{ downPayment | number:'1.0-0' }}</span>
            </div>
            <input
              type="range"
              min="0"
              [max]="vehiclePrice * 0.8"
              step="500"
              [(ngModel)]="downPayment"
              (input)="recalculate()"
              class="range-slider"
            />
            <div class="slider-limits">
              <span>\$0 (0%)</span>
              <span>\${{ (vehiclePrice * 0.8) | number:'1.0-0' }} (80%)</span>
            </div>
          </div>

          <!-- Loan Term -->
          <div class="form-group">
            <label class="form-label">Financing Term (Months)</label>
            <div class="term-selector">
              <button
                type="button"
                *ngFor="let term of [24, 36, 48, 60, 72, 84]"
                class="term-pill"
                [class.selected]="loanTermMonths === term"
                (click)="setTerm(term)"
              >
                {{ term }} Mo
              </button>
            </div>
          </div>

          <!-- Credit Score -->
          <div class="form-group">
            <div class="slider-header">
              <label class="form-label">Applicant FICO Credit Score</label>
              <span class="slider-val" [ngClass]="getCreditBadge(creditScore)">{{ creditScore }}</span>
            </div>
            <input
              type="range"
              min="300"
              max="850"
              step="5"
              [(ngModel)]="creditScore"
              (input)="recalculate()"
              class="range-slider"
            />
            <div class="slider-limits">
              <span>Poor (300)</span>
              <span>Fair (580)</span>
              <span>Good (670)</span>
              <span>Excellent (850)</span>
            </div>
          </div>
        </div>

        <!-- Output Results Card -->
        <div class="card calc-results" *ngIf="result">
          <div class="section-badge">AMORTIZATION SUMMARY</div>

          <div class="emi-hero">
            <span class="hero-label">Estimated Monthly Payment</span>
            <div class="hero-val">
              <span class="currency">\$</span>{{ result.monthlyEmi | number:'1.2-2' }}
              <span class="period">/month</span>
            </div>
            <div class="hero-sub">
              Based on <strong>{{ result.estimatedInterestRate }}% APR</strong> for {{ result.loanTermMonths }} months
            </div>
          </div>

          <div class="results-stats">
            <div class="stat-row">
              <span class="stat-name">Vehicle Price</span>
              <span class="stat-num">\${{ result.vehiclePrice | number:'1.2-2' }}</span>
            </div>
            <div class="stat-row">
              <span class="stat-name">Down Payment</span>
              <span class="stat-num text-success">-\${{ result.downPayment | number:'1.2-2' }}</span>
            </div>
            <div class="stat-row highlight-row">
              <span class="stat-name">Total Loan Principal</span>
              <span class="stat-num">\${{ result.loanAmount | number:'1.2-2' }}</span>
            </div>
            <div class="stat-row">
              <span class="stat-name">Total Interest Charges</span>
              <span class="stat-num text-amber">\${{ result.totalInterest | number:'1.2-2' }}</span>
            </div>
            <div class="stat-row total-row">
              <span class="stat-name">Total Financed Repayment</span>
              <span class="stat-num">\${{ result.totalPayment | number:'1.2-2' }}</span>
            </div>
          </div>

          <!-- Principal vs Interest Proportion Bar -->
          <div class="proportion-box">
            <div class="proportion-labels">
              <span>Principal: {{ getPrincipalPercent() }}%</span>
              <span>Interest: {{ getInterestPercent() }}%</span>
            </div>
            <div class="progress-track">
              <div class="progress-principal" [style.width.%]="getPrincipalPercent()"></div>
              <div class="progress-interest" [style.width.%]="getInterestPercent()"></div>
            </div>
          </div>

          <div class="credit-tier-callout">
            <div class="tier-indicator">
              <span>Assessed Risk Tier:</span>
              <span class="badge" [ngClass]="getCreditBadge(creditScore)">
                {{ formatCreditTier(result.creditTier) }}
              </span>
            </div>
            <p class="tier-desc">
              Rate determined by proprietary automated underwriting matrices. Tier 1 borrowers qualify for lowest prime lending rates.
            </p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .calculator-page { display: flex; flex-direction: column; gap: 1.5rem; }
    .page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; }
    .page-title { font-size: 1.65rem; font-weight: 800; color: #ffffff; letter-spacing: -0.02em; }
    .page-subtitle { font-size: 0.85rem; color: var(--text-secondary); margin-top: 0.25rem; }
    .calc-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; align-items: start; }
    @media (max-width: 900px) { .calc-grid { grid-template-columns: 1fr; } }
    .section-badge { font-size: 0.7rem; font-weight: 700; color: #38bdf8; letter-spacing: 0.08em; margin-bottom: 1.25rem; }
    .slider-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.4rem; }
    .slider-val { font-size: 1rem; font-weight: 800; }
    .range-slider {
      width: 100%;
      height: 6px;
      border-radius: 5px;
      background: #1e293b;
      outline: none;
      -webkit-appearance: none;
      cursor: pointer;
    }
    .range-slider::-webkit-slider-thumb {
      -webkit-appearance: none;
      width: 18px;
      height: 18px;
      border-radius: 50%;
      background: #3b82f6;
      cursor: pointer;
      box-shadow: 0 0 10px rgba(59, 130, 246, 0.6);
      transition: transform 0.1s ease;
    }
    .range-slider::-webkit-slider-thumb:hover { transform: scale(1.15); }
    .slider-limits { display: flex; justify-content: space-between; font-size: 0.7rem; color: var(--text-muted); margin-top: 0.35rem; }
    .term-selector { display: grid; grid-template-columns: repeat(3, 1fr); gap: 0.5rem; }
    .term-pill {
      background: #101624;
      border: 1px solid var(--border-subtle);
      color: var(--text-secondary);
      padding: 0.6rem;
      border-radius: 0.5rem;
      font-weight: 700;
      font-size: 0.85rem;
      cursor: pointer;
      transition: all 0.15s ease;
    }
    .term-pill:hover { border-color: #3b82f6; color: #ffffff; }
    .term-pill.selected {
      background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 100%);
      border-color: #3b82f6;
      color: #ffffff;
      box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
    }
    .emi-hero {
      background: linear-gradient(135deg, #172134 0%, #0f1624 100%);
      border: 1px solid var(--border-subtle);
      border-radius: 0.75rem;
      padding: 1.5rem;
      text-align: center;
      margin-bottom: 1.5rem;
    }
    .hero-label { font-size: 0.75rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; }
    .hero-val { font-size: 2.5rem; font-weight: 800; color: #ffffff; margin: 0.25rem 0; line-height: 1.1; }
    .hero-val .currency { font-size: 1.5rem; color: #38bdf8; margin-right: 0.15rem; }
    .hero-val .period { font-size: 0.9rem; color: var(--text-secondary); font-weight: 500; }
    .hero-sub { font-size: 0.8rem; color: var(--text-secondary); }
    .results-stats { display: flex; flex-direction: column; gap: 0.65rem; margin-bottom: 1.5rem; }
    .stat-row { display: flex; justify-content: space-between; font-size: 0.875rem; color: var(--text-secondary); }
    .stat-num { font-weight: 700; color: #ffffff; }
    .highlight-row { padding-top: 0.5rem; border-top: 1px solid var(--border-subtle); font-weight: 700; color: var(--text-main); }
    .total-row { padding-top: 0.5rem; border-top: 1px dashed var(--border-subtle); font-size: 1rem; font-weight: 800; color: #ffffff; }
    .text-success { color: #34d399 !important; }
    .text-amber { color: #fbbf24 !important; }
    .proportion-box { margin-bottom: 1.5rem; }
    .proportion-labels { display: flex; justify-content: space-between; font-size: 0.75rem; color: var(--text-muted); margin-bottom: 0.35rem; }
    .progress-track { display: flex; height: 10px; border-radius: 9999px; overflow: hidden; background: #0d1320; border: 1px solid var(--border-subtle); }
    .progress-principal { background: #3b82f6; height: 100%; transition: width 0.3s ease; }
    .progress-interest { background: #f59e0b; height: 100%; transition: width 0.3s ease; }
    .credit-tier-callout { background: #0d1320; border: 1px solid var(--border-subtle); padding: 1rem; border-radius: 0.5rem; }
    .tier-indicator { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; font-size: 0.8rem; font-weight: 600; }
    .tier-desc { font-size: 0.725rem; color: var(--text-muted); line-height: 1.4; }
  `]
})
export class CalculatorComponent implements OnInit {
  private financingService = inject(FinancingService);

  vehiclePrice = 42000;
  downPayment = 7000;
  loanTermMonths = 60;
  creditScore = 740;

  result: EmiCalculationResponse | null = null;

  ngOnInit(): void {
    this.recalculate();
  }

  setTerm(term: number): void {
    this.loanTermMonths = term;
    this.recalculate();
  }

  recalculate(): void {
    if (this.downPayment >= this.vehiclePrice) {
      this.downPayment = Math.round(this.vehiclePrice * 0.2);
    }

    this.financingService.calculateEmi({
      vehiclePrice: this.vehiclePrice,
      downPayment: this.downPayment,
      loanTermMonths: this.loanTermMonths,
      creditScore: this.creditScore
    }).subscribe({
      next: (res) => this.result = res
    });
  }

  getPrincipalPercent(): number {
    if (!this.result || this.result.totalPayment <= 0) return 100;
    return Math.round((this.result.loanAmount / this.result.totalPayment) * 100);
  }

  getInterestPercent(): number {
    return Math.max(0, 100 - this.getPrincipalPercent());
  }

  getCreditBadge(score: number): string {
    if (score >= 750) return 'badge-tier-1';
    if (score >= 670) return 'badge-tier-2';
    if (score >= 580) return 'badge-tier-3';
    return 'badge-high-risk';
  }

  formatCreditTier(tier: string): string {
    switch (tier) {
      case 'TIER_1_EXCELLENT': return 'Tier 1 (Prime 750+)';
      case 'TIER_2_GOOD': return 'Tier 2 (Good 670-749)';
      case 'TIER_3_FAIR': return 'Tier 3 (Fair 580-669)';
      case 'HIGH_RISK': return 'High Risk (<580)';
      default: return tier;
    }
  }
}
