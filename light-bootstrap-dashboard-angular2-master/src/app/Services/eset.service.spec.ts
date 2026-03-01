import { TestBed } from '@angular/core/testing';

import { EsetService } from './eset.service';

describe('EsetService', () => {
  let service: EsetService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EsetService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
