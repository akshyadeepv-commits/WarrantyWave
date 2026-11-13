import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CustomerService } from '../../core/services/customer.service';
import { Customer } from '../../core/models/customer.model';
import { Page } from '../../core/models/page.model';
import { Vehicle } from '../../core/models/vehicle.model';
import { WarrantyContract } from '../../core/models/warranty.model';

@Component({
  selector: 'app-customers',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="customers-page">
      <div class="page-header">
        <div>
          <h1 class="page-title">Customer Portfolio Management</h1>
          <p class="page-subtitle">Track policyholders, credit tier scores, annual income, and registered vehicles</p>
        </div>
        <div>
          <button class="btn btn-primary" (click)="openCreateModal()">
            <span>➕</span> Register Customer
          </button>
        </div>
      </div>

      <!-- Search & Filters -->
      <div class="filter-bar card">
        <div class="search-box">
          <input
            type="text"
            [(ngModel)]="searchQuery"
            (keyup.enter)="loadCustomers(0)"
            placeholder="Search by name, email, or phone number..."
            class="form-control"
          />
        </div>
        <button class="btn btn-secondary" (click)="loadCustomers(0)">Search</button>
        <button class="btn btn-secondary" *ngIf="searchQuery" (click)="clearSearch()">Reset</button>
      </div>

      <!-- Customer Table -->
      <div class="table-container">
        <table class="enterprise-table">
          <thead>
            <tr>
              <th>Customer</th>
              <th>Contact Details</th>
              <th>Credit Score</th>
              <th>Annual Income</th>
              <th>Employment</th>
              <th>Vehicles</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let cust of customersPage?.content">
              <td>
                <div class="cust-name">{{ cust.firstName }} {{ cust.lastName }}</div>
                <div class="sub-text">{{ cust.streetAddress }}, {{ cust.city }}, {{ cust.stateProvince }}</div>
              </td>
              <td>
                <div>{{ cust.email }}</div>
                <div class="sub-text">{{ cust.phone }}</div>
              </td>
              <td>
                <span class="badge" [ngClass]="getCreditScoreBadge(cust.creditScore)">
                  {{ cust.creditScore }}
                </span>
              </td>
              <td>
                <strong>\${{ cust.annualIncome | number:'1.2-2' }}</strong>
                <div class="sub-text">\${{ (cust.annualIncome / 12) | number:'1.0-0' }}/mo</div>
              </td>
              <td>
                <span class="employment-tag">{{ cust.employmentStatus }}</span>
              </td>
              <td>
                <span class="badge badge-tier-2">{{ cust.vehicleCount || 0 }} Vehicles</span>
              </td>
              <td>
                <div class="action-btns">
                  <button class="btn btn-secondary btn-sm" (click)="viewCustomerDetails(cust)">Inspect</button>
                  <button class="btn btn-secondary btn-sm" (click)="openEditModal(cust)">Edit</button>
                </div>
              </td>
            </tr>
            <tr *ngIf="!customersPage || customersPage.content.length === 0">
              <td colspan="7" class="empty-state">No customers found matching search criteria.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="pagination-bar" *ngIf="customersPage && customersPage.totalPages > 1">
        <span>Page {{ customersPage.number + 1 }} of {{ customersPage.totalPages }} (Total: {{ customersPage.totalElements }})</span>
        <div class="page-buttons">
          <button class="btn btn-secondary btn-sm" [disabled]="customersPage.first" (click)="loadCustomers(customersPage.number - 1)">Previous</button>
          <button class="btn btn-secondary btn-sm" [disabled]="customersPage.last" (click)="loadCustomers(customersPage.number + 1)">Next</button>
        </div>
      </div>

      <!-- Create / Edit Modal -->
      <div class="modal-overlay" *ngIf="showModal">
        <div class="modal-content">
          <div class="modal-header">
            <h3 class="modal-title">{{ isEdit ? 'Update Customer Profile' : 'Register New Customer' }}</h3>
            <button class="modal-close" (click)="closeModal()">&times;</button>
          </div>
          <form (ngSubmit)="saveCustomer()">
            <div class="modal-body">
              <div class="alert alert-danger" *ngIf="modalError">{{ modalError }}</div>
              
              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">First Name *</label>
                  <input type="text" [(ngModel)]="currentCustomer.firstName" name="firstName" required class="form-control" />
                </div>
                <div class="form-group">
                  <label class="form-label">Last Name *</label>
                  <input type="text" [(ngModel)]="currentCustomer.lastName" name="lastName" required class="form-control" />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">Email *</label>
                  <input type="email" [(ngModel)]="currentCustomer.email" name="email" required class="form-control" />
                </div>
                <div class="form-group">
                  <label class="form-label">Phone *</label>
                  <input type="text" [(ngModel)]="currentCustomer.phone" name="phone" required class="form-control" />
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">Street Address</label>
                <input type="text" [(ngModel)]="currentCustomer.streetAddress" name="streetAddress" class="form-control" />
              </div>

              <div class="form-row three-col">
                <div class="form-group">
                  <label class="form-label">City</label>
                  <input type="text" [(ngModel)]="currentCustomer.city" name="city" class="form-control" />
                </div>
                <div class="form-group">
                  <label class="form-label">State / Province</label>
                  <input type="text" [(ngModel)]="currentCustomer.stateProvince" name="stateProvince" class="form-control" />
                </div>
                <div class="form-group">
                  <label class="form-label">Postal Code</label>
                  <input type="text" [(ngModel)]="currentCustomer.postalCode" name="postalCode" class="form-control" />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">Credit Score (300 - 850) *</label>
                  <input type="number" min="300" max="850" [(ngModel)]="currentCustomer.creditScore" name="creditScore" required class="form-control" />
                </div>
                <div class="form-group">
                  <label class="form-label">Annual Income (\$) *</label>
                  <input type="number" [(ngModel)]="currentCustomer.annualIncome" name="annualIncome" required class="form-control" />
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">Employment Status *</label>
                <select [(ngModel)]="currentCustomer.employmentStatus" name="employmentStatus" class="form-control">
                  <option value="EMPLOYED">Employed</option>
                  <option value="SELF_EMPLOYED">Self Employed</option>
                  <option value="RETIRED">Retired</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="closeModal()">Cancel</button>
              <button type="submit" class="btn btn-primary" [disabled]="isSubmitting">
                {{ isSubmitting ? 'Saving...' : 'Save Customer' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- Customer Detail Slide-Over / Inspect Modal -->
      <div class="modal-overlay" *ngIf="selectedCustomer">
        <div class="modal-content modal-lg">
          <div class="modal-header">
            <div>
              <h3 class="modal-title">{{ selectedCustomer.firstName }} {{ selectedCustomer.lastName }}</h3>
              <span class="sub-text">Customer ID #{{ selectedCustomer.id }} • Credit Score {{ selectedCustomer.creditScore }}</span>
            </div>
            <button class="modal-close" (click)="selectedCustomer = null">&times;</button>
          </div>
          <div class="modal-body">
            <h4 class="section-title">Covered Vehicles</h4>
            <div class="table-container mb-4">
              <table class="enterprise-table">
                <thead>
                  <tr>
                    <th>VIN</th>
                    <th>Make & Model</th>
                    <th>Year</th>
                    <th>Mileage</th>
                    <th>Condition</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let veh of customerVehicles">
                    <td><span class="vin-badge">{{ veh.vin }}</span></td>
                    <td><strong>{{ veh.make }} {{ veh.model }}</strong></td>
                    <td>{{ veh.modelYear }}</td>
                    <td>{{ veh.mileage | number }} mi</td>
                    <td><span class="badge badge-tier-2">{{ veh.vehicleCondition }}</span></td>
                  </tr>
                  <tr *ngIf="customerVehicles.length === 0">
                    <td colspan="5" class="empty-state">No vehicles currently registered.</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <h4 class="section-title">Active Warranty Contracts</h4>
            <div class="table-container">
              <table class="enterprise-table">
                <thead>
                  <tr>
                    <th>Contract #</th>
                    <th>Plan</th>
                    <th>Coverage Period</th>
                    <th>Max Mileage</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let cnt of customerContracts">
                    <td><strong>{{ cnt.contractNumber }}</strong></td>
                    <td>{{ cnt.planName }}</td>
                    <td>{{ cnt.startDate }} to {{ cnt.endDate }}</td>
                    <td>{{ cnt.maxMileage | number }} mi</td>
                    <td><span class="badge" [ngClass]="'badge-' + cnt.status.toLowerCase()">{{ cnt.status }}</span></td>
                  </tr>
                  <tr *ngIf="customerContracts.length === 0">
                    <td colspan="5" class="empty-state">No warranty contracts on record.</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" (click)="selectedCustomer = null">Close</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .customers-page { display: flex; flex-direction: column; gap: 1.5rem; }
    .page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; }
    .page-title { font-size: 1.65rem; font-weight: 800; color: #ffffff; letter-spacing: -0.02em; }
    .page-subtitle { font-size: 0.85rem; color: var(--text-secondary); margin-top: 0.25rem; }
    .filter-bar { display: flex; gap: 0.75rem; align-items: center; padding: 1rem 1.25rem; }
    .search-box { flex: 1; }
    .cust-name { font-weight: 700; color: #ffffff; }
    .sub-text { font-size: 0.725rem; color: var(--text-muted); }
    .employment-tag { font-size: 0.75rem; color: var(--text-secondary); text-transform: capitalize; }
    .action-btns { display: flex; gap: 0.5rem; }
    .pagination-bar { display: flex; justify-content: space-between; align-items: center; color: var(--text-muted); font-size: 0.8rem; }
    .page-buttons { display: flex; gap: 0.5rem; }
    .empty-state { text-align: center; padding: 2rem !important; color: var(--text-muted); }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .form-row.three-col { grid-template-columns: 1fr 1fr 1fr; }
    .modal-lg { max-width: 800px; }
    .section-title { font-size: 0.95rem; font-weight: 700; color: #38bdf8; margin: 1rem 0 0.5rem 0; text-transform: uppercase; letter-spacing: 0.04em; }
    .mb-4 { margin-bottom: 1.5rem; }
    .alert-danger { background: rgba(239, 68, 68, 0.15); border: 1px solid rgba(239, 68, 68, 0.3); color: #f87171; padding: 0.75rem; border-radius: 6px; margin-bottom: 1rem; font-size: 0.85rem; }
  `]
})
export class CustomersComponent implements OnInit {
  private customerService = inject(CustomerService);

  customersPage: Page<Customer> | null = null;
  searchQuery = '';
  showModal = false;
  isEdit = false;
  isSubmitting = false;
  modalError = '';

  currentCustomer: Customer = this.createEmptyCustomer();
  selectedCustomer: Customer | null = null;
  customerVehicles: Vehicle[] = [];
  customerContracts: WarrantyContract[] = [];

  ngOnInit(): void {
    this.loadCustomers(0);
  }

  loadCustomers(page: number): void {
    this.customerService.getCustomers(page, 10, this.searchQuery).subscribe({
      next: (data) => {
        this.customersPage = data;
      }
    });
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.loadCustomers(0);
  }

  openCreateModal(): void {
    this.isEdit = false;
    this.currentCustomer = this.createEmptyCustomer();
    this.modalError = '';
    this.showModal = true;
  }

  openEditModal(customer: Customer): void {
    this.isEdit = true;
    this.currentCustomer = { ...customer };
    this.modalError = '';
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.modalError = '';
  }

  saveCustomer(): void {
    this.isSubmitting = true;
    this.modalError = '';

    const req = this.isEdit && this.currentCustomer.id
      ? this.customerService.updateCustomer(this.currentCustomer.id, this.currentCustomer)
      : this.customerService.createCustomer(this.currentCustomer);

    req.subscribe({
      next: () => {
        this.isSubmitting = false;
        this.closeModal();
        this.loadCustomers(this.customersPage ? this.customersPage.number : 0);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.modalError = err?.error?.message || 'Failed to save customer record.';
      }
    });
  }

  viewCustomerDetails(customer: Customer): void {
    this.selectedCustomer = customer;
    if (customer.id) {
      this.customerService.getCustomerVehicles(customer.id).subscribe(v => this.customerVehicles = v);
      this.customerService.getCustomerContracts(customer.id).subscribe(c => this.customerContracts = c);
    }
  }

  getCreditScoreBadge(score: number): string {
    if (score >= 750) return 'badge-tier-1';
    if (score >= 670) return 'badge-tier-2';
    if (score >= 580) return 'badge-tier-3';
    return 'badge-high-risk';
  }

  private createEmptyCustomer(): Customer {
    return {
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      streetAddress: '',
      city: '',
      stateProvince: '',
      postalCode: '',
      creditScore: 720,
      annualIncome: 85000,
      employmentStatus: 'EMPLOYED'
    };
  }
}
