import { TestBed } from '@angular/core/testing';

import { SecPointService } from './sec-point.service';

describe('SecPointService', () => {
  let service: SecPointService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SecPointService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
