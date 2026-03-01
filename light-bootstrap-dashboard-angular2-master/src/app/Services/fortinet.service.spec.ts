import { TestBed } from '@angular/core/testing';

import { FortinetService } from './fortinet.service';

describe('FortinetService', () => {
  let service: FortinetService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FortinetService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
