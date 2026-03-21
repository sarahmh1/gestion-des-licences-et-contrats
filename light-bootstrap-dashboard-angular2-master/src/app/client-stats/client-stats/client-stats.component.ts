import { Component, OnInit } from '@angular/core';
import { ClientStatsService } from '../../Services/client-stats.service';
import { ClientService } from '../../Services/client.service';
import { ClientStats } from '../../Model/ClientStats';

@Component({
  selector: 'app-client-stats',
  templateUrl: './client-stats.component.html',
  styleUrls: ['./client-stats.component.scss']
})
export class ClientStatsComponent implements OnInit {
  clientStats: ClientStats[] = [];
  filteredStats: ClientStats[] = [];
  pagedStats: ClientStats[] = [];
  searchTerm: string = '';
  showAddModal = false;
  newClientName = '';

  currentPage = 0;
  pageSize = 4;
  totalPages = 0;

  constructor(private clientStatsService: ClientStatsService, private clientService: ClientService) { }

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.clientStatsService.getClientStats().subscribe(
      (data: ClientStats[]) => {
        this.clientStats = data.reverse();
        this.filteredStats = [...this.clientStats];
        this.calculatePagination();
        this.changePage(0);
      },
      (error) => {
        console.error('Erreur lors du chargement des statistiques', error);
      }
    );
  }

  onSearch(): void {
    if (!this.searchTerm || this.searchTerm.trim() === '') {
      this.filteredStats = [...this.clientStats];
    } else {
      const term = this.searchTerm.toLowerCase();
      this.filteredStats = this.clientStats.filter(s =>
        s.client.toLowerCase().includes(term) ||
        (s.nomProduit && s.nomProduit.toLowerCase().includes(term))
      );
    }
    this.calculatePagination();
    this.changePage(0);
  }

  calculatePagination(): void {
    this.totalPages = Math.ceil(this.filteredStats.length / this.pageSize);
  }

  changePage(pageIndex: number): void {
    this.currentPage = pageIndex;
    const start = this.currentPage * this.pageSize;
    const end = start + this.pageSize;
    this.pagedStats = this.filteredStats.slice(start, end);
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }

  openAddModal(): void {
    this.newClientName = '';
    this.showAddModal = true;
  }

  closeAddModal(): void {
    this.showAddModal = false;
  }

  saveClient(): void {
    const name = this.newClientName.trim();
    if (!name) return;
    this.clientService.addClient({ nomClient: name }).subscribe(() => {
      this.closeAddModal();
      this.loadStats();
    });
  }

  deleteEntry(stat: ClientStats): void {
    const produitLabel = stat.nomProduit ? ` / ${stat.nomProduit}` : '';
    if (!confirm(`Supprimer le suivi d'interventions pour "${stat.client}${produitLabel}" ?\n\nNote : Cela supprimera tous les contrats associés à ce client et ce produit.`)) {
      return;
    }
    this.clientStatsService.deleteClientStatEntry(stat.client, stat.nomProduit || '').subscribe({
      next: () => this.loadStats(),
      error: (err) => {
        console.error('Erreur lors de la suppression', err);
        alert('Erreur lors de la suppression.');
      }
    });
  }
}
