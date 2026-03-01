import { Component, OnInit } from '@angular/core';
import { VeeamService } from 'app/Services/veeam.service';
import { Veeam } from 'app/Model/Veeam';
import { Router } from '@angular/router';

@Component({
  selector: 'app-affichervee',
  templateUrl: './affichervee.component.html',
  styleUrls: ['./affichervee.component.scss']
})
export class AfficherveeComponent implements OnInit {
 searchTerm: string = '';
  veeams: Veeam[] = [];
  filteredVeeams: Veeam[] = [];
  unapprovedVeeams : Veeam[] = [];

  currentPage = 0;
  pageSize = 10;
  totalPages: number = 0;
  pagedVeeams: Veeam[] = [];

  constructor(private veeamService: VeeamService, private router: Router) {}

  ngOnInit(): void {
    this.getAllVeeams();
  }

  onSearch() {
    this.filteredVeeams = this.filterVeeams();
    this.calculatePagination();
    this.changePage(0);
  }

  getAllVeeams(): void {
    this.veeamService.getAllVeeams().subscribe(
      (data: Veeam[]) => {
        this.veeams = data;
        this.filteredVeeams = data;
        this.calculatePagination();
        this.changePage(0);
      },
      (error) => {
        console.error('Erreur récupération Veeams', error);
      }
    );
  }

  filterVeeams(): Veeam[] {
    const term = this.searchTerm.toLowerCase();
    return this.veeams.filter((veeam) => {
      const inLicences = veeam.licences?.some(lic =>
        lic.nomDesLicences?.toLowerCase().includes(term) ||
        lic.quantite?.toLowerCase().includes(term) ||
        (lic.dateEx && new Date(lic.dateEx).toLocaleDateString('fr-FR').includes(term))
      );

      const inMainFields =
        veeam.client?.toLowerCase().includes(term) ||
        veeam.nomDuContact?.toLowerCase().includes(term) ||
        veeam.adresseEmailContact?.toLowerCase().includes(term) ||
        veeam.numero?.toLowerCase().includes(term) ||
        veeam.dureeDeLicence?.toLowerCase?.().includes(term);

      return inMainFields || inLicences;
    });
  }

  calculatePagination() {
    this.totalPages = Math.ceil(this.filteredVeeams.length / this.pageSize);
  }

  changePage(pageIndex: number) {
    this.currentPage = pageIndex;
    const start = this.currentPage * this.pageSize;
    const end = start + this.pageSize;
    this.pagedVeeams = this.filteredVeeams.slice(start, end);
  }

  approveVeeam(id: number): void {
    this.veeamService.activate(id).subscribe(() => {
      this.unapprovedVeeams = this.unapprovedVeeams.filter(veeam => veeam.veeamId !== id);
      this.filteredVeeams = this.filteredVeeams.filter(veeam => veeam.veeamId !== id);
      this.calculatePagination();
      this.changePage(this.currentPage);
      console.log('Article approuvé et retiré de la liste');
    });
  }

  deleteVeeam(id: number | undefined | null): void {
    if (id != null && confirm('Confirmer la suppression ?')) {
      this.veeamService.deleteVeeam(id).subscribe(
        () => {
          this.getAllVeeams();
          alert('Veeam supprimé avec succès');
        },
        error => {
          console.error('Erreur suppression Veeam', error);
          alert('Échec suppression');
        }
      );
    }
  }

  updateVeeam(veeam: Veeam): void {
    this.router.navigate(['/edit-veeam', veeam.veeamId]);
  }

  goToAddVeeam() {
    this.router.navigate(['/Ajouterveeam']);
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }
  getCommandePasserParLabel(value: any): string {
  switch (value) {
    case 'GI_TN': return 'GI_TN';
    case 'GI_FR': return 'GI_FR';
    case 'GI_CI': return 'GI_CI';
    default: return value;
  }
}

  getFileDownloadUrl(veeamId: number): string {
    return this.veeamService.getFileDownloadUrlById(veeamId);
  }
}
