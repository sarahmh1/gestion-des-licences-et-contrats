import { TestBed } from '@angular/core/testing';

import { EsetfrService } from './esetfr.service';

describe('EsetfrService', () => {
  let service: EsetfrService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EsetfrService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
