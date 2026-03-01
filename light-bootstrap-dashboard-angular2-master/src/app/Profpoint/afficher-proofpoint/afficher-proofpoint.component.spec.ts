import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AfficherProofpointComponent } from './afficher-proofpoint.component';

describe('AfficherProofpointComponent', () => {
  let component: AfficherProofpointComponent;
  let fixture: ComponentFixture<AfficherProofpointComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AfficherProofpointComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AfficherProofpointComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
