import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  WarrantyPlan,
  WarrantyContract,
  ContractStatus,
  EligibilityCheckRequest,
  EligibilityCheckResponse,
  ContractRenewal
} from '../models/warranty.model';
import { Page } from '../models/page.model';

@Injectable({
  providedIn: 'root'
})
export class WarrantyService {
  private plansUrl = `${environment.apiUrl}/plans`;
  private contractsUrl = `${environment.apiUrl}/contracts`;

  constructor(private http: HttpClient) {}

  // Warranty Plans
  getPlans(): Observable<WarrantyPlan[]> {
    return this.http.get<WarrantyPlan[]>(this.plansUrl);
  }

  getActivePlans(): Observable<WarrantyPlan[]> {
    return this.http.get<WarrantyPlan[]>(`${this.plansUrl}/active`);
  }

  getPlan(id: number): Observable<WarrantyPlan> {
    return this.http.get<WarrantyPlan>(`${this.plansUrl}/${id}`);
  }

  createPlan(plan: WarrantyPlan): Observable<WarrantyPlan> {
    return this.http.post<WarrantyPlan>(this.plansUrl, plan);
  }

  updatePlan(id: number, plan: WarrantyPlan): Observable<WarrantyPlan> {
    return this.http.put<WarrantyPlan>(`${this.plansUrl}/${id}`, plan);
  }

  // Warranty Contracts
  getContracts(page: number = 0, size: number = 10, status?: ContractStatus, query?: string): Observable<Page<WarrantyContract>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (status) {
      params = params.set('status', status);
    }
    if (query && query.trim()) {
      params = params.set('query', query.trim());
    }
    return this.http.get<Page<WarrantyContract>>(this.contractsUrl, { params });
  }

  getContract(id: number): Observable<WarrantyContract> {
    return this.http.get<WarrantyContract>(`${this.contractsUrl}/${id}`);
  }

  checkEligibility(req: EligibilityCheckRequest): Observable<EligibilityCheckResponse> {
    return this.http.post<EligibilityCheckResponse>(`${this.contractsUrl}/check-eligibility`, req);
  }

  createContract(contract: WarrantyContract): Observable<WarrantyContract> {
    return this.http.post<WarrantyContract>(this.contractsUrl, contract);
  }

  renewContract(id: number, renewal: ContractRenewal): Observable<WarrantyContract> {
    return this.http.post<WarrantyContract>(`${this.contractsUrl}/${id}/renew`, renewal);
  }

  updateContractStatus(id: number, status: ContractStatus): Observable<WarrantyContract> {
    return this.http.patch<WarrantyContract>(`${this.contractsUrl}/${id}/status`, null, {
      params: { status }
    });
  }
}
