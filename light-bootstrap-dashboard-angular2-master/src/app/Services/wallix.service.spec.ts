import { TestBed } from '@angular/core/testing';

import { WallixService } from './wallix.service';

describe('WallixService', () => {
  let service: WallixService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WallixService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
