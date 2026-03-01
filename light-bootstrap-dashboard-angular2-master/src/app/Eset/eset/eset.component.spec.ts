import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EsetComponent } from './eset.component';

describe('EsetComponent', () => {
  let component: EsetComponent;
  let fixture: ComponentFixture<EsetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EsetComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EsetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
