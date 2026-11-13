export type ClaimStatus = 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED' | 'PAID';

export interface Claim {
  id?: number;
  claimNumber?: string;
  contractId: number;
  contractNumber?: string;
  customerId?: number;
  customerName?: string;
  vehicleId?: number;
  vehicleVin?: string;
  vehicleName?: string;
  incidentDate: string;
  currentMileage: number;
  description: string;
  repairShopName: string;
  repairEstimate: number;
  approvedAmount?: number;
  deductiblePaid?: number;
  status: ClaimStatus;
  rejectionReason?: string;
  officerNotes?: string;
  reviewerName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ClaimRequest {
  contractId: number;
  incidentDate: string;
  currentMileage: number;
  description: string;
  repairShopName: string;
  repairEstimate: number;
}

export interface ClaimReview {
  approved: boolean;
  approvedAmount?: number;
  deductiblePaid?: number;
  officerNotes?: string;
  rejectionReason?: string;
}
