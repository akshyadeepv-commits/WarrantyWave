import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  FinancingApplication,
  FinancingRequest,
  FinancingReview,
  FinancingStatus,
  EmiCalculationRequest,
  EmiCalculationResponse
} from '../models/financing.model';
import { Page } from '../models/page.model';

@Injectable({
  providedIn: 'root'
})
export class FinancingService {
  private apiUrl = `${environment.apiUrl}/financing`;

  constructor(private http: HttpClient) {}

  calculateEmi(req: EmiCalculationRequest): Observable<EmiCalculationResponse> {
    return this.http.post<EmiCalculationResponse>(`${this.apiUrl}/calculate-emi`, req);
  }

  getApplications(page: number = 0, size: number = 10, status?: FinancingStatus, query?: string): Observable<Page<FinancingApplication>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (status) {
      params = params.set('status', status);
    }
    if (query && query.trim()) {
      params = params.set('query', query.trim());
    }
    return this.http.get<Page<FinancingApplication>>(this.apiUrl, { params });
  }

  getApplication(id: number): Observable<FinancingApplication> {
    return this.http.get<FinancingApplication>(`${this.apiUrl}/${id}`);
  }

  submitApplication(req: FinancingRequest): Observable<FinancingApplication> {
    return this.http.post<FinancingApplication>(this.apiUrl, req);
  }

  reviewApplication(id: number, review: FinancingReview): Observable<FinancingApplication> {
    return this.http.post<FinancingApplication>(`${this.apiUrl}/${id}/review`, review);
  }
}
