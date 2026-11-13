import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ClaimService } from './claim.service';
import { Claim, ClaimReview } from '../models/claim.model';
import { environment } from '../../../environments/environment';

describe('ClaimService', () => {
  let service: ClaimService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ClaimService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(ClaimService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should review and approve a claim with deductible deduction', () => {
    const reviewData: ClaimReview = {
      approved: true,
      officerNotes: 'Part failure covered under warranty.'
    };

    const mockApprovedClaim: Partial<Claim> = {
      id: 3,
      claimNumber: 'CLM-2024-103',
      repairEstimate: 1200,
      approvedAmount: 1100,
      deductiblePaid: 100,
      status: 'APPROVED'
    };

    service.reviewClaim(3, reviewData).subscribe(res => {
      expect(res.status).toBe('APPROVED');
      expect(res.approvedAmount).toBe(1100);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/claims/3/review`);
    expect(req.request.method).toBe('POST');
    req.flush(mockApprovedClaim);
  });
});
