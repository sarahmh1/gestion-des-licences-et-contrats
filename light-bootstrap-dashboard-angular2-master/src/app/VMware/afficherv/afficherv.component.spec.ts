import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AffichervComponent } from './afficherv.component';

describe('AffichervComponent', () => {
  let component: AffichervComponent;
  let fixture: ComponentFixture<AffichervComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AffichervComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AffichervComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
