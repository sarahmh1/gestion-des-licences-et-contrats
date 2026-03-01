import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AjouterPaloComponent } from './ajouter-palo.component';

describe('AjouterPaloComponent', () => {
  let component: AjouterPaloComponent;
  let fixture: ComponentFixture<AjouterPaloComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AjouterPaloComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AjouterPaloComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
