import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DashboardKpi } from '../models/dashboard.model';
import { AuditLog } from '../models/audit.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private kpisUrl = `${environment.apiUrl}/dashboard/kpis`;
  private auditUrl = `${environment.apiUrl}/audit-logs`;

  constructor(private http: HttpClient) {}

  getKpis(): Observable<DashboardKpi> {
    return this.http.get<DashboardKpi>(this.kpisUrl);
  }

  getAuditLogs(): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(this.auditUrl);
  }
}
