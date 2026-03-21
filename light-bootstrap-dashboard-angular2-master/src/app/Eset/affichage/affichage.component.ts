import { Component, OnInit } from '@angular/core';
import { EsetService } from 'app/Services/eset.service';
import { Eset } from 'app/Model/Eset';
import { Router } from '@angular/router';

@Component({
  selector: 'app-affichage',
  templateUrl: './affichage.component.html',
  styleUrls: ['./affichage.component.scss']
})
export class AffichageComponent implements OnInit {
  searchTerm: string = '';
  esets: Eset[] = [];
  unapprovedEsets: Eset[] = [];
  filteredEsets: Eset[] = [];
  selectedEset: Eset | null = null;

  currentPage = 0;
  pageSize = 10;
  totalPages: number = 0;
  pagedEsets: Eset[] = [];

  productNameMapping = {
    'eset_protect_entry': 'ESET PROTECT Entry',
    'eset_protect_entry_on_prem': 'ESET PROTECT Entry On-Prem',
    'eset_protect_advanced': 'ESET PROTECT Advanced',
    'eset_protect_advanced_on_prem': 'ESET PROTECT Advanced On-Prem',
    'eset_protect_essential': 'ESET PROTECT Essential',
    'eset_protect_essential_on_prem': 'ESET PROTECT Essential On-Prem',
    'eset_protect_essential_plus_on': 'ESET PROTECT Essential Plus On-Prem',
    'eset_protect_enterprise_on_prem': 'ESET PROTECT Enterprise On-Prem',




    'eset_home_security_essential' :'Eset Home Security Essential' ,
    'eset_protect_enterprise' : 'Eset Protect Enterprise',
    'eset_endpoint_encryption' : 'Eset Endpoint Encryption',
    'eset_endpoint_encryption_pro': 'Eset Endpoint Encryption Pro',
    'eset_mail_security': 'Eset Mail Security',
    'eset_smart_security_premium': 'Eset Smart Security Premium',
    'eset_secure_authentification': 'Eset Secure Authentification',
    'eset_internet_security': 'Eset Internet Security',
    'eset_server_security': 'Eset Server Security',
    'eset_protect_mail_plus': 'Eset Protect Mail Plus',
    'eset_protect_complete': 'Eset Protect Complete'
  };

  TypeAchatNameMapping = {
    'RENOUVELLEMENT': 'Renouvellemnt',
    'upgrade': 'Upgrade',
    'nouvel_licence': 'Nouvel Licence',
    'businessTrial': 'Business Trial',
    'Augmentation': 'Augmentation',
    'DownGrade': 'Down Grade',
    'licence_gratuit': 'Licence Gratuit',
  };

  constructor(private esetService: EsetService, private router: Router) { }

  ngOnInit(): void {
    this.getAllEsets();
  }

  onSearch() {
    this.filteredEsets = this.filterEsets();
    this.calculatePagination();
    this.changePage(0);
  }

  // Charger tous les ESETs mais filtrer seulement les non approuvés pour l'affichage
  getAllEsets(): void {
    this.esetService.getAllEsets().subscribe(
      (data: Eset[]) => {
        this.esets = data;
        // Filtrer seulement les ESETs non approuvés
        this.unapprovedEsets = data.filter(eset => !eset.approuve);
        this.filteredEsets = [...this.unapprovedEsets]; // Copie pour le filtrage
        this.calculatePagination();
        this.changePage(0);
        console.log('ESETs non approuvés chargés avec succès', this.unapprovedEsets);
      },
      (error) => {
        console.error('Error fetching ESETs', error);
      }
    );
  }

  filterEsets(): Eset[] {
    const term = this.searchTerm.toLowerCase();
    return this.unapprovedEsets.filter((eset) => {
      return (
        (eset.cle_de_Licence && eset.cle_de_Licence.toString().toLowerCase().includes(term)) ||
        (eset.client && eset.client.toLowerCase().includes(term)) ||
        (eset.dureeDeLicence && eset.dureeDeLicence.toLowerCase().includes(term)) ||
        (eset.identifiant && eset.identifiant.toString().toLowerCase().includes(term)) ||
        (eset.mailAdmin && eset.mailAdmin.toLowerCase().includes(term)) ||
        (eset.nmb_tlf && eset.nmb_tlf.toString().toLowerCase().includes(term)) ||
        (eset.nom_contact && eset.nom_contact.toLowerCase().includes(term)) ||
        (eset.nom_produit && eset.nom_produit.toLowerCase().includes(term)) ||
        (eset.typeAchat && eset.typeAchat.toLowerCase().includes(term)) ||
        (eset.nombre && eset.nombre.toString().toLowerCase().includes(term))
      );
    });
  }

  calculatePagination() {
    this.totalPages = Math.ceil(this.filteredEsets.length / this.pageSize);
  }

  changePage(pageIndex: number) {
    this.currentPage = pageIndex;
    const start = this.currentPage * this.pageSize;
    const end = start + this.pageSize;
    this.pagedEsets = this.filteredEsets.slice(start, end);
  }

  // Méthode d'approbation - retire l'ESET approuvé de la liste
  aprouve(id: number) {
    this.esetService.activate(id).subscribe(() => {
      // Retirer l'ESET approuvé de la liste des non approuvés
      this.unapprovedEsets = this.unapprovedEsets.filter(eset => eset.esetid !== id);
      this.filteredEsets = this.filteredEsets.filter(eset => eset.esetid !== id);
      this.calculatePagination();
      this.changePage(this.currentPage);
      console.log('Article approuvé et retiré de la liste');
    });
  }

  updateEset(eset: Eset): void {
    this.router.navigate(['/edit-eset', eset.esetid]);
  }

  selectEset(eset: Eset): void {
    this.selectedEset = this.selectedEset?.esetid === eset.esetid ? null : eset;
  }

  closeDetail(): void {
    this.selectedEset = null;
  }

  deleteEset(id: number | undefined | null): void {
    if (id != null && confirm("Are you sure you want to delete this ESET?")) {
      this.esetService.deleteEset(id).subscribe(
        () => {
          // Retirer l'ESET supprimé de la liste
          this.unapprovedEsets = this.unapprovedEsets.filter(eset => eset.esetid !== id);
          this.filteredEsets = this.filteredEsets.filter(eset => eset.esetid !== id);
          this.calculatePagination();
          this.changePage(this.currentPage);
          window.alert('Eset deleted successfully');
        },
        (error) => {
          console.error('Error deleting ESET', error);
          window.alert('Failed to delete ESET');
        }
      );
    }
  }

  getProductName(nom_produit: string): string {
    return this.productNameMapping[nom_produit] || nom_produit;
  }

  getTypeAchatName(TypeAchat: string): string {
    return this.TypeAchatNameMapping[TypeAchat] || TypeAchat;
  }

  goToProducts() {
    this.router.navigate(['/products']);
  }

  getCommandePasserParLabel(value: any): string {
    switch (value) {
      case 'GI_TN': return 'GI_TN';
      case 'GI_FR': return 'GI_FR';
      case 'GI_CI': return 'GI_CI';
      default: return value;
    }
  }

  getFileDownloadUrl(filename: string): string {
    return this.esetService.getFileDownloadUrl(filename);
  }

  getFileDownloadUrlById(esetId: number): string {
    return this.esetService.getFileDownloadUrlById(esetId);
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }
}