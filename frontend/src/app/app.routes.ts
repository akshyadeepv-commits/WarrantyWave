import { Routes } from '@angular/router';
import { authGuard, adminGuard } from './core/guards/auth.guard';
import { LoginComponent } from './pages/login/login.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { CustomersComponent } from './pages/customers/customers.component';
import { VehiclesComponent } from './pages/vehicles/vehicles.component';
import { WarrantyPlansComponent } from './pages/warranty-plans/warranty-plans.component';
import { ContractsComponent } from './pages/contracts/contracts.component';
import { ClaimsComponent } from './pages/claims/claims.component';
import { FinancingComponent } from './pages/financing/financing.component';
import { CalculatorComponent } from './pages/calculator/calculator.component';
import { AuditLogsComponent } from './pages/audit-logs/audit-logs.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'customers', component: CustomersComponent, canActivate: [authGuard] },
  { path: 'vehicles', component: VehiclesComponent, canActivate: [authGuard] },
  { path: 'warranty-plans', component: WarrantyPlansComponent, canActivate: [authGuard] },
  { path: 'contracts', component: ContractsComponent, canActivate: [authGuard] },
  { path: 'claims', component: ClaimsComponent, canActivate: [authGuard] },
  { path: 'financing', component: FinancingComponent, canActivate: [authGuard] },
  { path: 'calculator', component: CalculatorComponent, canActivate: [authGuard] },
  { path: 'audit-logs', component: AuditLogsComponent, canActivate: [adminGuard] },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: '**', redirectTo: 'dashboard' }
];
