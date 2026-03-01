import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AffichermComponent } from './afficherm.component';

describe('AffichermComponent', () => {
  let component: AffichermComponent;
  let fixture: ComponentFixture<AffichermComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AffichermComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AffichermComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
