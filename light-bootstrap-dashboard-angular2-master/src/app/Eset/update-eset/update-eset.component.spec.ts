import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateEsetComponent } from './update-eset.component';

describe('UpdateEsetComponent', () => {
  let component: UpdateEsetComponent;
  let fixture: ComponentFixture<UpdateEsetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UpdateEsetComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UpdateEsetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
