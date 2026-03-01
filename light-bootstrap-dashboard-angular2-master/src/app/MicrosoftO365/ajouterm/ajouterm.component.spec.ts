import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AjoutermComponent } from './ajouterm.component';

describe('AjoutermComponent', () => {
  let component: AjoutermComponent;
  let fixture: ComponentFixture<AjoutermComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AjoutermComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AjoutermComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
