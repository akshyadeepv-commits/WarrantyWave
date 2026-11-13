import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClaimService } from '../../core/services/claim.service';
import { WarrantyService } from '../../core/services/warranty.service';
import { AuthService } from '../../core/services/auth.service';
import { Claim, ClaimRequest, ClaimReview, ClaimStatus } from '../../core/models/claim.model';
import { WarrantyContract } from '../../core/models/warranty.model';
import { Page } from '../../core/models/page.model';

@Component({
  selector: 'app-claims',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="claims-page">
      <div class="page-header">
        <div>
          <h1 class="page-title">Warranty Claims Adjudication</h1>
          <p class="page-subtitle">Submit repair claims, review shop diagnostics, apply deductibles, and authorize payouts</p>
        </div>
        <div>
          <button class="btn btn-primary" (click)="openSubmitModal()">
            <span>➕</span> File Repair Claim
          </button>
        </div>
      </div>

      <!-- Filter Bar -->
      <div class="filter-bar card">
        <div class="search-box">
          <input
            type="text"
            [(ngModel)]="searchQuery"
            (keyup.enter)="loadClaims(0)"
            placeholder="Search by claim #, customer, or repair facility..."
            class="form-control"
          />
        </div>
        <select [(ngModel)]="statusFilter" (change)="loadClaims(0)" class="form-control status-select">
          <option [ngValue]="null">All Claim Statuses</option>
          <option value="SUBMITTED">Submitted</option>
          <option value="UNDER_REVIEW">Under Review</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
          <option value="PAID">Disbursed (Paid)</option>
        </select>
        <button class="btn btn-secondary" (click)="loadClaims(0)">Filter</button>
        <button class="btn btn-secondary" *ngIf="searchQuery || statusFilter" (click)="resetFilters()">Reset</button>
      </div>

      <!-- Claims Table -->
      <div class="table-container">
        <table class="enterprise-table">
          <thead>
            <tr>
              <th>Claim #</th>
              <th>Customer & Vehicle</th>
              <th>Incident Date</th>
              <th>Repair Facility</th>
              <th>Estimate</th>
              <th>Settlement Payout</th>
              <th>Status</th>
              <th>Adjudicator</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let c of claimsPage?.content">
              <td>
                <strong>{{ c.claimNumber }}</strong>
                <div class="sub-text">Contract: {{ c.contractNumber }}</div>
              </td>
              <td>
                <div>{{ c.customerName }}</div>
                <div class="sub-text">{{ c.vehicleName }} ({{ c.currentMileage | number }} mi)</div>
              </td>
              <td>{{ c.incidentDate }}</td>
              <td>
                <div>{{ c.repairShopName }}</div>
                <div class="sub-text clamp-text" [title]="c.description">{{ c.description }}</div>
              </td>
              <td>
                <strong>\${{ c.repairEstimate | number:'1.2-2' }}</strong>
              </td>
              <td>
                <div *ngIf="c.status === 'APPROVED' || c.status === 'PAID'">
                  <span class="amount-highlight">\${{ c.approvedAmount | number:'1.2-2' }}</span>
                  <div class="sub-text">Ded: \${{ c.deductiblePaid | number:'1.0-0' }}</div>
                </div>
                <div *ngIf="c.status === 'REJECTED'">
                  <span class="text-danger">\$0.00</span>
                  <div class="sub-text text-danger clamp-text" [title]="c.rejectionReason">{{ c.rejectionReason }}</div>
                </div>
                <span *ngIf="c.status === 'SUBMITTED' || c.status === 'UNDER_REVIEW'" class="sub-text">Pending Review</span>
              </td>
              <td>
                <span class="badge" [ngClass]="'badge-' + c.status.toLowerCase()">
                  {{ c.status }}
                </span>
              </td>
              <td>
                <span class="sub-text">{{ c.reviewerName || 'Unassigned' }}</span>
              </td>
              <td>
                <div class="action-btns">
                  <!-- Adjudicate button for Claims Officer / Admin -->
                  <button
                    class="btn btn-secondary btn-sm"
                    *ngIf="authService.isClaimsOfficer() && (c.status === 'SUBMITTED' || c.status === 'UNDER_REVIEW')"
                    (click)="openReviewModal(c)"
                  >
                    Adjudicate
                  </button>
                  <!-- Disburse button if approved -->
                  <button
                    class="btn btn-success btn-sm"
                    *ngIf="authService.isClaimsOfficer() && c.status === 'APPROVED'"
                    (click)="disbursePayment(c.id!)"
                  >
                    Disburse
                  </button>
                </div>
              </td>
            </tr>
            <tr *ngIf="!claimsPage || claimsPage.content.length === 0">
              <td colspan="9" class="empty-state">No claims found matching current criteria.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="pagination-bar" *ngIf="claimsPage && claimsPage.totalPages > 1">
        <span>Page {{ claimsPage.number + 1 }} of {{ claimsPage.totalPages }}</span>
        <div class="page-buttons">
          <button class="btn btn-secondary btn-sm" [disabled]="claimsPage.first" (click)="loadClaims(claimsPage.number - 1)">Previous</button>
          <button class="btn btn-secondary btn-sm" [disabled]="claimsPage.last" (click)="loadClaims(claimsPage.number + 1)">Next</button>
        </div>
      </div>

      <!-- Submit Claim Modal -->
      <div class="modal-overlay" *ngIf="showSubmitModal">
        <div class="modal-content modal-lg">
          <div class="modal-header">
            <h3 class="modal-title">File New Warranty Repair Claim</h3>
            <button class="modal-close" (click)="showSubmitModal = false">&times;</button>
          </div>
          <form (ngSubmit)="submitClaim()">
            <div class="modal-body">
              <div class="alert alert-danger" *ngIf="modalError">{{ modalError }}</div>

              <div class="form-group">
                <label class="form-label">Covered Warranty Contract *</label>
                <select [(ngModel)]="newClaim.contractId" name="contractId" required class="form-control">
                  <option [ngValue]="null">-- Select Active Contract --</option>
                  <option *ngFor="let cnt of activeContractsList" [ngValue]="cnt.id">
                    {{ cnt.contractNumber }} — {{ cnt.customerName }} ({{ cnt.vehicleName }} • Max: {{ cnt.maxMileage | number }} mi)
                  </option>
                </select>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">Incident Date *</label>
                  <input type="date" [(ngModel)]="newClaim.incidentDate" name="incidentDate" required class="form-control" />
                </div>
                <div class="form-group">
                  <label class="form-label">Odometer Mileage at Incident *</label>
                  <input type="number" min="0" [(ngModel)]="newClaim.currentMileage" name="currentMileage" required class="form-control" />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">Certified Repair Facility Name *</label>
                  <input type="text" [(ngModel)]="newClaim.repairShopName" name="repairShopName" required class="form-control" placeholder="e.g. MasterCare Automotive" />
                </div>
                <div class="form-group">
                  <label class="form-label">Total Repair Estimate (\$) *</label>
                  <input type="number" min="1" step="0.01" [(ngModel)]="newClaim.repairEstimate" name="repairEstimate" required class="form-control" />
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">Diagnostic Failure Breakdown & Description *</label>
                <textarea rows="3" [(ngModel)]="newClaim.description" name="description" required class="form-control" placeholder="Describe symptoms, OBD fault codes, or mechanical failure..."></textarea>
              </div>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="showSubmitModal = false">Cancel</button>
              <button type="submit" class="btn btn-primary" [disabled]="isSubmitting">
                {{ isSubmitting ? 'Submitting...' : 'Submit Claim' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- Adjudication Modal for Claims Officer -->
      <div class="modal-overlay" *ngIf="showReviewModal">
        <div class="modal-content">
          <div class="modal-header">
            <div>
              <h3 class="modal-title">Adjudicate Claim: {{ targetClaim?.claimNumber }}</h3>
              <span class="sub-text">Estimate: \${{ targetClaim?.repairEstimate | number:'1.2-2' }} • Facility: {{ targetClaim?.repairShopName }}</span>
            </div>
            <button class="modal-close" (click)="showReviewModal = false">&times;</button>
          </div>
          <form (ngSubmit)="submitReview()">
            <div class="modal-body">
              <div class="alert alert-danger" *ngIf="modalError">{{ modalError }}</div>

              <div class="form-group">
                <label class="form-label">Adjudication Decision *</label>
                <div class="decision-radios">
                  <label class="radio-label">
                    <input type="radio" [value]="true" [(ngModel)]="reviewData.approved" name="approved" />
                    <span class="text-success">Approve Payout</span>
                  </label>
                  <label class="radio-label">
                    <input type="radio" [value]="false" [(ngModel)]="reviewData.approved" name="approved" />
                    <span class="text-danger">Reject Claim</span>
                  </label>
                </div>
              </div>

              <!-- Approved Fields -->
              <div *ngIf="reviewData.approved">
                <div class="form-row">
                  <div class="form-group">
                    <label class="form-label">Approved Payout Amount (\$) *</label>
                    <input type="number" min="0" step="0.01" [(ngModel)]="reviewData.approvedAmount" name="approvedAmount" class="form-control" />
                  </div>
                  <div class="form-group">
                    <label class="form-label">Policy Deductible Collected (\$) *</label>
                    <input type="number" min="0" step="0.01" [(ngModel)]="reviewData.deductiblePaid" name="deductiblePaid" class="form-control" />
                  </div>
                </div>
              </div>

              <!-- Rejected Fields -->
              <div *ngIf="!reviewData.approved">
                <div class="form-group">
                  <label class="form-label">Reason for Policy Rejection *</label>
                  <input type="text" [(ngModel)]="reviewData.rejectionReason" name="rejectionReason" required class="form-control" placeholder="e.g. Excluded wearable friction material (brake pads)" />
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">Officer Audit Notes</label>
                <textarea rows="2" [(ngModel)]="reviewData.officerNotes" name="officerNotes" class="form-control" placeholder="Internal technical justification..."></textarea>
              </div>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="showReviewModal = false">Cancel</button>
              <button type="submit" class="btn btn-primary" [disabled]="isSubmitting">
                {{ isSubmitting ? 'Recording...' : 'Authorize Decision' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .claims-page { display: flex; flex-direction: column; gap: 1.5rem; }
    .page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; }
    .page-title { font-size: 1.65rem; font-weight: 800; color: #ffffff; letter-spacing: -0.02em; }
    .page-subtitle { font-size: 0.85rem; color: var(--text-secondary); margin-top: 0.25rem; }
    .filter-bar { display: flex; gap: 0.75rem; align-items: center; padding: 1rem 1.25rem; }
    .search-box { flex: 1; }
    .status-select { width: 190px; }
    .sub-text { font-size: 0.725rem; color: var(--text-muted); }
    .clamp-text { max-width: 220px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .action-btns { display: flex; gap: 0.5rem; }
    .pagination-bar { display: flex; justify-content: space-between; align-items: center; color: var(--text-muted); font-size: 0.8rem; }
    .page-buttons { display: flex; gap: 0.5rem; }
    .empty-state { text-align: center; padding: 2rem !important; color: var(--text-muted); }
    .modal-lg { max-width: 700px; }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .decision-radios { display: flex; gap: 1.5rem; padding: 0.5rem 0; }
    .radio-label { display: flex; align-items: center; gap: 0.5rem; cursor: pointer; font-weight: 700; font-size: 0.9rem; }
    .text-success { color: #34d399; }
    .text-danger { color: #f87171; }
    .alert-danger { background: rgba(239, 68, 68, 0.15); border: 1px solid rgba(239, 68, 68, 0.3); color: #f87171; padding: 0.75rem; border-radius: 6px; margin-bottom: 1rem; font-size: 0.85rem; }
  `]
})
export class ClaimsComponent implements OnInit {
  private claimService = inject(ClaimService);
  private warrantyService = inject(WarrantyService);
  authService = inject(AuthService);

  claimsPage: Page<Claim> | null = null;
  activeContractsList: WarrantyContract[] = [];
  statusFilter: ClaimStatus | null = null;
  searchQuery = '';

  showSubmitModal = false;
  showReviewModal = false;
  isSubmitting = false;
  modalError = '';

  newClaim: ClaimRequest = this.createEmptyClaimRequest();
  targetClaim: Claim | null = null;
  reviewData: ClaimReview = {
    approved: true,
    approvedAmount: 0,
    deductiblePaid: 100,
    officerNotes: '',
    rejectionReason: ''
  };

  ngOnInit(): void {
    this.loadClaims(0);
    this.warrantyService.getContracts(0, 100, 'ACTIVE').subscribe(p => this.activeContractsList = p.content);
  }

  loadClaims(page: number): void {
    this.claimService.getClaims(page, 10, this.statusFilter || undefined, this.searchQuery).subscribe({
      next: (data) => this.claimsPage = data
    });
  }

  resetFilters(): void {
    this.searchQuery = '';
    this.statusFilter = null;
    this.loadClaims(0);
  }

  openSubmitModal(): void {
    this.newClaim = this.createEmptyClaimRequest();
    this.modalError = '';
    this.showSubmitModal = true;
  }

  submitClaim(): void {
    if (!this.newClaim.contractId) {
      this.modalError = 'Please select an active covered warranty contract.';
      return;
    }

    this.isSubmitting = true;
    this.modalError = '';

    this.claimService.submitClaim(this.newClaim).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.showSubmitModal = false;
        this.loadClaims(0);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.modalError = err?.error?.message || 'Failed to submit repair claim.';
      }
    });
  }

  openReviewModal(claim: Claim): void {
    this.targetClaim = claim;
    const est = claim.repairEstimate || 0;
    const ded = 100;
    this.reviewData = {
      approved: true,
      approvedAmount: Math.max(0, est - ded),
      deductiblePaid: ded,
      officerNotes: 'Diagnostics and repair labor rates validated.',
      rejectionReason: ''
    };
    this.modalError = '';
    this.showReviewModal = true;
  }

  submitReview(): void {
    if (!this.targetClaim?.id) return;

    if (!this.reviewData.approved && !this.reviewData.rejectionReason?.trim()) {
      this.modalError = 'A formal rejection reason is required when denying policy coverage.';
      return;
    }

    this.isSubmitting = true;
    this.modalError = '';

    this.claimService.reviewClaim(this.targetClaim.id, this.reviewData).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.showReviewModal = false;
        this.loadClaims(this.claimsPage ? this.claimsPage.number : 0);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.modalError = err?.error?.message || 'Failed to review claim.';
      }
    });
  }

  disbursePayment(claimId: number): void {
    this.claimService.disburseClaim(claimId).subscribe({
      next: () => this.loadClaims(this.claimsPage ? this.claimsPage.number : 0)
    });
  }

  private createEmptyClaimRequest(): ClaimRequest {
    return {
      contractId: 0,
      incidentDate: new Date().toISOString().substring(0, 10),
      currentMileage: 30000,
      description: '',
      repairShopName: '',
      repairEstimate: 500
    };
  }
}
