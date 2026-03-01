import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AjouterveeComponent } from './ajoutervee.component';

describe('AjouterveeComponent', () => {
  let component: AjouterveeComponent;
  let fixture: ComponentFixture<AjouterveeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AjouterveeComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AjouterveeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
