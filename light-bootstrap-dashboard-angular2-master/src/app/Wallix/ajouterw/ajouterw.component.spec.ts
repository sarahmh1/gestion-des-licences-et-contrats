import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AjouterwComponent } from './ajouterw.component';

describe('AjouterwComponent', () => {
  let component: AjouterwComponent;
  let fixture: ComponentFixture<AjouterwComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AjouterwComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AjouterwComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
