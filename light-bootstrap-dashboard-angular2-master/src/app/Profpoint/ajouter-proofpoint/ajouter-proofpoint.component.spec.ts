import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AjouterProofpointComponent } from './ajouter-proofpoint.component';

describe('AjouterProofpointComponent', () => {
  let component: AjouterProofpointComponent;
  let fixture: ComponentFixture<AjouterProofpointComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AjouterProofpointComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AjouterProofpointComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
