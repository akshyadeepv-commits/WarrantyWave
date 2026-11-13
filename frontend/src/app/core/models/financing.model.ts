export type FinancingStatus = 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED';
export type CreditTier = 'TIER_1_EXCELLENT' | 'TIER_2_GOOD' | 'TIER_3_FAIR' | 'HIGH_RISK';

export interface FinancingApplication {
  id?: number;
  applicationNumber?: string;
  customerId: number;
  customerName?: string;
  vehicleId: number;
  vehicleVin?: string;
  vehicleName?: string;
  vehiclePrice: number;
  downPayment: number;
  loanAmount: number;
  loanTermMonths: number;
  interestRate: number;
  calculatedEmi: number;
  status: FinancingStatus;
  monthlyIncome: number;
  debtToIncomeRatio: number;
  creditTier: CreditTier;
  officerNotes?: string;
  reviewerName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface FinancingRequest {
  customerId: number;
  vehicleId: number;
  vehiclePrice: number;
  downPayment: number;
  loanTermMonths: number;
}

export interface FinancingReview {
  approved: boolean;
  customInterestRate?: number;
  officerNotes?: string;
}

export interface EmiCalculationRequest {
  vehiclePrice: number;
  downPayment: number;
  loanTermMonths: number;
  creditScore?: number;
}

export interface EmiCalculationResponse {
  vehiclePrice: number;
  downPayment: number;
  loanAmount: number;
  loanTermMonths: number;
  estimatedInterestRate: number;
  monthlyEmi: number;
  totalPayment: number;
  totalInterest: number;
  creditTier: CreditTier;
}
