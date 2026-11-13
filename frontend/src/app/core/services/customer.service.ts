import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Customer } from '../models/customer.model';
import { Page } from '../models/page.model';
import { Vehicle } from '../models/vehicle.model';
import { WarrantyContract } from '../models/warranty.model';
import { Claim } from '../models/claim.model';
import { FinancingApplication } from '../models/financing.model';

@Injectable({
  providedIn: 'root'
})
export class CustomerService {
  private apiUrl = `${environment.apiUrl}/customers`;

  constructor(private http: HttpClient) {}

  getCustomers(page: number = 0, size: number = 10, query?: string): Observable<Page<Customer>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (query && query.trim()) {
      params = params.set('query', query.trim());
    }
    return this.http.get<Page<Customer>>(this.apiUrl, { params });
  }

  getCustomer(id: number): Observable<Customer> {
    return this.http.get<Customer>(`${this.apiUrl}/${id}`);
  }

  createCustomer(customer: Customer): Observable<Customer> {
    return this.http.post<Customer>(this.apiUrl, customer);
  }

  updateCustomer(id: number, customer: Customer): Observable<Customer> {
    return this.http.put<Customer>(`${this.apiUrl}/${id}`, customer);
  }

  deleteCustomer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getCustomerVehicles(id: number): Observable<Vehicle[]> {
    return this.http.get<Vehicle[]>(`${this.apiUrl}/${id}/vehicles`);
  }

  getCustomerContracts(id: number): Observable<WarrantyContract[]> {
    return this.http.get<WarrantyContract[]>(`${this.apiUrl}/${id}/contracts`);
  }

  getCustomerClaims(id: number): Observable<Claim[]> {
    return this.http.get<Claim[]>(`${this.apiUrl}/${id}/claims`);
  }

  getCustomerFinancing(id: number): Observable<FinancingApplication[]> {
    return this.http.get<FinancingApplication[]>(`${this.apiUrl}/${id}/financing`);
  }
}
