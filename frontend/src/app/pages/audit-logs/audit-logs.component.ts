import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../core/services/dashboard.service';
import { AuditLog } from '../../core/models/audit.model';

@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="audit-page">
      <div class="page-header">
        <div>
          <h1 class="page-title">Enterprise Audit & Compliance Trail</h1>
          <p class="page-subtitle">Immutable chronological journal of approvals, adjudications, and policy mutations</p>
        </div>
        <div>
          <button class="btn btn-secondary" (click)="loadLogs()">
            <span>🔄</span> Refresh Trail
          </button>
        </div>
      </div>

      <div class="table-container">
        <table class="enterprise-table">
          <thead>
            <tr>
              <th>Timestamp</th>
              <th>Module / Entity</th>
              <th>Entity ID</th>
              <th>Action</th>
              <th>Operator</th>
              <th>Audit Narrative</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let log of logs">
              <td class="font-mono">{{ log.logTimestamp | date:'medium' }}</td>
              <td><strong>{{ log.entityName }}</strong></td>
              <td><span class="vin-badge">#{{ log.entityId }}</span></td>
              <td>
                <span class="badge" [ngClass]="getActionBadge(log.actionType)">
                  {{ log.actionType }}
                </span>
              </td>
              <td>
                <span class="operator-tag">{{ log.performedBy }}</span>
              </td>
              <td class="narrative-cell">{{ log.details }}</td>
            </tr>
            <tr *ngIf="logs.length === 0">
              <td colspan="6" class="empty-state">No audit logs recorded yet.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .audit-page { display: flex; flex-direction: column; gap: 1.5rem; }
    .page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; }
    .page-title { font-size: 1.65rem; font-weight: 800; color: #ffffff; letter-spacing: -0.02em; }
    .page-subtitle { font-size: 0.85rem; color: var(--text-secondary); margin-top: 0.25rem; }
    .font-mono { font-family: var(--font-mono); font-size: 0.775rem; color: var(--text-secondary); }
    .operator-tag { font-family: var(--font-mono); font-size: 0.8rem; color: #38bdf8; }
    .narrative-cell { font-size: 0.825rem; color: var(--text-main); max-width: 450px; }
    .empty-state { text-align: center; padding: 2rem !important; color: var(--text-muted); }
  `]
})
export class AuditLogsComponent implements OnInit {
  private dashboardService = inject(DashboardService);
  logs: AuditLog[] = [];

  ngOnInit(): void {
    this.loadLogs();
  }

  loadLogs(): void {
    this.dashboardService.getAuditLogs().subscribe({
      next: (data) => this.logs = data
    });
  }

  getActionBadge(action: string): string {
    switch (action) {
      case 'APPROVE':
      case 'PAYMENT_DISBURSED':
      case 'CREATE':
        return 'badge-active';
      case 'REJECT':
      case 'DELETE':
        return 'badge-rejected';
      case 'RENEW':
      case 'SUBMIT':
        return 'badge-tier-1';
      default:
        return 'badge-tier-3';
    }
  }
}
