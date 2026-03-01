import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AfficherRapid7Component } from './afficher-rapid7.component';

describe('AfficherRapid7Component', () => {
  let component: AfficherRapid7Component;
  let fixture: ComponentFixture<AfficherRapid7Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AfficherRapid7Component ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AfficherRapid7Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
