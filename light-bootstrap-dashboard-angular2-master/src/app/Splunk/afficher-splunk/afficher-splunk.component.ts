import { Component, OnInit } from '@angular/core';
import { SplunkService } from 'app/Services/splunk.service';
import { Splunk } from 'app/Model/Splunk';
import { Router } from '@angular/router';

@Component({
  selector: 'app-afficher-splunk',
  templateUrl: './afficher-splunk.component.html',
  styleUrls: ['./afficher-splunk.component.scss']
})
export class AfficherSplunkComponent implements OnInit {
 searchTerm: string = '';
  selectedSplunk: Splunk | null = null;
    splunks: Splunk[] = [];
    filteredSplunks: Splunk[] = [];
    unapprovedSplunks: Splunk[] = [];
  
     currentPage = 0;
      pageSize = 10;
      totalPages: number = 0;
      pagedSplunks:Splunk[] = [];
  
    constructor(private splunkService: SplunkService, private router: Router) {}
  
   ngOnInit(): void {
       this.getAllSplunks();
     }
   
     onSearch() {
       this.filteredSplunks = this.filterSplunks();
       this.calculatePagination();
       this.changePage(0);
     }
   
     getAllSplunks(): void {
       this.splunkService.getAllSplunks().subscribe(
         (data: Splunk[]) => {
           this.splunks = data;
           this.filteredSplunks = data;
           this.calculatePagination();
           this.changePage(0);
         },
         (error) => {
           console.error('Erreur rÃ©cupÃ©ration Splunks', error);
         }
       );
     }
   
     filterSplunks(): Splunk[] {
       const term = this.searchTerm.toLowerCase();
       return this.splunks.filter((splunk) => {
         const inLicences = splunk.licences?.some(lic =>
           lic.nomDesLicences?.toLowerCase().includes(term) ||
           lic.quantite?.toLowerCase().includes(term) ||
           (lic.dateEx && new Date(lic.dateEx).toLocaleDateString('fr-FR').includes(term))
         );
   
         const inMainFields =
           splunk.client?.toLowerCase().includes(term) ||
           splunk.nomDuContact?.toLowerCase().includes(term) ||
           splunk.adresseEmailContact?.toLowerCase().includes(term) ||
           splunk.numero?.toLowerCase().includes(term) ||
           splunk.remarques?.toLowerCase().includes(term) ||
           splunk.dureeLicence?.toLowerCase?.().includes(term);
   
         return inMainFields || inLicences;
       });
     }
   
     calculatePagination() {
       this.totalPages = Math.ceil(this.filteredSplunks.length / this.pageSize);
     }
   
     changePage(pageIndex: number) {
       this.currentPage = pageIndex;
       const start = this.currentPage * this.pageSize;
       const end = start + this.pageSize;
       this.pagedSplunks = this.filteredSplunks.slice(start, end);
     }
   
     approveSplunk(id: number): void {
       this.splunkService.activate(id).subscribe(() => {
     this.unapprovedSplunks = this.unapprovedSplunks.filter(splunk => splunk.splunkid !== id);
      this.filteredSplunks = this.filteredSplunks.filter(splunk => splunk.splunkid !== id);
      this.calculatePagination();
      this.changePage(this.currentPage);
      console.log('Article approuvÃ© et retirÃ© de la liste');
    });
  }
   
     deleteSplunk(id: number | undefined | null): void {
       if (id != null && confirm('Confirmer la suppression ?')) {
         this.splunkService.deleteSplunk(id).subscribe(
           () => {
             this.getAllSplunks();
             alert('Splunk supprimÃ© avec succÃ¨s');
           },
           error => {
             console.error('Erreur suppression Splunk', error);
             alert('Ã‰chec suppression');
           }
         );
       }
     }
   
     updateSplunk(splunk: Splunk): void {
       this.router.navigate(['/edit-splunk', splunk.splunkid]);
     }
   
     goToAddSplunk() {
       this.router.navigate(['/Ajoutersplunk']);
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

getFileDownloadUrl(id: number): string {
  return this.splunkService.getFileDownloadUrl(id);
}
   
  selectSplunk(x: Splunk): void { this.selectedSplunk = this.selectedSplunk?.splunkid === x.splunkid ? null : x; }
  closeDetail(): void { this.selectedSplunk = null; }
}
