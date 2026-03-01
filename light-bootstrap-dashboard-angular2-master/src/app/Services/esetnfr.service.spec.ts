import { TestBed } from '@angular/core/testing';

import { EsetnfrService } from './esetnfr.service';

describe('EsetnfrService', () => {
  let service: EsetnfrService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EsetnfrService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
