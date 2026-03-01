import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AfficherwComponent } from './afficherw.component';

describe('AfficherwComponent', () => {
  let component: AfficherwComponent;
  let fixture: ComponentFixture<AfficherwComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AfficherwComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AfficherwComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
