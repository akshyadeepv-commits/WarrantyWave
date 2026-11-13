import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Vehicle } from '../models/vehicle.model';
import { Page } from '../models/page.model';

@Injectable({
  providedIn: 'root'
})
export class VehicleService {
  private apiUrl = `${environment.apiUrl}/vehicles`;

  constructor(private http: HttpClient) {}

  getVehicles(page: number = 0, size: number = 10, query?: string): Observable<Page<Vehicle>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (query && query.trim()) {
      params = params.set('query', query.trim());
    }
    return this.http.get<Page<Vehicle>>(this.apiUrl, { params });
  }

  getVehicle(id: number): Observable<Vehicle> {
    return this.http.get<Vehicle>(`${this.apiUrl}/${id}`);
  }

  createVehicle(vehicle: Vehicle): Observable<Vehicle> {
    return this.http.post<Vehicle>(this.apiUrl, vehicle);
  }

  updateVehicle(id: number, vehicle: Vehicle): Observable<Vehicle> {
    return this.http.put<Vehicle>(`${this.apiUrl}/${id}`, vehicle);
  }

  deleteVehicle(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
