import { TestBed } from '@angular/core/testing';

import { VMwareService } from './vmware.service';

describe('VMwareService', () => {
  let service: VMwareService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(VMwareService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
