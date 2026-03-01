import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AfficherSplunkComponent } from './afficher-splunk.component';

describe('AfficherSplunkComponent', () => {
  let component: AfficherSplunkComponent;
  let fixture: ComponentFixture<AfficherSplunkComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AfficherSplunkComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AfficherSplunkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
