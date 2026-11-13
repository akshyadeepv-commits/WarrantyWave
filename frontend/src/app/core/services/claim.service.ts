import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Claim, ClaimRequest, ClaimReview, ClaimStatus } from '../models/claim.model';
import { Page } from '../models/page.model';

@Injectable({
  providedIn: 'root'
})
export class ClaimService {
  private apiUrl = `${environment.apiUrl}/claims`;

  constructor(private http: HttpClient) {}

  getClaims(page: number = 0, size: number = 10, status?: ClaimStatus, query?: string): Observable<Page<Claim>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (status) {
      params = params.set('status', status);
    }
    if (query && query.trim()) {
      params = params.set('query', query.trim());
    }
    return this.http.get<Page<Claim>>(this.apiUrl, { params });
  }

  getClaim(id: number): Observable<Claim> {
    return this.http.get<Claim>(`${this.apiUrl}/${id}`);
  }

  submitClaim(claim: ClaimRequest): Observable<Claim> {
    return this.http.post<Claim>(this.apiUrl, claim);
  }

  reviewClaim(id: number, review: ClaimReview): Observable<Claim> {
    return this.http.post<Claim>(`${this.apiUrl}/${id}/review`, review);
  }

  disburseClaim(id: number): Observable<Claim> {
    return this.http.post<Claim>(`${this.apiUrl}/${id}/disburse`, null);
  }
}
