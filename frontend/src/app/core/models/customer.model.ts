export interface Customer {
  id?: number;
  firstName: string;
  lastName: string;
  fullName?: string;
  email: string;
  phone: string;
  streetAddress?: string;
  city?: string;
  stateProvince?: string;
  postalCode?: string;
  creditScore: number;
  annualIncome: number;
  employmentStatus: string;
  vehicleCount?: number;
}
