import { TestBed } from '@angular/core/testing';

import { PaloService } from './palo.service';

describe('PaloService', () => {
  let service: PaloService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PaloService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
