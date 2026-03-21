import { Component, OnInit } from '@angular/core';
import { ContratService } from 'app/Services/contrat.service';
import { Contrat } from 'app/Model/Contrat';

@Component({
  selector: 'app-historique-contrat',
  templateUrl: './historique-contrat.component.html',
  styleUrls: ['./historique-contrat.component.scss']
})
export class HistoriqueContratComponent implements OnInit {
  contrats: Contrat[] = [];
  filteredContrats: Contrat[] = [];
  pagedContrats: Contrat[] = [];
  searchTerm = '';
  selectedContrat: Contrat | null = null;

  currentPage = 0;
  pageSize = 10;
  totalPages = 0;

  loading = true;

  constructor(private contratService: ContratService) { }

  ngOnInit(): void {
    this.loadHistorique();
  }

  loadHistorique(): void {
    this.loading = true;
    this.contratService.getHistoriqueContrats().subscribe({
      next: (data) => {
        this.contrats = data;
        this.filteredContrats = [...data];
        this.calculatePagination();
        this.changePage(0);
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement historique', err);
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    const term = this.searchTerm.toLowerCase().trim();
    if (!term) {
      this.filteredContrats = [...this.contrats];
    } else {
      this.filteredContrats = this.contrats.filter(c =>
        (c.client && c.client.toLowerCase().includes(term)) ||
        (c.objetContrat && c.objetContrat.toLowerCase().includes(term)) ||
        (c.nomProduit && c.nomProduit.toLowerCase().includes(term))
      );
    }
    this.calculatePagination();
    this.changePage(0);
  }

  calculatePagination(): void {
    this.totalPages = Math.ceil(this.filteredContrats.length / this.pageSize);
  }

  changePage(page: number): void {
    this.currentPage = page;
    const start = page * this.pageSize;
    this.pagedContrats = this.filteredContrats.slice(start, start + this.pageSize);
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }

  openDetail(contrat: Contrat): void {
    this.selectedContrat = contrat;
  }

  closeDetail(): void {
    this.selectedContrat = null;
  }

  getExpiredSince(dateDebut: string): string {
    if (!dateDebut) return '';
    try {
      const debut = new Date(dateDebut);
      const expiry = new Date(debut);
      expiry.setMonth(expiry.getMonth() + 12);
      return expiry.toLocaleDateString('fr-FR');
    } catch {
      return '';
    }
  }
}
