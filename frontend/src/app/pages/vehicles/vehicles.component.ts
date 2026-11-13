import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VehicleService } from '../../core/services/vehicle.service';
import { CustomerService } from '../../core/services/customer.service';
import { Vehicle } from '../../core/models/vehicle.model';
import { Customer } from '../../core/models/customer.model';
import { Page } from '../../core/models/page.model';

@Component({
  selector: 'app-vehicles',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="vehicles-page">
      <div class="page-header">
        <div>
          <h1 class="page-title">Automotive Vehicle Fleet</h1>
          <p class="page-subtitle">Track VIN registries, mileage telematics, vehicle condition, and title ownership</p>
        </div>
        <div>
          <button class="btn btn-primary" (click)="openCreateModal()">
            <span>➕</span> Register Vehicle
          </button>
        </div>
      </div>

      <!-- Filter Bar -->
      <div class="filter-bar card">
        <div class="search-box">
          <input
            type="text"
            [(ngModel)]="searchQuery"
            (keyup.enter)="loadVehicles(0)"
            placeholder="Search by VIN, make, or model..."
            class="form-control"
          />
        </div>
        <button class="btn btn-secondary" (click)="loadVehicles(0)">Search</button>
        <button class="btn btn-secondary" *ngIf="searchQuery" (click)="clearSearch()">Reset</button>
      </div>

      <!-- Vehicle Table -->
      <div class="table-container">
        <table class="enterprise-table">
          <thead>
            <tr>
              <th>VIN</th>
              <th>Make & Model</th>
              <th>Year</th>
              <th>Odometer</th>
              <th>Purchase Price</th>
              <th>Condition</th>
              <th>Owner</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let v of vehiclesPage?.content">
              <td><span class="vin-badge">{{ v.vin }}</span></td>
              <td><strong>{{ v.make }} {{ v.model }}</strong></td>
              <td>{{ v.modelYear }}</td>
              <td>{{ v.mileage | number }} mi</td>
              <td>\${{ v.purchasePrice | number:'1.2-2' }}</td>
              <td>
                <span class="badge" [ngClass]="getConditionBadge(v.vehicleCondition)">
                  {{ v.vehicleCondition }}
                </span>
              </td>
              <td>
                <div class="owner-name">{{ v.customerName }}</div>
                <div class="sub-text">Customer #{{ v.customerId }}</div>
              </td>
              <td>
                <button class="btn btn-secondary btn-sm" (click)="openEditModal(v)">Edit</button>
              </td>
            </tr>
            <tr *ngIf="!vehiclesPage || vehiclesPage.content.length === 0">
              <td colspan="8" class="empty-state">No vehicles found matching search criteria.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="pagination-bar" *ngIf="vehiclesPage && vehiclesPage.totalPages > 1">
        <span>Page {{ vehiclesPage.number + 1 }} of {{ vehiclesPage.totalPages }} (Total: {{ vehiclesPage.totalElements }})</span>
        <div class="page-buttons">
          <button class="btn btn-secondary btn-sm" [disabled]="vehiclesPage.first" (click)="loadVehicles(vehiclesPage.number - 1)">Previous</button>
          <button class="btn btn-secondary btn-sm" [disabled]="vehiclesPage.last" (click)="loadVehicles(vehiclesPage.number + 1)">Next</button>
        </div>
      </div>

      <!-- Create / Edit Modal -->
      <div class="modal-overlay" *ngIf="showModal">
        <div class="modal-content">
          <div class="modal-header">
            <h3 class="modal-title">{{ isEdit ? 'Update Vehicle Record' : 'Register Vehicle to Customer' }}</h3>
            <button class="modal-close" (click)="closeModal()">&times;</button>
          </div>
          <form (ngSubmit)="saveVehicle()">
            <div class="modal-body">
              <div class="alert alert-danger" *ngIf="modalError">{{ modalError }}</div>

              <div class="form-group">
                <label class="form-label">Vehicle Owner (Customer) *</label>
                <select [(ngModel)]="currentVehicle.customerId" name="customerId" required class="form-control" [disabled]="isEdit">
                  <option [ngValue]="null">-- Select Registered Customer --</option>
                  <option *ngFor="let c of customerList" [ngValue]="c.id">
                    {{ c.firstName }} {{ c.lastName }} ({{ c.email }})
                  </option>
                </select>
              </div>

              <div class="form-group">
                <label class="form-label">VIN (17 Characters) *</label>
                <input
                  type="text"
                  maxlength="17"
                  [(ngModel)]="currentVehicle.vin"
                  name="vin"
                  required
                  class="form-control vin-input"
                  placeholder="e.g. 1FTFW1ED5NFA12345"
                />
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">Make *</label>
                  <input type="text" [(ngModel)]="currentVehicle.make" name="make" required class="form-control" placeholder="e.g. Ford, Tesla, Toyota" />
                </div>
                <div class="form-group">
                  <label class="form-label">Model *</label>
                  <input type="text" [(ngModel)]="currentVehicle.model" name="model" required class="form-control" placeholder="e.g. F-150, Model 3" />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">Model Year *</label>
                  <input type="number" min="1990" max="2030" [(ngModel)]="currentVehicle.modelYear" name="modelYear" required class="form-control" />
                </div>
                <div class="form-group">
                  <label class="form-label">Current Odometer Mileage (mi) *</label>
                  <input type="number" min="0" [(ngModel)]="currentVehicle.mileage" name="mileage" required class="form-control" />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">Purchase Price (\$) *</label>
                  <input type="number" min="100" [(ngModel)]="currentVehicle.purchasePrice" name="purchasePrice" required class="form-control" />
                </div>
                <div class="form-group">
                  <label class="form-label">Purchase Date *</label>
                  <input type="date" [(ngModel)]="currentVehicle.purchaseDate" name="purchaseDate" required class="form-control" />
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">Vehicle Condition *</label>
                <select [(ngModel)]="currentVehicle.vehicleCondition" name="vehicleCondition" class="form-control">
                  <option value="NEW">Brand New</option>
                  <option value="CERTIFIED_PRE_OWNED">Certified Pre-Owned (CPO)</option>
                  <option value="USED">Pre-Owned / Used</option>
                </select>
              </div>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="closeModal()">Cancel</button>
              <button type="submit" class="btn btn-primary" [disabled]="isSubmitting">
                {{ isSubmitting ? 'Saving...' : 'Save Vehicle' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .vehicles-page { display: flex; flex-direction: column; gap: 1.5rem; }
    .page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; }
    .page-title { font-size: 1.65rem; font-weight: 800; color: #ffffff; letter-spacing: -0.02em; }
    .page-subtitle { font-size: 0.85rem; color: var(--text-secondary); margin-top: 0.25rem; }
    .filter-bar { display: flex; gap: 0.75rem; align-items: center; padding: 1rem 1.25rem; }
    .search-box { flex: 1; }
    .owner-name { font-weight: 600; color: #ffffff; }
    .sub-text { font-size: 0.725rem; color: var(--text-muted); }
    .pagination-bar { display: flex; justify-content: space-between; align-items: center; color: var(--text-muted); font-size: 0.8rem; }
    .page-buttons { display: flex; gap: 0.5rem; }
    .empty-state { text-align: center; padding: 2rem !important; color: var(--text-muted); }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .vin-input { font-family: var(--font-mono); text-transform: uppercase; letter-spacing: 0.08em; }
    .alert-danger { background: rgba(239, 68, 68, 0.15); border: 1px solid rgba(239, 68, 68, 0.3); color: #f87171; padding: 0.75rem; border-radius: 6px; margin-bottom: 1rem; font-size: 0.85rem; }
  `]
})
export class VehiclesComponent implements OnInit {
  private vehicleService = inject(VehicleService);
  private customerService = inject(CustomerService);

  vehiclesPage: Page<Vehicle> | null = null;
  customerList: Customer[] = [];
  searchQuery = '';
  showModal = false;
  isEdit = false;
  isSubmitting = false;
  modalError = '';

  currentVehicle: Vehicle = this.createEmptyVehicle();

  ngOnInit(): void {
    this.loadVehicles(0);
    this.customerService.getCustomers(0, 100).subscribe(p => this.customerList = p.content);
  }

  loadVehicles(page: number): void {
    this.vehicleService.getVehicles(page, 10, this.searchQuery).subscribe({
      next: (data) => this.vehiclesPage = data
    });
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.loadVehicles(0);
  }

  openCreateModal(): void {
    this.isEdit = false;
    this.currentVehicle = this.createEmptyVehicle();
    this.modalError = '';
    this.showModal = true;
  }

  openEditModal(vehicle: Vehicle): void {
    this.isEdit = true;
    this.currentVehicle = { ...vehicle };
    this.modalError = '';
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.modalError = '';
  }

  saveVehicle(): void {
    if (!this.currentVehicle.vin || this.currentVehicle.vin.trim().length !== 17) {
      this.modalError = 'VIN must be exactly 17 characters in accordance with ISO 3779.';
      return;
    }

    this.isSubmitting = true;
    this.modalError = '';

    const req = this.isEdit && this.currentVehicle.id
      ? this.vehicleService.updateVehicle(this.currentVehicle.id, this.currentVehicle)
      : this.vehicleService.createVehicle(this.currentVehicle);

    req.subscribe({
      next: () => {
        this.isSubmitting = false;
        this.closeModal();
        this.loadVehicles(this.vehiclesPage ? this.vehiclesPage.number : 0);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.modalError = err?.error?.message || 'Failed to register vehicle.';
      }
    });
  }

  getConditionBadge(condition: string): string {
    switch (condition) {
      case 'NEW': return 'badge-active';
      case 'CERTIFIED_PRE_OWNED': return 'badge-tier-1';
      default: return 'badge-tier-3';
    }
  }

  private createEmptyVehicle(): Vehicle {
    return {
      vin: '',
      make: '',
      model: '',
      modelYear: 2023,
      mileage: 25000,
      purchasePrice: 35000,
      purchaseDate: new Date().toISOString().substring(0, 10),
      vehicleCondition: 'USED',
      status: 'ACTIVE',
      customerId: 1
    };
  }
}
