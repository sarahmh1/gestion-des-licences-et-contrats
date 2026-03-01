import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AfficherPaloComponent } from './afficher-palo.component';

describe('AfficherPaloComponent', () => {
  let component: AfficherPaloComponent;
  let fixture: ComponentFixture<AfficherPaloComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AfficherPaloComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AfficherPaloComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
