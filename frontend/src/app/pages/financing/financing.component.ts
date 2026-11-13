import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FinancingService } from '../../core/services/financing.service';
import { CustomerService } from '../../core/services/customer.service';
import { VehicleService } from '../../core/services/vehicle.service';
import { AuthService } from '../../core/services/auth.service';
import {
  FinancingApplication,
  FinancingRequest,
  FinancingReview,
  FinancingStatus,
  CreditTier
} from '../../core/models/financing.model';
import { Customer } from '../../core/models/customer.model';
import { Vehicle } from '../../core/models/vehicle.model';
import { Page } from '../../core/models/page.model';

@Component({
  selector: 'app-financing',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="financing-page">
      <div class="page-header">
        <div>
          <h1 class="page-title">Automotive Financing & Leasing</h1>
          <p class="page-subtitle">Underwrite auto loans, evaluate credit score tiers, compute debt-to-income (DTI), and manage approvals</p>
        </div>
        <div>
          <button class="btn btn-primary" (click)="openSubmitModal()">
            <span>➕</span> New Loan Application
          </button>
        </div>
      </div>

      <!-- Filter Bar -->
      <div class="filter-bar card">
        <div class="search-box">
          <input
            type="text"
            [(ngModel)]="searchQuery"
            (keyup.enter)="loadApplications(0)"
            placeholder="Search by app #, customer, or VIN..."
            class="form-control"
          />
        </div>
        <select [(ngModel)]="statusFilter" (change)="loadApplications(0)" class="form-control status-select">
          <option [ngValue]="null">All Application Statuses</option>
          <option value="SUBMITTED">Submitted</option>
          <option value="UNDER_REVIEW">Under Review</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
        </select>
        <button class="btn btn-secondary" (click)="loadApplications(0)">Filter</button>
        <button class="btn btn-secondary" *ngIf="searchQuery || statusFilter" (click)="resetFilters()">Reset</button>
      </div>

      <!-- Applications Table -->
      <div class="table-container">
        <table class="enterprise-table">
          <thead>
            <tr>
              <th>Application #</th>
              <th>Customer</th>
              <th>Vehicle & Price</th>
              <th>Financing Terms</th>
              <th>Monthly EMI</th>
              <th>Risk Assessment</th>
              <th>Status</th>
              <th>Underwriter</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let app of applicationsPage?.content">
              <td>
                <strong>{{ app.applicationNumber }}</strong>
                <div class="sub-text">Filed {{ app.createdAt | date:'shortDate' }}</div>
              </td>
              <td>
                <div>{{ app.customerName }}</div>
                <div class="sub-text">\${{ app.monthlyIncome | number:'1.0-0' }}/mo income</div>
              </td>
              <td>
                <div class="veh-title">{{ app.vehicleName }}</div>
                <div class="sub-text">Price: \${{ app.vehiclePrice | number:'1.0-0' }} (Down: \${{ app.downPayment | number:'1.0-0' }})</div>
              </td>
              <td>
                <strong>\${{ app.loanAmount | number:'1.0-0' }} Loan</strong>
                <div class="sub-text">{{ app.loanTermMonths }} Mo &#64; {{ app.interestRate }}% APR</div>
              </td>
              <td>
                <span class="amount-highlight">\${{ app.calculatedEmi | number:'1.2-2' }}/mo</span>
              </td>
              <td>
                <span class="badge" [ngClass]="getCreditTierBadge(app.creditTier)">
                  {{ formatCreditTier(app.creditTier) }}
                </span>
                <div class="sub-text mt-1" [class.text-danger]="app.debtToIncomeRatio > 43">
                  DTI: {{ app.debtToIncomeRatio }}%
                </div>
              </td>
              <td>
                <span class="badge" [ngClass]="'badge-' + app.status.toLowerCase()">
                  {{ app.status }}
                </span>
              </td>
              <td>
                <span class="sub-text">{{ app.reviewerName || 'Unassigned' }}</span>
              </td>
              <td>
                <button
                  class="btn btn-secondary btn-sm"
                  *ngIf="authService.isFinanceOfficer() && (app.status === 'SUBMITTED' || app.status === 'UNDER_REVIEW')"
                  (click)="openReviewModal(app)"
                >
                  Underwrite
                </button>
              </td>
            </tr>
            <tr *ngIf="!applicationsPage || applicationsPage.content.length === 0">
              <td colspan="9" class="empty-state">No financing applications found matching criteria.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="pagination-bar" *ngIf="applicationsPage && applicationsPage.totalPages > 1">
        <span>Page {{ applicationsPage.number + 1 }} of {{ applicationsPage.totalPages }}</span>
        <div class="page-buttons">
          <button class="btn btn-secondary btn-sm" [disabled]="applicationsPage.first" (click)="loadApplications(applicationsPage.number - 1)">Previous</button>
          <button class="btn btn-secondary btn-sm" [disabled]="applicationsPage.last" (click)="loadApplications(applicationsPage.number + 1)">Next</button>
        </div>
      </div>

      <!-- New Application Modal -->
      <div class="modal-overlay" *ngIf="showSubmitModal">
        <div class="modal-content modal-lg">
          <div class="modal-header">
            <h3 class="modal-title">New Auto Financing / Lease Application</h3>
            <button class="modal-close" (click)="showSubmitModal = false">&times;</button>
          </div>
          <form (ngSubmit)="submitApplication()">
            <div class="modal-body">
              <div class="alert alert-danger" *ngIf="modalError">{{ modalError }}</div>

              <div class="form-group">
                <label class="form-label">Customer (Applicant) *</label>
                <select [(ngModel)]="newRequest.customerId" (change)="onCustomerSelected()" name="customerId" required class="form-control">
                  <option [ngValue]="null">-- Select Applicant --</option>
                  <option *ngFor="let cust of customersList" [ngValue]="cust.id">
                    {{ cust.firstName }} {{ cust.lastName }} (Score: {{ cust.creditScore }} • Income: \${{ cust.annualIncome | number:'1.0-0' }})
                  </option>
                </select>
              </div>

              <div class="form-group" *ngIf="customerVehicles.length > 0">
                <label class="form-label">Vehicle *</label>
                <select [(ngModel)]="newRequest.vehicleId" (change)="onVehicleSelected()" name="vehicleId" required class="form-control">
                  <option [ngValue]="null">-- Select Vehicle --</option>
                  <option *ngFor="let veh of customerVehicles" [ngValue]="veh.id">
                    {{ veh.modelYear }} {{ veh.make }} {{ veh.model }} (Price: \${{ veh.purchasePrice | number:'1.0-0' }})
                  </option>
                </select>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">Vehicle Price (\$) *</label>
                  <input type="number" min="500" [(ngModel)]="newRequest.vehiclePrice" name="vehiclePrice" required class="form-control" />
                </div>
                <div class="form-group">
                  <label class="form-label">Down Payment (\$) *</label>
                  <input type="number" min="0" [(ngModel)]="newRequest.downPayment" name="downPayment" required class="form-control" />
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">Loan Term *</label>
                <select [(ngModel)]="newRequest.loanTermMonths" name="loanTermMonths" required class="form-control">
                  <option [ngValue]="24">24 Months (2 Years)</option>
                  <option [ngValue]="36">36 Months (3 Years)</option>
                  <option [ngValue]="48">48 Months (4 Years)</option>
                  <option [ngValue]="60">60 Months (5 Years)</option>
                  <option [ngValue]="72">72 Months (6 Years)</option>
                  <option [ngValue]="84">84 Months (7 Years)</option>
                </select>
              </div>

              <div class="loan-preview-box" *ngIf="newRequest.vehiclePrice && newRequest.downPayment != null">
                <div class="preview-metric">
                  <span class="preview-label">Loan Principal</span>
                  <span class="preview-val">\${{ (newRequest.vehiclePrice - newRequest.downPayment) | number:'1.0-0' }}</span>
                </div>
              </div>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="showSubmitModal = false">Cancel</button>
              <button type="submit" class="btn btn-primary" [disabled]="isSubmitting">
                {{ isSubmitting ? 'Submitting...' : 'Submit Application' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- Underwriting Review Modal -->
      <div class="modal-overlay" *ngIf="showReviewModal">
        <div class="modal-content">
          <div class="modal-header">
            <div>
              <h3 class="modal-title">Underwrite Application: {{ targetApp?.applicationNumber }}</h3>
              <span class="sub-text">{{ targetApp?.customerName }} • Loan \${{ targetApp?.loanAmount | number:'1.0-0' }}</span>
            </div>
            <button class="modal-close" (click)="showReviewModal = false">&times;</button>
          </div>
          <form (ngSubmit)="submitReview()">
            <div class="modal-body">
              <div class="alert alert-danger" *ngIf="modalError">{{ modalError }}</div>

              <div class="credit-summary card mb-3">
                <div class="summary-metric">
                  <span class="summary-label">Credit Tier</span>
                  <span class="summary-val">{{ formatCreditTier(targetApp?.creditTier || '') }}</span>
                </div>
                <div class="summary-metric">
                  <span class="summary-label">Monthly Income</span>
                  <span class="summary-val">\${{ targetApp?.monthlyIncome | number:'1.0-0' }}</span>
                </div>
                <div class="summary-metric">
                  <span class="summary-label">DTI Ratio</span>
                  <span class="summary-val" [class.text-danger]="(targetApp?.debtToIncomeRatio || 0) > 43">
                    {{ targetApp?.debtToIncomeRatio }}%
                  </span>
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">Underwriting Decision *</label>
                <div class="decision-radios">
                  <label class="radio-label">
                    <input type="radio" [value]="true" [(ngModel)]="reviewData.approved" name="approved" />
                    <span class="text-success">Approve Loan</span>
                  </label>
                  <label class="radio-label">
                    <input type="radio" [value]="false" [(ngModel)]="reviewData.approved" name="approved" />
                    <span class="text-danger">Reject Application</span>
                  </label>
                </div>
              </div>

              <div class="form-group" *ngIf="reviewData.approved">
                <label class="form-label">Approved APR Interest Rate (%) *</label>
                <input type="number" step="0.05" [(ngModel)]="reviewData.customInterestRate" name="customInterestRate" class="form-control" />
              </div>

              <div class="form-group">
                <label class="form-label">Underwriting Notes</label>
                <textarea rows="3" [(ngModel)]="reviewData.officerNotes" name="officerNotes" class="form-control" placeholder="Stipulations, cosigner requirements, or rationale..."></textarea>
              </div>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="showReviewModal = false">Cancel</button>
              <button type="submit" class="btn btn-primary" [disabled]="isSubmitting">
                {{ isSubmitting ? 'Recording...' : 'Finalize Decision' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .financing-page { display: flex; flex-direction: column; gap: 1.5rem; }
    .page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; }
    .page-title { font-size: 1.65rem; font-weight: 800; color: #ffffff; letter-spacing: -0.02em; }
    .page-subtitle { font-size: 0.85rem; color: var(--text-secondary); margin-top: 0.25rem; }
    .filter-bar { display: flex; gap: 0.75rem; align-items: center; padding: 1rem 1.25rem; }
    .search-box { flex: 1; }
    .status-select { width: 210px; }
    .veh-title { font-weight: 600; color: #ffffff; }
    .sub-text { font-size: 0.725rem; color: var(--text-muted); }
    .mt-1 { margin-top: 0.25rem; }
    .text-danger { color: #f87171 !important; }
    .text-success { color: #34d399 !important; }
    .pagination-bar { display: flex; justify-content: space-between; align-items: center; color: var(--text-muted); font-size: 0.8rem; }
    .page-buttons { display: flex; gap: 0.5rem; }
    .empty-state { text-align: center; padding: 2rem !important; color: var(--text-muted); }
    .modal-lg { max-width: 650px; }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .decision-radios { display: flex; gap: 1.5rem; padding: 0.5rem 0; }
    .radio-label { display: flex; align-items: center; gap: 0.5rem; cursor: pointer; font-weight: 700; font-size: 0.9rem; }
    .loan-preview-box { background: #0c121e; border: 1px solid var(--border-subtle); padding: 0.75rem 1rem; border-radius: 6px; margin-top: 0.5rem; }
    .preview-val { font-size: 1.1rem; font-weight: 800; color: #38bdf8; margin-left: 0.5rem; }
    .credit-summary { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 0.5rem; background: #0c121e; padding: 0.75rem; margin-bottom: 1rem; }
    .summary-metric { display: flex; flex-direction: column; }
    .summary-label { font-size: 0.65rem; color: var(--text-muted); text-transform: uppercase; }
    .summary-val { font-size: 0.95rem; font-weight: 700; color: #ffffff; }
    .alert-danger { background: rgba(239, 68, 68, 0.15); border: 1px solid rgba(239, 68, 68, 0.3); color: #f87171; padding: 0.75rem; border-radius: 6px; margin-bottom: 1rem; font-size: 0.85rem; }
  `]
})
export class FinancingComponent implements OnInit {
  private financingService = inject(FinancingService);
  private customerService = inject(CustomerService);
  private vehicleService = inject(VehicleService);
  authService = inject(AuthService);

  applicationsPage: Page<FinancingApplication> | null = null;
  statusFilter: FinancingStatus | null = null;
  searchQuery = '';

  customersList: Customer[] = [];
  customerVehicles: Vehicle[] = [];

  showSubmitModal = false;
  showReviewModal = false;
  isSubmitting = false;
  modalError = '';

  newRequest: FinancingRequest = {
    customerId: 0,
    vehicleId: 0,
    vehiclePrice: 35000,
    downPayment: 5000,
    loanTermMonths: 60
  };

  targetApp: FinancingApplication | null = null;
  reviewData: FinancingReview = {
    approved: true,
    customInterestRate: 5.25,
    officerNotes: 'Approved based on solid income and DTI within risk tolerance.'
  };

  ngOnInit(): void {
    this.loadApplications(0);
    this.customerService.getCustomers(0, 100).subscribe(p => this.customersList = p.content);
  }

  loadApplications(page: number): void {
    this.financingService.getApplications(page, 10, this.statusFilter || undefined, this.searchQuery).subscribe({
      next: (data) => this.applicationsPage = data
    });
  }

  resetFilters(): void {
    this.searchQuery = '';
    this.statusFilter = null;
    this.loadApplications(0);
  }

  openSubmitModal(): void {
    this.newRequest = {
      customerId: 0,
      vehicleId: 0,
      vehiclePrice: 35000,
      downPayment: 5000,
      loanTermMonths: 60
    };
    this.customerVehicles = [];
    this.modalError = '';
    this.showSubmitModal = true;
  }

  onCustomerSelected(): void {
    if (this.newRequest.customerId) {
      this.customerService.getCustomerVehicles(this.newRequest.customerId).subscribe(v => {
        this.customerVehicles = v;
        if (v.length > 0) {
          this.newRequest.vehicleId = v[0].id!;
          this.onVehicleSelected();
        }
      });
    }
  }

  onVehicleSelected(): void {
    const veh = this.customerVehicles.find(v => v.id === this.newRequest.vehicleId);
    if (veh) {
      this.newRequest.vehiclePrice = veh.purchasePrice;
      this.newRequest.downPayment = Math.round(veh.purchasePrice * 0.15);
    }
  }

  submitApplication(): void {
    if (!this.newRequest.customerId || !this.newRequest.vehicleId) {
      this.modalError = 'Please select both an applicant and a vehicle.';
      return;
    }

    this.isSubmitting = true;
    this.modalError = '';

    this.financingService.submitApplication(this.newRequest).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.showSubmitModal = false;
        this.loadApplications(0);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.modalError = err?.error?.message || 'Failed to submit financing application.';
      }
    });
  }

  openReviewModal(app: FinancingApplication): void {
    this.targetApp = app;
    this.reviewData = {
      approved: true,
      customInterestRate: app.interestRate,
      officerNotes: 'Credit tier and debt service coverage verified.'
    };
    this.modalError = '';
    this.showReviewModal = true;
  }

  submitReview(): void {
    if (!this.targetApp?.id) return;

    this.isSubmitting = true;
    this.modalError = '';

    this.financingService.reviewApplication(this.targetApp.id, this.reviewData).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.showReviewModal = false;
        this.loadApplications(this.applicationsPage ? this.applicationsPage.number : 0);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.modalError = err?.error?.message || 'Failed to review application.';
      }
    });
  }

  getCreditTierBadge(tier: CreditTier): string {
    switch (tier) {
      case 'TIER_1_EXCELLENT': return 'badge-tier-1';
      case 'TIER_2_GOOD': return 'badge-tier-2';
      case 'TIER_3_FAIR': return 'badge-tier-3';
      default: return 'badge-high-risk';
    }
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
