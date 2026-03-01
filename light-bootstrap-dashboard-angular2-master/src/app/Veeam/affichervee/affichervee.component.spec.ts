import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AfficherveeComponent } from './affichervee.component';

describe('AfficherveeComponent', () => {
  let component: AfficherveeComponent;
  let fixture: ComponentFixture<AfficherveeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AfficherveeComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AfficherveeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
