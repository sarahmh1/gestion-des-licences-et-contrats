import { TestBed } from '@angular/core/testing';

import { ProofpointService } from './proofpoint.service';

describe('ProofpointService', () => {
  let service: ProofpointService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProofpointService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
