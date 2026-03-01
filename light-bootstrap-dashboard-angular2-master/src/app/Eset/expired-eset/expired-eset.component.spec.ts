import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExpiredEsetComponent } from './expired-eset.component';

describe('ExpiredEsetComponent', () => {
  let component: ExpiredEsetComponent;
  let fixture: ComponentFixture<ExpiredEsetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ExpiredEsetComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ExpiredEsetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
