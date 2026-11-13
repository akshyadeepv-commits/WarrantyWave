import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WarrantyService } from '../../core/services/warranty.service';
import { AuthService } from '../../core/services/auth.service';
import { WarrantyPlan, PlanType } from '../../core/models/warranty.model';

@Component({
  selector: 'app-warranty-plans',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="plans-page">
      <div class="page-header">
        <div>
          <h1 class="page-title">Warranty Coverage Products</h1>
          <p class="page-subtitle">Configure enterprise protection packages, deductible tiers, and maximum claim caps</p>
        </div>
        <div *ngIf="authService.isAdmin()">
          <button class="btn btn-primary" (click)="openCreateModal()">
            <span>➕</span> Add Warranty Plan
          </button>
        </div>
      </div>

      <!-- Plan Cards Grid -->
      <div class="plans-grid">
        <div *ngFor="let plan of plans" class="card plan-card">
          <div class="plan-header">
            <span class="badge" [ngClass]="getPlanTypeBadge(plan.planType)">{{ plan.planType }}</span>
            <span class="plan-status" [class.active]="plan.active">{{ plan.active ? 'Available' : 'Archived' }}</span>
          </div>

          <h3 class="plan-name">{{ plan.planName }}</h3>
          <p class="plan-code">{{ plan.planCode }}</p>

          <p class="plan-desc">{{ plan.description }}</p>

          <div class="plan-specs">
            <div class="spec-item">
              <span class="spec-label">Duration</span>
              <span class="spec-val">{{ plan.durationMonths }} Months</span>
            </div>
            <div class="spec-item">
              <span class="spec-label">Mileage Cap</span>
              <span class="spec-val">{{ plan.maxMileageCoverage | number }} mi</span>
            </div>
            <div class="spec-item">
              <span class="spec-label">Deductible</span>
              <span class="spec-val">\${{ plan.deductibleAmount | number:'1.2-2' }}</span>
            </div>
            <div class="spec-item">
              <span class="spec-label">Max Claim Limit</span>
              <span class="spec-val amount-highlight">\${{ plan.maxClaimLimit | number:'1.0-0' }}</span>
            </div>
          </div>

          <div class="plan-pricing">
            <div class="price-box">
              <span class="price-currency">\$</span>
              <span class="price-amount">{{ plan.basePremium | number:'1.0-0' }}</span>
              <span class="price-period">/ {{ plan.durationMonths }}mo</span>
            </div>
            <div class="price-monthly">
              \${{ (plan.basePremium / plan.durationMonths) | number:'1.2-2' }}/month
            </div>
          </div>

          <div class="plan-actions" *ngIf="authService.isAdmin()">
            <button class="btn btn-secondary btn-sm" (click)="openEditModal(plan)">Edit Policy Terms</button>
          </div>
        </div>
      </div>

      <!-- Admin Add/Edit Modal -->
      <div class="modal-overlay" *ngIf="showModal">
        <div class="modal-content">
          <div class="modal-header">
            <h3 class="modal-title">{{ isEdit ? 'Edit Warranty Plan' : 'Define New Warranty Plan' }}</h3>
            <button class="modal-close" (click)="closeModal()">&times;</button>
          </div>
          <form (ngSubmit)="savePlan()">
            <div class="modal-body">
              <div class="alert alert-danger" *ngIf="modalError">{{ modalError }}</div>

              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">Plan Code *</label>
                  <input type="text" [(ngModel)]="currentPlan.planCode" name="planCode" required class="form-control" [disabled]="isEdit" />
                </div>
                <div class="form-group">
                  <label class="form-label">Plan Type *</label>
                  <select [(ngModel)]="currentPlan.planType" name="planType" class="form-control">
                    <option value="POWERTRAIN">Powertrain</option>
                    <option value="COMPREHENSIVE">Comprehensive</option>
                    <option value="BUMPER_TO_BUMPER">Bumper to Bumper</option>
                    <option value="EV_DRIVETRAIN">EV Drivetrain</option>
                  </select>
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">Plan Name *</label>
                <input type="text" [(ngModel)]="currentPlan.planName" name="planName" required class="form-control" />
              </div>

              <div class="form-group">
                <label class="form-label">Description *</label>
                <textarea rows="3" [(ngModel)]="currentPlan.description" name="description" required class="form-control"></textarea>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">Duration (Months) *</label>
                  <input type="number" min="6" max="120" [(ngModel)]="currentPlan.durationMonths" name="durationMonths" required class="form-control" />
                </div>
                <div class="form-group">
                  <label class="form-label">Max Mileage Coverage (mi) *</label>
                  <input type="number" min="1000" [(ngModel)]="currentPlan.maxMileageCoverage" name="maxMileageCoverage" required class="form-control" />
                </div>
              </div>

              <div class="form-row three-col">
                <div class="form-group">
                  <label class="form-label">Deductible (\$) *</label>
                  <input type="number" min="0" [(ngModel)]="currentPlan.deductibleAmount" name="deductibleAmount" required class="form-control" />
                </div>
                <div class="form-group">
                  <label class="form-label">Base Premium (\$) *</label>
                  <input type="number" min="10" [(ngModel)]="currentPlan.basePremium" name="basePremium" required class="form-control" />
                </div>
                <div class="form-group">
                  <label class="form-label">Max Claim Limit (\$) *</label>
                  <input type="number" min="500" [(ngModel)]="currentPlan.maxClaimLimit" name="maxClaimLimit" required class="form-control" />
                </div>
              </div>

              <div class="form-group checkbox-group">
                <label>
                  <input type="checkbox" [(ngModel)]="currentPlan.active" name="active" /> Active for new enrollments
                </label>
              </div>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="closeModal()">Cancel</button>
              <button type="submit" class="btn btn-primary" [disabled]="isSubmitting">
                {{ isSubmitting ? 'Saving...' : 'Save Plan' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .plans-page { display: flex; flex-direction: column; gap: 1.5rem; }
    .page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; }
    .page-title { font-size: 1.65rem; font-weight: 800; color: #ffffff; letter-spacing: -0.02em; }
    .page-subtitle { font-size: 0.85rem; color: var(--text-secondary); margin-top: 0.25rem; }
    .plans-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 1.5rem; }
    .plan-card { display: flex; flex-direction: column; justify-content: space-between; }
    .plan-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem; }
    .plan-status { font-size: 0.7rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase; }
    .plan-status.active { color: #34d399; }
    .plan-name { font-size: 1.25rem; font-weight: 700; color: #ffffff; margin-bottom: 0.2rem; }
    .plan-code { font-family: var(--font-mono); font-size: 0.75rem; color: #38bdf8; margin-bottom: 0.75rem; }
    .plan-desc { font-size: 0.825rem; color: var(--text-secondary); line-height: 1.4; margin-bottom: 1.25rem; min-height: 50px; }
    .plan-specs { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; background: #0e1422; padding: 0.75rem 1rem; border-radius: 0.5rem; border: 1px solid var(--border-subtle); margin-bottom: 1.25rem; }
    .spec-item { display: flex; flex-direction: column; }
    .spec-label { font-size: 0.675rem; color: var(--text-muted); text-transform: uppercase; font-weight: 600; }
    .spec-val { font-size: 0.875rem; font-weight: 700; color: #ffffff; }
    .plan-pricing { display: flex; justify-content: space-between; align-items: flex-end; padding-top: 0.5rem; border-top: 1px solid var(--border-subtle); margin-bottom: 1rem; }
    .price-box { display: flex; align-items: baseline; }
    .price-currency { font-size: 1.1rem; font-weight: 700; color: #38bdf8; }
    .price-amount { font-size: 1.75rem; font-weight: 800; color: #ffffff; }
    .price-period { font-size: 0.75rem; color: var(--text-muted); margin-left: 0.25rem; }
    .price-monthly { font-size: 0.8rem; font-weight: 600; color: #10b981; }
    .plan-actions { display: flex; justify-content: flex-end; }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .form-row.three-col { grid-template-columns: 1fr 1fr 1fr; }
    .checkbox-group label { display: flex; align-items: center; gap: 0.5rem; font-size: 0.85rem; color: var(--text-secondary); cursor: pointer; }
    .alert-danger { background: rgba(239, 68, 68, 0.15); border: 1px solid rgba(239, 68, 68, 0.3); color: #f87171; padding: 0.75rem; border-radius: 6px; margin-bottom: 1rem; font-size: 0.85rem; }
  `]
})
export class WarrantyPlansComponent implements OnInit {
  private warrantyService = inject(WarrantyService);
  authService = inject(AuthService);

  plans: WarrantyPlan[] = [];
  showModal = false;
  isEdit = false;
  isSubmitting = false;
  modalError = '';

  currentPlan: WarrantyPlan = this.createEmptyPlan();

  ngOnInit(): void {
    this.loadPlans();
  }

  loadPlans(): void {
    this.warrantyService.getPlans().subscribe(data => this.plans = data);
  }

  openCreateModal(): void {
    this.isEdit = false;
    this.currentPlan = this.createEmptyPlan();
    this.modalError = '';
    this.showModal = true;
  }

  openEditModal(plan: WarrantyPlan): void {
    this.isEdit = true;
    this.currentPlan = { ...plan };
    this.modalError = '';
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.modalError = '';
  }

  savePlan(): void {
    this.isSubmitting = true;
    this.modalError = '';

    const req = this.isEdit && this.currentPlan.id
      ? this.warrantyService.updatePlan(this.currentPlan.id, this.currentPlan)
      : this.warrantyService.createPlan(this.currentPlan);

    req.subscribe({
      next: () => {
        this.isSubmitting = false;
        this.closeModal();
        this.loadPlans();
      },
      error: (err) => {
        this.isSubmitting = false;
        this.modalError = err?.error?.message || 'Failed to save warranty plan.';
      }
    });
  }

  getPlanTypeBadge(type: PlanType): string {
    switch (type) {
      case 'BUMPER_TO_BUMPER': return 'badge-active';
      case 'EV_DRIVETRAIN': return 'badge-tier-1';
      case 'COMPREHENSIVE': return 'badge-tier-2';
      default: return 'badge-tier-3';
    }
  }

  private createEmptyPlan(): WarrantyPlan {
    return {
      planCode: '',
      planName: '',
      planType: 'COMPREHENSIVE',
      description: '',
      durationMonths: 36,
      maxMileageCoverage: 60000,
      deductibleAmount: 100,
      basePremium: 1199,
      maxClaimLimit: 15000,
      active: true
    };
  }
}
