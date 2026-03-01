import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-afficher-liste',
  templateUrl: './afficher-liste.component.html',
  styleUrls: ['./afficher-liste.component.scss']
})
export class AfficherListeComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }
  sidebarToggle() {
    // Implement the functionality to toggle the sidebar
    console.log('Sidebar toggled');
    // Add the logic to open/close the sidebar here
  }
}
