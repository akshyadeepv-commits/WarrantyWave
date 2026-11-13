import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WarrantyService } from '../../core/services/warranty.service';
import { CustomerService } from '../../core/services/customer.service';
import { VehicleService } from '../../core/services/vehicle.service';
import {
  WarrantyContract,
  WarrantyPlan,
  ContractStatus,
  EligibilityCheckResponse,
  ContractRenewal
} from '../../core/models/warranty.model';
import { Customer } from '../../core/models/customer.model';
import { Vehicle } from '../../core/models/vehicle.model';
import { Page } from '../../core/models/page.model';

@Component({
  selector: 'app-contracts',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="contracts-page">
      <div class="page-header">
        <div>
          <h1 class="page-title">Warranty Contracts & Policy Servicing</h1>
          <p class="page-subtitle">Underwrite policies, validate telematics eligibility, and service extensions/renewals</p>
        </div>
        <div>
          <button class="btn btn-primary" (click)="openIssueModal()">
            <span>➕</span> Issue New Policy
          </button>
        </div>
      </div>

      <!-- Filters -->
      <div class="filter-bar card">
        <div class="search-box">
          <input
            type="text"
            [(ngModel)]="searchQuery"
            (keyup.enter)="loadContracts(0)"
            placeholder="Search by contract #, customer, or VIN..."
            class="form-control"
          />
        </div>
        <select [(ngModel)]="statusFilter" (change)="loadContracts(0)" class="form-control status-select">
          <option [ngValue]="null">All Statuses</option>
          <option value="ACTIVE">Active</option>
          <option value="EXPIRED">Expired</option>
          <option value="CANCELLED">Cancelled</option>
          <option value="CLAIM_SUSPENDED">Claim Suspended</option>
        </select>
        <button class="btn btn-secondary" (click)="loadContracts(0)">Filter</button>
        <button class="btn btn-secondary" *ngIf="searchQuery || statusFilter" (click)="resetFilters()">Reset</button>
      </div>

      <!-- Contracts Table -->
      <div class="table-container">
        <table class="enterprise-table">
          <thead>
            <tr>
              <th>Contract #</th>
              <th>Customer</th>
              <th>Vehicle & VIN</th>
              <th>Plan Coverage</th>
              <th>Term Period</th>
              <th>Mileage Limit</th>
              <th>Premium (Mo)</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let c of contractsPage?.content">
              <td>
                <strong>{{ c.contractNumber }}</strong>
                <div class="sub-text" *ngIf="c.renewalCount > 0">Renewed {{ c.renewalCount }}x</div>
              </td>
              <td>{{ c.customerName }}</td>
              <td>
                <div class="veh-title">{{ c.vehicleName }}</div>
                <div><span class="vin-badge">{{ c.vehicleVin }}</span></div>
              </td>
              <td>
                <span class="badge badge-tier-2">{{ c.planName }}</span>
              </td>
              <td>
                <div>{{ c.startDate }} to {{ c.endDate }}</div>
              </td>
              <td>
                <strong>{{ c.maxMileage | number }} mi</strong>
                <div class="sub-text">Started &#64; {{ c.startMileage | number }} mi</div>
              </td>
              <td>
                <span class="amount-highlight">\${{ c.monthlyPremium | number:'1.2-2' }}/mo</span>
                <div class="sub-text">Total: \${{ c.totalPremium | number:'1.0-0' }}</div>
              </td>
              <td>
                <span class="badge" [ngClass]="'badge-' + c.status.toLowerCase()">
                  {{ c.status }}
                </span>
              </td>
              <td>
                <div class="action-btns">
                  <button class="btn btn-secondary btn-sm" (click)="openRenewModal(c)">Renew</button>
                  <button
                    class="btn btn-danger btn-sm"
                    *ngIf="c.status === 'ACTIVE'"
                    (click)="updateStatus(c.id!, 'CANCELLED')"
                  >
                    Cancel
                  </button>
                </div>
              </td>
            </tr>
            <tr *ngIf="!contractsPage || contractsPage.content.length === 0">
              <td colspan="9" class="empty-state">No warranty contracts matching criteria.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="pagination-bar" *ngIf="contractsPage && contractsPage.totalPages > 1">
        <span>Page {{ contractsPage.number + 1 }} of {{ contractsPage.totalPages }}</span>
        <div class="page-buttons">
          <button class="btn btn-secondary btn-sm" [disabled]="contractsPage.first" (click)="loadContracts(contractsPage.number - 1)">Previous</button>
          <button class="btn btn-secondary btn-sm" [disabled]="contractsPage.last" (click)="loadContracts(contractsPage.number + 1)">Next</button>
        </div>
      </div>

      <!-- Issue Contract Modal with Live Eligibility Checker -->
      <div class="modal-overlay" *ngIf="showIssueModal">
        <div class="modal-content modal-lg">
          <div class="modal-header">
            <h3 class="modal-title">Underwrite & Issue Warranty Policy</h3>
            <button class="modal-close" (click)="closeIssueModal()">&times;</button>
          </div>
          <form (ngSubmit)="saveContract()">
            <div class="modal-body">
              <div class="alert alert-danger" *ngIf="modalError">{{ modalError }}</div>

              <!-- Customer Selector -->
              <div class="form-group">
                <label class="form-label">Customer *</label>
                <select [(ngModel)]="selectedCustomerId" (change)="onCustomerSelected()" name="selectedCustomerId" required class="form-control">
                  <option [ngValue]="null">-- Select Policyholder --</option>
                  <option *ngFor="let cust of customersList" [ngValue]="cust.id">
                    {{ cust.firstName }} {{ cust.lastName }} ({{ cust.email }})
                  </option>
                </select>
              </div>

              <!-- Vehicle Selector -->
              <div class="form-group" *ngIf="customerVehicles.length > 0">
                <label class="form-label">Customer Vehicle *</label>
                <select [(ngModel)]="newContract.vehicleId" (change)="onVehicleOrPlanChanged()" name="vehicleId" required class="form-control">
                  <option [ngValue]="null">-- Select Vehicle to Cover --</option>
                  <option *ngFor="let veh of customerVehicles" [ngValue]="veh.id">
                    {{ veh.modelYear }} {{ veh.make }} {{ veh.model }} (VIN: {{ veh.vin }} • {{ veh.mileage | number }} mi)
                  </option>
                </select>
              </div>

              <!-- Plan Selector -->
              <div class="form-group">
                <label class="form-label">Warranty Protection Plan *</label>
                <select [(ngModel)]="newContract.planId" (change)="onVehicleOrPlanChanged()" name="planId" required class="form-control">
                  <option [ngValue]="null">-- Select Protection Tier --</option>
                  <option *ngFor="let p of plansList" [ngValue]="p.id">
                    {{ p.planName }} ({{ p.durationMonths }}mo / {{ p.maxMileageCoverage | number }} mi • \${{ p.basePremium | number:'1.0-0' }})
                  </option>
                </select>
              </div>

              <!-- Live Eligibility Verification Panel -->
              <div class="eligibility-card" *ngIf="newContract.vehicleId && newContract.planId">
                <div class="eligibility-header">
                  <span class="eligibility-title">Underwriting Eligibility Engine</span>
                  <button type="button" class="btn btn-secondary btn-sm" (click)="runEligibilityCheck()">
                    Verify Eligibility
                  </button>
                </div>

                <div *ngIf="eligibilityResult" class="eligibility-result" [class.passed]="eligibilityResult.eligible" [class.failed]="!eligibilityResult.eligible">
                  <div class="result-icon">{{ eligibilityResult.eligible ? '✅' : '❌' }}</div>
                  <div class="result-details">
                    <strong>{{ eligibilityResult.eligible ? 'Vehicle Qualifies for Policy' : 'Eligibility Disqualified' }}</strong>
                    <p>{{ eligibilityResult.message }}</p>
                  </div>
                </div>
              </div>

              <!-- Notes -->
              <div class="form-group" style="margin-top: 1rem;">
                <label class="form-label">Policy Notes / Special Endorsements</label>
                <input type="text" [(ngModel)]="newContract.notes" name="notes" class="form-control" placeholder="e.g. Purchased alongside auto financing" />
              </div>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="closeIssueModal()">Cancel</button>
              <button type="submit" class="btn btn-primary" [disabled]="isSubmitting || (eligibilityResult && !eligibilityResult.eligible)">
                {{ isSubmitting ? 'Issuing Policy...' : 'Issue Contract' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- Renew Contract Modal -->
      <div class="modal-overlay" *ngIf="showRenewModal">
        <div class="modal-content">
          <div class="modal-header">
            <div>
              <h3 class="modal-title">Renew Contract: {{ targetContract?.contractNumber }}</h3>
              <span class="sub-text">Current Expiration: {{ targetContract?.endDate }}</span>
            </div>
            <button class="modal-close" (click)="showRenewModal = false">&times;</button>
          </div>
          <form (ngSubmit)="submitRenewal()">
            <div class="modal-body">
              <div class="alert alert-danger" *ngIf="modalError">{{ modalError }}</div>

              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">Extend Duration (Months) *</label>
                  <input type="number" min="6" max="60" [(ngModel)]="renewalData.additionalMonths" name="additionalMonths" required class="form-control" />
                </div>
                <div class="form-group">
                  <label class="form-label">Extend Mileage (Miles) *</label>
                  <input type="number" min="5000" step="1000" [(ngModel)]="renewalData.additionalMileage" name="additionalMileage" required class="form-control" />
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">Renewal Fee / Additional Premium (\$) *</label>
                <input type="number" min="0" [(ngModel)]="renewalData.updatedTotalPremium" name="updatedTotalPremium" required class="form-control" />
              </div>

              <div class="form-group">
                <label class="form-label">Renewal Justification / Notes</label>
                <input type="text" [(ngModel)]="renewalData.notes" name="renewalNotes" class="form-control" placeholder="e.g. Standard 12-month policy renewal" />
              </div>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="showRenewModal = false">Cancel</button>
              <button type="submit" class="btn btn-primary" [disabled]="isSubmitting">
                {{ isSubmitting ? 'Renewing...' : 'Confirm Renewal' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .contracts-page { display: flex; flex-direction: column; gap: 1.5rem; }
    .page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; }
    .page-title { font-size: 1.65rem; font-weight: 800; color: #ffffff; letter-spacing: -0.02em; }
    .page-subtitle { font-size: 0.85rem; color: var(--text-secondary); margin-top: 0.25rem; }
    .filter-bar { display: flex; gap: 0.75rem; align-items: center; padding: 1rem 1.25rem; }
    .search-box { flex: 1; }
    .status-select { width: 180px; }
    .veh-title { font-weight: 600; color: #ffffff; margin-bottom: 0.15rem; }
    .action-btns { display: flex; gap: 0.5rem; }
    .pagination-bar { display: flex; justify-content: space-between; align-items: center; color: var(--text-muted); font-size: 0.8rem; }
    .page-buttons { display: flex; gap: 0.5rem; }
    .empty-state { text-align: center; padding: 2rem !important; color: var(--text-muted); }
    .modal-lg { max-width: 700px; }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .eligibility-card {
      background: #0d1320;
      border: 1px solid var(--border-subtle);
      border-radius: 0.5rem;
      padding: 1rem;
      margin-top: 0.5rem;
    }
    .eligibility-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.75rem;
    }
    .eligibility-title {
      font-size: 0.75rem;
      font-weight: 700;
      color: #38bdf8;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    .eligibility-result {
      display: flex;
      gap: 0.75rem;
      padding: 0.75rem;
      border-radius: 0.5rem;
      font-size: 0.825rem;
    }
    .eligibility-result.passed {
      background: rgba(16, 185, 129, 0.1);
      border: 1px solid rgba(16, 185, 129, 0.3);
      color: #34d399;
    }
    .eligibility-result.failed {
      background: rgba(239, 68, 68, 0.1);
      border: 1px solid rgba(239, 68, 68, 0.3);
      color: #f87171;
    }
    .result-icon { font-size: 1.25rem; }
    .result-details p { margin-top: 0.2rem; font-size: 0.775rem; }
    .alert-danger { background: rgba(239, 68, 68, 0.15); border: 1px solid rgba(239, 68, 68, 0.3); color: #f87171; padding: 0.75rem; border-radius: 6px; margin-bottom: 1rem; font-size: 0.85rem; }
    .sub-text { font-size: 0.725rem; color: var(--text-muted); }
  `]
})
export class ContractsComponent implements OnInit {
  private warrantyService = inject(WarrantyService);
  private customerService = inject(CustomerService);
  private vehicleService = inject(VehicleService);

  contractsPage: Page<WarrantyContract> | null = null;
  statusFilter: ContractStatus | null = null;
  searchQuery = '';

  customersList: Customer[] = [];
  customerVehicles: Vehicle[] = [];
  plansList: WarrantyPlan[] = [];

  showIssueModal = false;
  showRenewModal = false;
  isSubmitting = false;
  modalError = '';

  selectedCustomerId: number | null = null;
  newContract: WarrantyContract = this.createEmptyContract();
  eligibilityResult: EligibilityCheckResponse | null = null;

  targetContract: WarrantyContract | null = null;
  renewalData: ContractRenewal = {
    additionalMonths: 12,
    additionalMileage: 15000,
    updatedTotalPremium: 499,
    notes: 'Standard 12-month extension'
  };

  ngOnInit(): void {
    this.loadContracts(0);
    this.customerService.getCustomers(0, 100).subscribe(p => this.customersList = p.content);
    this.warrantyService.getActivePlans().subscribe(p => this.plansList = p);
  }

  loadContracts(page: number): void {
    this.warrantyService.getContracts(page, 10, this.statusFilter || undefined, this.searchQuery).subscribe({
      next: (data) => this.contractsPage = data
    });
  }

  resetFilters(): void {
    this.searchQuery = '';
    this.statusFilter = null;
    this.loadContracts(0);
  }

  openIssueModal(): void {
    this.selectedCustomerId = null;
    this.customerVehicles = [];
    this.newContract = this.createEmptyContract();
    this.eligibilityResult = null;
    this.modalError = '';
    this.showIssueModal = true;
  }

  closeIssueModal(): void {
    this.showIssueModal = false;
    this.modalError = '';
  }

  onCustomerSelected(): void {
    if (this.selectedCustomerId) {
      this.customerService.getCustomerVehicles(this.selectedCustomerId).subscribe(v => {
        this.customerVehicles = v;
        if (v.length > 0) {
          this.newContract.vehicleId = v[0].id!;
          this.onVehicleOrPlanChanged();
        }
      });
      this.newContract.customerId = this.selectedCustomerId;
    } else {
      this.customerVehicles = [];
    }
  }

  onVehicleOrPlanChanged(): void {
    if (this.newContract.vehicleId && this.newContract.planId) {
      this.runEligibilityCheck();
    }
  }

  runEligibilityCheck(): void {
    if (!this.newContract.vehicleId || !this.newContract.planId) return;

    this.warrantyService.checkEligibility({
      vehicleId: this.newContract.vehicleId,
      planId: this.newContract.planId
    }).subscribe({
      next: (res) => this.eligibilityResult = res,
      error: () => this.modalError = 'Eligibility check could not be completed.'
    });
  }

  saveContract(): void {
    if (!this.newContract.customerId || !this.newContract.vehicleId || !this.newContract.planId) {
      this.modalError = 'Please select a customer, covered vehicle, and warranty plan.';
      return;
    }

    this.isSubmitting = true;
    this.modalError = '';

    this.warrantyService.createContract(this.newContract).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.closeIssueModal();
        this.loadContracts(0);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.modalError = err?.error?.message || 'Failed to issue warranty contract.';
      }
    });
  }

  openRenewModal(contract: WarrantyContract): void {
    this.targetContract = contract;
    this.renewalData = {
      additionalMonths: 12,
      additionalMileage: 15000,
      updatedTotalPremium: 499,
      notes: '12-Month policy extension'
    };
    this.modalError = '';
    this.showRenewModal = true;
  }

  submitRenewal(): void {
    if (!this.targetContract?.id) return;

    this.isSubmitting = true;
    this.modalError = '';

    this.warrantyService.renewContract(this.targetContract.id, this.renewalData).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.showRenewModal = false;
        this.loadContracts(this.contractsPage ? this.contractsPage.number : 0);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.modalError = err?.error?.message || 'Failed to renew contract.';
      }
    });
  }

  updateStatus(id: number, status: ContractStatus): void {
    this.warrantyService.updateContractStatus(id, status).subscribe({
      next: () => this.loadContracts(this.contractsPage ? this.contractsPage.number : 0)
    });
  }

  private createEmptyContract(): WarrantyContract {
    return {
      customerId: 0,
      vehicleId: 0,
      planId: 0,
      startDate: new Date().toISOString().substring(0, 10),
      endDate: '',
      startMileage: 0,
      maxMileage: 0,
      totalPremium: 0,
      monthlyPremium: 0,
      status: 'ACTIVE',
      renewalCount: 0
    };
  }
}
