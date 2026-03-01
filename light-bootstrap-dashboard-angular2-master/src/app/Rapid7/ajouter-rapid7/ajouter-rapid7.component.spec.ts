import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AjouterRapid7Component } from './ajouter-rapid7.component';

describe('AjouterRapid7Component', () => {
  let component: AjouterRapid7Component;
  let fixture: ComponentFixture<AjouterRapid7Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AjouterRapid7Component ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AjouterRapid7Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
