import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AfficherListeComponent } from './afficher-liste.component';

describe('AfficherListeComponent', () => {
  let component: AfficherListeComponent;
  let fixture: ComponentFixture<AfficherListeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AfficherListeComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AfficherListeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
