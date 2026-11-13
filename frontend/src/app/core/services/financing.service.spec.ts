import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { FinancingService } from './financing.service';
import { EmiCalculationRequest, EmiCalculationResponse } from '../models/financing.model';
import { environment } from '../../../environments/environment';

describe('FinancingService', () => {
  let service: FinancingService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        FinancingService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(FinancingService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should call calculate-emi endpoint with payload and return response', () => {
    const request: EmiCalculationRequest = {
      vehiclePrice: 35000,
      downPayment: 5000,
      loanTermMonths: 60,
      creditScore: 760
    };

    const mockResponse: EmiCalculationResponse = {
      vehiclePrice: 35000,
      downPayment: 5000,
      loanAmount: 30000,
      loanTermMonths: 60,
      estimatedInterestRate: 5.25,
      monthlyEmi: 569.40,
      totalPayment: 34164.00,
      totalInterest: 4164.00,
      creditTier: 'TIER_1_EXCELLENT'
    };

    service.calculateEmi(request).subscribe(res => {
      expect(res.loanAmount).toBe(30000);
      expect(res.monthlyEmi).toBe(569.40);
      expect(res.creditTier).toBe('TIER_1_EXCELLENT');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/financing/calculate-emi`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });
});
