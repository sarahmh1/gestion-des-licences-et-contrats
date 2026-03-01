import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AjouterSplunkComponent } from './ajouter-splunk.component';

describe('AjouterSplunkComponent', () => {
  let component: AjouterSplunkComponent;
  let fixture: ComponentFixture<AjouterSplunkComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AjouterSplunkComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AjouterSplunkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
