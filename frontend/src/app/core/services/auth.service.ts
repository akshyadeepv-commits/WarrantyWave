import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthRequest, AuthResponse, User, UserRole } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private readonly TOKEN_KEY = 'ww_jwt_token';
  private readonly USER_KEY = 'ww_user_profile';

  private currentUserSignal = signal<User | null>(this.getStoredUser());
  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => !!this.currentUserSignal());

  readonly isAdmin = computed(() => this.currentUserSignal()?.role === 'ROLE_ADMIN');
  readonly isClaimsOfficer = computed(() => 
    this.currentUserSignal()?.role === 'ROLE_CLAIMS_OFFICER' || this.currentUserSignal()?.role === 'ROLE_ADMIN'
  );
  readonly isFinanceOfficer = computed(() => 
    this.currentUserSignal()?.role === 'ROLE_FINANCE_OFFICER' || this.currentUserSignal()?.role === 'ROLE_ADMIN'
  );

  constructor(private http: HttpClient) {}

  login(credentials: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(res => {
        this.saveSession(res);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSignal.set(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  private saveSession(response: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, response.token);
    const user: User = {
      id: response.id,
      username: response.username,
      fullName: response.fullName,
      email: response.email,
      role: response.role,
      active: true
    };
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    this.currentUserSignal.set(user);
  }

  private getStoredUser(): User | null {
    try {
      const stored = localStorage.getItem(this.USER_KEY);
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  }
}
