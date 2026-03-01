import { TestBed } from '@angular/core/testing';

import { VeeamService } from './veeam.service';

describe('VeeamService', () => {
  let service: VeeamService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(VeeamService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
