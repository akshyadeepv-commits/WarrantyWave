import { Claim } from './claim.model';
import { FinancingApplication } from './financing.model';

export interface PlanDistribution {
  planName: string;
  planType: string;
  activeContractsCount: number;
  percentage: number;
}

export interface DashboardKpi {
  totalCustomers: number;
  totalVehicles: number;
  activeContracts: number;
  totalFinancingVolume: number;
  totalApprovedClaimsAmount: number;
  pendingClaimsCount: number;
  pendingFinancingCount: number;
  monthlyRevenue: number;
  claimsLossRatio: number;
  recentClaims: Claim[];
  recentFinancing: FinancingApplication[];
  planDistribution: PlanDistribution[];
}
