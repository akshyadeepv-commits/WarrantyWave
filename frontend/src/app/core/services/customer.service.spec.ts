import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { CustomerService } from './customer.service';
import { Customer } from '../models/customer.model';
import { environment } from '../../../environments/environment';

describe('CustomerService', () => {
  let service: CustomerService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        CustomerService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(CustomerService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch customers with pagination and query', () => {
    const mockPage = {
      content: [{ id: 1, firstName: 'David', lastName: 'Miller', email: 'david@example.com' }],
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0,
      first: true,
      last: true
    };

    service.getCustomers(0, 10, 'David').subscribe(page => {
      expect(page.content.length).toBe(1);
      expect(page.content[0].firstName).toBe('David');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/customers?page=0&size=10&query=David`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
  });

  it('should create customer via POST', () => {
    const newCust: Partial<Customer> = {
      firstName: 'Emily',
      lastName: 'Blunt',
      email: 'emily@example.com',
      phone: '+1-555-8888',
      creditScore: 780,
      annualIncome: 95000,
      employmentStatus: 'EMPLOYED'
    };

    service.createCustomer(newCust as Customer).subscribe(created => {
      expect(created.id).toBe(12);
      expect(created.firstName).toBe('Emily');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/customers`);
    expect(req.request.method).toBe('POST');
    req.flush({ ...newCust, id: 12 });
  });
});
