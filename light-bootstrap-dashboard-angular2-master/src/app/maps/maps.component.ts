import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-maps',
  templateUrl: './maps.component.html',
  styleUrls: ['./maps.component.css']
})

export class MapsComponent implements OnInit {
  [x: string]: any;
  licenses = [
    {
      clientIdentifiant: '123456',
      cleLicense: 'XYZ789',
      nomProduit: 'Produit A',
      nombreLicense: 5,
      typeAchat: 'Achat Unique',
      dateExpiration: '01/01/2024',
      nomContact: 'Jean Dupont',
      nomTelephone: '+33 1 23 45 67 89',
      adresseMailContact: 'jean.dupont@example.com',
      emailAdmin: 'admin@example.com'
    },
    // Add more objects as needed
  ];
  constructor() { }

  ngOnInit() { }
  onBack() {
    // Implement the logic for "Revenir" button (e.g., navigate to another route)
    this.router.navigate(['/previous-page']); // Navigate to the previous page (optional)
  }

  onAdd() {
    // Implement the logic for "Ajouter" button (e.g., add a new entry)
    alert('Ajouter button clicked! Implement add logic here.');
  }
}
