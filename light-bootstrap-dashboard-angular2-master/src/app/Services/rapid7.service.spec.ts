import { TestBed } from '@angular/core/testing';

import { Rapid7Service } from './rapid7.service';

describe('Rapid7Service', () => {
  let service: Rapid7Service;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Rapid7Service);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
