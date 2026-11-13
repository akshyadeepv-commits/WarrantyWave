import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { WarrantyService } from './warranty.service';
import { WarrantyPlan, EligibilityCheckResponse } from '../models/warranty.model';
import { environment } from '../../../environments/environment';

describe('WarrantyService', () => {
  let service: WarrantyService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        WarrantyService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(WarrantyService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch active warranty plans catalog', () => {
    const mockPlans: Partial<WarrantyPlan>[] = [
      { id: 1, planCode: 'PW-01', planName: 'Powertrain Plus Shield', active: true }
    ];

    service.getActivePlans().subscribe(plans => {
      expect(plans.length).toBe(1);
      expect(plans[0].planCode).toBe('PW-01');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/plans/active`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPlans);
  });

  it('should verify vehicle eligibility via telematics pre-check', () => {
    const mockEligibility: EligibilityCheckResponse = {
      eligible: true,
      message: 'Vehicle passes all underwriting telematics criteria.',
      vehicleMileage: 24000,
      maxMileageCoverage: 75000,
      vehicleAgeYears: 2,
      maxAgeAllowedYears: 10
    };

    service.checkEligibility({ vehicleId: 1, planId: 2 }).subscribe(res => {
      expect(res.eligible).toBe(true);
      expect(res.vehicleAgeYears).toBe(2);
      expect(res.message).toContain('Vehicle passes');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/contracts/check-eligibility`);
    expect(req.request.method).toBe('POST');
    req.flush(mockEligibility);
  });
});
