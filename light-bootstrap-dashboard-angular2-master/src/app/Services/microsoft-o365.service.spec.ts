import { TestBed } from '@angular/core/testing';

import { MicrosoftO365Service } from './microsoft-o365.service';

describe('MicrosoftO365Service', () => {
  let service: MicrosoftO365Service;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MicrosoftO365Service);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
