export type VehicleCondition = 'NEW' | 'USED' | 'CERTIFIED_PRE_OWNED';

export interface Vehicle {
  id?: number;
  vin: string;
  make: string;
  model: string;
  modelYear: number;
  mileage: number;
  purchasePrice: number;
  purchaseDate: string;
  vehicleCondition: VehicleCondition;
  status: string;
  customerId: number;
  customerName?: string;
}
