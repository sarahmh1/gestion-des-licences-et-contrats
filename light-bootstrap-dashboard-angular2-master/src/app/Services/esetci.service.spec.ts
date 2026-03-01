import { TestBed } from '@angular/core/testing';

import { EsetciService } from './esetci.service';

describe('EsetciService', () => {
  let service: EsetciService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EsetciService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
