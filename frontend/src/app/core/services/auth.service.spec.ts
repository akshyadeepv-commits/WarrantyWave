import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { AuthResponse } from '../models/user.model';
import { environment } from '../../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should authenticate user and store JWT token in localStorage', () => {
    const mockResponse: AuthResponse = {
      token: 'jwt-token-sample',
      type: 'Bearer',
      id: 1,
      username: 'admin',
      fullName: 'Alexander Wright',
      email: 'admin@warrantywave.com',
      role: 'ROLE_ADMIN'
    };

    service.login({ username: 'admin', password: 'Password@123' }).subscribe(res => {
      expect(res.token).toBe('jwt-token-sample');
      expect(service.isAuthenticated()).toBe(true);
      expect(service.isAdmin()).toBe(true);
      expect(service.getToken()).toBe('jwt-token-sample');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should clear token and user on logout', () => {
    localStorage.setItem('ww_jwt_token', 'token');
    service.logout();
    expect(service.getToken()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
  });
});
