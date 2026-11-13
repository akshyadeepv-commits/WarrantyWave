export type PlanType = 'POWERTRAIN' | 'COMPREHENSIVE' | 'BUMPER_TO_BUMPER' | 'EV_DRIVETRAIN';
export type ContractStatus = 'ACTIVE' | 'EXPIRED' | 'CANCELLED' | 'CLAIM_SUSPENDED';

export interface WarrantyPlan {
  id?: number;
  planCode: string;
  planName: string;
  planType: PlanType;
  description: string;
  durationMonths: number;
  maxMileageCoverage: number;
  deductibleAmount: number;
  basePremium: number;
  maxClaimLimit: number;
  active: boolean;
}

export interface WarrantyContract {
  id?: number;
  contractNumber?: string;
  customerId: number;
  customerName?: string;
  vehicleId: number;
  vehicleVin?: string;
  vehicleName?: string;
  planId: number;
  planName?: string;
  planType?: PlanType;
  startDate: string;
  endDate: string;
  startMileage: number;
  maxMileage: number;
  totalPremium: number;
  monthlyPremium: number;
  status: ContractStatus;
  renewalCount: number;
  notes?: string;
  createdAt?: string;
}

export interface EligibilityCheckRequest {
  vehicleId: number;
  planId: number;
  currentMileage?: number;
}

export interface EligibilityCheckResponse {
  eligible: boolean;
  message: string;
  vehicleMileage: number;
  maxMileageCoverage: number;
  vehicleAgeYears: number;
  maxAgeAllowedYears: number;
}

export interface ContractRenewal {
  additionalMonths: number;
  additionalMileage: number;
  updatedTotalPremium?: number;
  notes?: string;
}
