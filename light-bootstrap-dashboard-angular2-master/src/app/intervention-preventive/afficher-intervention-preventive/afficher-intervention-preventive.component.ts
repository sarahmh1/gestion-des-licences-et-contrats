import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { InterventionPreventiveService } from '../../Services/intervention-preventive.service';
import { InterventionPreventive, IntervenantPreventif, StatutInterventionPreventive } from '../../Model/InterventionPreventive';

@Component({
  selector: 'app-afficher-intervention-preventive',
  templateUrl: './afficher-intervention-preventive.component.html',
  styleUrls: ['./afficher-intervention-preventive.component.scss']
})
export class AfficherInterventionPreventiveComponent implements OnInit {

  interventions: InterventionPreventive[] = [];
  interventionForm: FormGroup;
  showModal = false;
  showDeleteModal = false;
  isEditMode = false;
  currentInterventionId: number | null = null;
  searchTerm: string = '';
  interventionToDelete: InterventionPreventive | null = null;

  // Variables pour gestion des fichiers
  selectedFile: File | null = null;
  existingFile: string | null = null;
  existingFileName: string | null = null;
  uploading: boolean = false;

  // Variables pour gestion des rôles
  currentUserRole: string = '';
  currentEditingStatut: StatutInterventionPreventive | null = null;
  StatutInterventionPreventive = StatutInterventionPreventive;

  // Variables pour utilisateurs assignés
  userSearchQuery: string = '';
  allUsers: any[] = [];
  filteredAssignableUsers: any[] = [];
  assignedUsers: any[] = [];

  // Variables pour sous-popup technique
  showTechSubPopup = false;
  techSubPopupLineIndex: number | null = null;
  techSubForm: FormGroup;
  techSelectedFile: File | null = null;

  constructor(
    private interventionPreventiveService: InterventionPreventiveService,
    private fb: FormBuilder,
    private http: HttpClient
  ) {
    this.initForm();
    this.techSubForm = this.fb.group({
      dateIntervention: [''],
      dateRapportPreventive: [''],
      techIntervenants: this.fb.array([this.fb.control('')])
    });
  }

  ngOnInit(): void {
    this.loadCurrentUserRole();
    this.loadInterventions();
    this.loadAllUsers();
  }

  loadCurrentUserRole(): void {
    const userData = localStorage.getItem('user');
    if (userData) {
      const user = JSON.parse(userData);
      this.currentUserRole = user.role || '';
      console.log('Current user role:', this.currentUserRole);
    }
  }

  // ==================== GESTION DES RÔLES ====================

  isAdmin(): boolean {
    return this.currentUserRole === 'ROLE_ADMINISTRATEUR';
  }

  isTechnique(): boolean {
    return this.currentUserRole === 'ROLE_TECHNIQUE';
  }

  canEditAdminFields(): boolean {
    // Le technique ne peut JAMAIS modifier les champs admin
    if (this.isTechnique()) return false;
    // Si statut TERMINE, personne ne peut modifier
    if (this.currentEditingStatut === StatutInterventionPreventive.TERMINE) return false;
    // Sinon, l'admin (ou tout autre rôle non-technique) peut modifier
    return true;
  }

  canEditTechFields(): boolean {
    // L'admin ne peut JAMAIS modifier les champs techniques
    if (this.isAdmin()) return false;
    // Si statut TERMINE, personne ne peut modifier
    if (this.currentEditingStatut === StatutInterventionPreventive.TERMINE) return false;
    // Le technique peut modifier ses champs si le statut est EN_ATTENTE_INTERVENTION
    if (this.isTechnique()) {
      return this.currentEditingStatut === StatutInterventionPreventive.EN_ATTENTE_INTERVENTION;
    }
    return false;
  }

  // Variables pour popup de détail
  selectedIntervention: InterventionPreventive | null = null;
  showDetailPopup = false;

  // ── Méthodes de groupement par statut ──────────────────────────────────────
  getEnAttenteInterventions(): InterventionPreventive[] {
    const filtered = this.searchTerm
      ? this.interventions.filter(i =>
        i.nomClient?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        i.emailCommercial?.toLowerCase().includes(this.searchTerm.toLowerCase()))
      : this.interventions;
    return filtered.filter(i =>
      i.statut === StatutInterventionPreventive.EN_ATTENTE_INTERVENTION ||
      i.statut === StatutInterventionPreventive.CREE ||
      !i.statut
    );
  }

  getTermineInterventions(): InterventionPreventive[] {
    const filtered = this.searchTerm
      ? this.interventions.filter(i =>
        i.nomClient?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        i.emailCommercial?.toLowerCase().includes(this.searchTerm.toLowerCase()))
      : this.interventions;
    return filtered.filter(i => i.statut === StatutInterventionPreventive.TERMINE);
  }

  getEnAttenteCount(): number { return this.getEnAttenteInterventions().length; }
  getTermineCount(): number { return this.getTermineInterventions().length; }

  // ── Popup de détail ────────────────────────────────────────────────────────
  openDetailPopup(intervention: InterventionPreventive): void {
    this.selectedIntervention = intervention;
    this.showDetailPopup = true;
  }

  closeDetailPopup(): void {
    this.showDetailPopup = false;
    this.selectedIntervention = null;
  }

  // Construit un tableau unifié de lignes de période depuis :
  //  - backend periodeLignes (nouveau format)
  //  - ou champs plats (ancien format)
  buildAllPeriodeLines(intervention: InterventionPreventive | null): any[] {
    if (!intervention) return [];

    // Priorité 1 : tableau periodeLignes du backend
    const backend = (intervention as any).periodeLignes;
    if (Array.isArray(backend) && backend.length > 0) {
      return backend;
    }

    // Priorité 2 : champs plats (ancien format) → construire une ligne
    const hasFlat = intervention.periodeDe || intervention.periodeA ||
      intervention.periodeRecommandeDe || intervention.periodeRecommandeA ||
      intervention.dateInterventionExigee;

    if (hasFlat) {
      return [{
        periodeDe: intervention.periodeDe,
        periodeA: intervention.periodeA,
        periodeRecommandeDe: intervention.periodeRecommandeDe,
        periodeRecommandeA: intervention.periodeRecommandeA,
        dateInterventionExigee: intervention.dateInterventionExigee,
        dateIntervention: intervention.dateIntervention || null,
        dateRapportPreventive: intervention.dateRapportPreventive || null,
      }];
    }

    return [];
  }

  // Ancien alias (gardé pour compatibilité si utilisé ailleurs)
  getPeriodeLignes(intervention: InterventionPreventive | null): any[] {
    return this.buildAllPeriodeLines(intervention);
  }


  getStatutLabel(statut: StatutInterventionPreventive | undefined): string {
    switch (statut) {
      case StatutInterventionPreventive.CREE:
        return 'Créé';
      case StatutInterventionPreventive.EN_ATTENTE_INTERVENTION:
        return 'En attente d\'intervention';
      case StatutInterventionPreventive.TERMINE:
        return 'Terminé';
      default:
        return '-';
    }
  }

  getStatutBadgeClass(statut: StatutInterventionPreventive | undefined): string {
    switch (statut) {
      case StatutInterventionPreventive.CREE:
        return 'badge-warning';
      case StatutInterventionPreventive.EN_ATTENTE_INTERVENTION:
        return 'badge-info';
      case StatutInterventionPreventive.TERMINE:
        return 'badge-success';
      default:
        return 'badge-secondary';
    }
  }

  initForm(): void {
    this.interventionForm = this.fb.group({
      nomClient: [''],
      nbInterventionsParAn: [''],
      periodeLines: this.fb.array([this.createPeriodeLine()]),
      dateIntervention: [''],
      dateRapportPreventive: [''],
      intervenants: this.fb.array([]),
      statut: [StatutInterventionPreventive.CREE],
      emailCommercial: [''],
      ccMail: this.fb.array([])
    });
  }

  createPeriodeLine(): FormGroup {
    return this.fb.group({
      periodeDe: [''],
      periodeA: [''],
      periodeRecommandeDe: [''],
      periodeRecommandeA: [''],
      dateInterventionExigee: ['']
    });
  }

  get periodeLinesArray(): FormArray {
    return this.interventionForm.get('periodeLines') as FormArray;
  }

  addPeriodeLine(): void {
    this.periodeLinesArray.push(this.createPeriodeLine());
  }

  removePeriodeLine(index: number): void {
    if (this.periodeLinesArray.length > 1) {
      this.periodeLinesArray.removeAt(index);
    }
  }

  get intervenants(): FormArray {
    return this.interventionForm.get('intervenants') as FormArray;
  }

  get ccMailArray(): FormArray {
    return this.interventionForm.get('ccMail') as FormArray;
  }

  addCcMail(): void {
    this.ccMailArray.push(this.fb.control(''));
  }

  removeCcMail(index: number): void {
    this.ccMailArray.removeAt(index);
  }

  createIntervenantControl(): FormGroup {
    return this.fb.group({
      nom: ['']
    });
  }

  addIntervenant(): void {
    this.intervenants.push(this.createIntervenantControl());
  }

  removeIntervenant(index: number): void {
    if (this.intervenants.length > 1) {
      this.intervenants.removeAt(index);
    }
  }

  // ── Utilisateurs assignés ───────────────────────────────────────────────────
  loadAllUsers(): void {
    const token = localStorage.getItem('token');
    const headers = token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : undefined;
    this.http.get<any[]>('http://localhost:8080/api/utilisateurs', { headers }).subscribe({
      next: (users) => { this.allUsers = users; },
      error: () => { this.allUsers = []; }
    });
  }

  searchAssignableUsers(): void {
    const q = this.userSearchQuery.toLowerCase().trim();
    if (!q) { this.filteredAssignableUsers = []; return; }
    this.filteredAssignableUsers = this.allUsers.filter(u =>
      (`${u.firstname} ${u.lastname}`).toLowerCase().includes(q) ||
      (u.email || '').toLowerCase().includes(q)
    ).filter(u => !this.assignedUsers.find(a => a.id === u.id));
  }

  assignUser(user: any): void {
    if (!this.assignedUsers.find(u => u.id === user.id)) {
      this.assignedUsers.push(user);
    }
    this.userSearchQuery = '';
    this.filteredAssignableUsers = [];
  }

  removeAssignedUser(index: number): void {
    this.assignedUsers.splice(index, 1);
  }

  // ── Sous-popup Technique ─────────────────────────────────────────────
  get techIntervenantsArray(): FormArray {
    return this.techSubForm.get('techIntervenants') as FormArray;
  }

  addTechIntervenant(): void {
    this.techIntervenantsArray.push(this.fb.control(''));
  }

  removeTechIntervenant(index: number): void {
    if (this.techIntervenantsArray.length > 1) {
      this.techIntervenantsArray.removeAt(index);
    }
  }

  onTechFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.techSelectedFile = input.files[0];
    }
  }

  clearTechFile(): void {
    this.techSelectedFile = null;
  }

  openTechSubPopup(lineIndex: number): void {
    this.techSubPopupLineIndex = lineIndex;
    // Pré-remplir depuis la ligne si elle a déjà des infos
    const line = this.periodeLinesArray.at(lineIndex);
    // Reset form
    this.techSubForm.patchValue({
      dateIntervention: line?.get('dateIntervention')?.value || '',
      dateRapportPreventive: line?.get('dateRapportPreventive')?.value || ''
    });
    // Reset intervenants
    while (this.techIntervenantsArray.length) { this.techIntervenantsArray.removeAt(0); }
    this.techIntervenantsArray.push(this.fb.control(''));
    this.techSelectedFile = null;
    this.showTechSubPopup = true;
  }

  closeTechSubPopup(): void {
    this.showTechSubPopup = false;
    this.techSubPopupLineIndex = null;
  }

  saveTechSubLine(): void {
    if (this.techSubPopupLineIndex === null) return;
    const line = this.periodeLinesArray.at(this.techSubPopupLineIndex) as FormGroup;
    const techValues = this.techSubForm.value;
    // Stocker les valeurs techniques dans la ligne de période
    if (!line.get('dateIntervention')) {
      line.addControl('dateIntervention', this.fb.control(techValues.dateIntervention || ''));
    } else {
      line.get('dateIntervention')!.setValue(techValues.dateIntervention || '');
    }
    if (!line.get('dateRapportPreventive')) {
      line.addControl('dateRapportPreventive', this.fb.control(techValues.dateRapportPreventive || ''));
    } else {
      line.get('dateRapportPreventive')!.setValue(techValues.dateRapportPreventive || '');
    }
    this.closeTechSubPopup();
    // Sauvegarder automatiquement
    this.saveIntervention();
  }

  loadInterventions(): void {
    this.interventionPreventiveService.getAllInterventionsPreventives().subscribe({
      next: (data) => {
        this.interventions = data;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des interventions préventives', err);
      }
    });
  }

  openAddModal(): void {
    this.isEditMode = false;
    this.currentInterventionId = null;
    this.currentEditingStatut = StatutInterventionPreventive.CREE;
    this.interventionForm.reset();
    this.interventionForm.patchValue({ statut: StatutInterventionPreventive.CREE });
    // Réinitialiser periodeLines
    while (this.periodeLinesArray.length) { this.periodeLinesArray.removeAt(0); }
    this.periodeLinesArray.push(this.createPeriodeLine());
    this.intervenants.clear();
    this.addIntervenant();
    // Vider et initialiser ccMail
    while (this.ccMailArray.length) { this.ccMailArray.removeAt(0); }
    this.addCcMail();
    // Réinitialiser utilisateurs assignés
    this.assignedUsers = [];
    this.userSearchQuery = '';
    this.filteredAssignableUsers = [];
    // Réinitialiser les variables de fichier
    this.selectedFile = null;
    this.existingFile = null;
    this.existingFileName = null;
    this.showModal = true;
  }

  openEditModal(intervention: InterventionPreventive): void {
    // Recharger le rôle à chaque ouverture pour s'assurer qu'il est à jour
    this.loadCurrentUserRole();

    this.isEditMode = true;
    this.currentInterventionId = intervention.interventionPreventiveId;
    this.currentEditingStatut = intervention.statut || StatutInterventionPreventive.CREE;

    console.log('Opening edit modal:');
    console.log('- Current user role:', this.currentUserRole);
    console.log('- Intervention statut:', intervention.statut);
    console.log('- currentEditingStatut:', this.currentEditingStatut);
    console.log('- canEditAdminFields:', this.canEditAdminFields());
    console.log('- canEditTechFields:', this.canEditTechFields());

    this.interventionForm.patchValue({
      nomClient: intervention.nomClient,
      nbInterventionsParAn: intervention.nbInterventionsParAn,
      dateIntervention: intervention.dateIntervention,
      dateRapportPreventive: intervention.dateRapportPreventive,
      statut: intervention.statut || StatutInterventionPreventive.CREE,
      emailCommercial: intervention.emailCommercial || ''
    });
    // Charger periodeLines depuis periodeLignes du backend (ou créer 1 ligne par défaut)
    while (this.periodeLinesArray.length) { this.periodeLinesArray.removeAt(0); }
    const lignes = (intervention as any).periodeLignes;
    if (lignes && lignes.length > 0) {
      lignes.forEach((l: any) => {
        this.periodeLinesArray.push(this.fb.group({
          periodeDe: [l.periodeDe || ''],
          periodeA: [l.periodeA || ''],
          periodeRecommandeDe: [l.periodeRecommandeDe || ''],
          periodeRecommandeA: [l.periodeRecommandeA || ''],
          dateInterventionExigee: [l.dateInterventionExigee || '']
        }));
      });
    } else {
      // Rétro-compat : lire les champs plats s'ils existent
      this.periodeLinesArray.push(this.fb.group({
        periodeDe: [intervention.periodeDe || ''],
        periodeA: [intervention.periodeA || ''],
        periodeRecommandeDe: [intervention.periodeRecommandeDe || ''],
        periodeRecommandeA: [intervention.periodeRecommandeA || ''],
        dateInterventionExigee: [intervention.dateInterventionExigee || '']
      }));
    }
    // Réinitialiser utilisateurs assignés
    this.assignedUsers = [];

    // Charger ccMail
    while (this.ccMailArray.length) {
      this.ccMailArray.removeAt(0);
    }
    if (intervention.ccMail && intervention.ccMail.length > 0) {
      intervention.ccMail.forEach(email => {
        this.ccMailArray.push(this.fb.control(email));
      });
    } else {
      this.addCcMail();
    }

    this.intervenants.clear();
    if (intervention.intervenants && intervention.intervenants.length > 0) {
      intervention.intervenants.forEach(intervenant => {
        this.intervenants.push(this.fb.group({
          nom: [intervenant.nom]
        }));
      });
    } else {
      this.addIntervenant();
    }

    // Charger info fichier existant
    this.selectedFile = null;
    this.existingFile = intervention.fichier || null;
    this.existingFileName = intervention.fichierOriginalName || null;
    this.showModal = true;
  }

  saveIntervention(): void {
    const formValue = this.interventionForm.value;

    // Lire les dates depuis la 1ère ligne du FormArray periodeLines
    const firstLine = this.periodeLinesArray.length > 0 ? this.periodeLinesArray.at(0).value : {};

    // Déterminer le nouveau statut basé sur le rôle et l'action
    let newStatut = formValue.statut || StatutInterventionPreventive.CREE;

    if (this.isAdmin() && !this.isEditMode) {
      // Admin crée une nouvelle intervention -> statut EN_ATTENTE_INTERVENTION
      newStatut = StatutInterventionPreventive.EN_ATTENTE_INTERVENTION;
    } else if (this.isTechnique() && this.currentEditingStatut === StatutInterventionPreventive.EN_ATTENTE_INTERVENTION) {
      // Technique complète l'intervention -> statut TERMINE
      newStatut = StatutInterventionPreventive.TERMINE;
    }

    const intervention: InterventionPreventive = {
      nomClient: formValue.nomClient,
      nbInterventionsParAn: formValue.nbInterventionsParAn,
      // Dates de période lues depuis la 1ère ligne du FormArray
      periodeDe: firstLine.periodeDe || null,
      periodeA: firstLine.periodeA || null,
      periodeRecommandeDe: firstLine.periodeRecommandeDe || null,
      periodeRecommandeA: firstLine.periodeRecommandeA || null,
      dateInterventionExigee: firstLine.dateInterventionExigee || null,
      // Dates techniques depuis le 1ère ligne si remplies, sinon depuis formValue
      dateIntervention: firstLine.dateIntervention || formValue.dateIntervention || null,
      dateRapportPreventive: firstLine.dateRapportPreventive || formValue.dateRapportPreventive || null,
      intervenants: formValue.intervenants ? formValue.intervenants.filter((i: IntervenantPreventif) => i.nom && i.nom.trim() !== '') : [],
      statut: newStatut,
      emailCommercial: formValue.emailCommercial,
      ccMail: formValue.ccMail ? formValue.ccMail.filter((email: string) => email && email.trim() !== '') : []
    };

    if (this.isEditMode && this.currentInterventionId) {
      this.interventionPreventiveService.updateInterventionPreventive(this.currentInterventionId, intervention).subscribe({
        next: () => {
          // Upload le fichier si sélectionné
          if (this.selectedFile) {
            this.uploadFileAfterSave(this.currentInterventionId!);
          } else {
            this.loadInterventions();
            this.closeModal();
          }
        },
        error: (err) => {
          console.error('Erreur lors de la mise à jour', err);
        }
      });
    } else {
      this.interventionPreventiveService.addInterventionPreventive(intervention).subscribe({
        next: (newIntervention: InterventionPreventive) => {
          // Upload le fichier si sélectionné
          if (this.selectedFile && newIntervention.interventionPreventiveId) {
            this.uploadFileAfterSave(newIntervention.interventionPreventiveId);
          } else {
            this.loadInterventions();
            this.closeModal();
          }
        },
        error: (err) => {
          console.error('Erreur lors de l\'ajout', err);
        }
      });
    }
  }


  confirmDelete(intervention: InterventionPreventive): void {
    this.interventionToDelete = intervention;
    this.showDeleteModal = true;
  }

  deleteIntervention(): void {
    if (this.interventionToDelete && this.interventionToDelete.interventionPreventiveId) {
      this.interventionPreventiveService.deleteInterventionPreventive(this.interventionToDelete.interventionPreventiveId).subscribe({
        next: () => {
          this.loadInterventions();
          this.closeDeleteModal();
        },
        error: (err) => {
          console.error('Erreur lors de la suppression', err);
        }
      });
    }
  }

  search(): void {
    if (this.searchTerm && this.searchTerm.trim() !== '') {
      this.interventionPreventiveService.searchInterventionsPreventives(this.searchTerm).subscribe({
        next: (data) => {
          this.interventions = data;
        },
        error: (err) => {
          console.error('Erreur lors de la recherche', err);
        }
      });
    } else {
      this.loadInterventions();
    }
  }

  formatDate(date: string): string {
    if (!date) return '-';
    const d = new Date(date);
    const day = d.getDate().toString().padStart(2, '0');
    const month = (d.getMonth() + 1).toString().padStart(2, '0');
    const year = d.getFullYear();
    return `${day}/${month}/${year}`;
  }

  closeModal(): void {
    this.showModal = false;
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.interventionToDelete = null;
  }

  // ==================== GESTION DES FICHIERS ====================

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  getFileDownloadUrl(id: number | null | undefined): string {
    if (!id) return '';
    return this.interventionPreventiveService.getFileDownloadUrl(id);
  }

  uploadFileAfterSave(interventionId: number): void {
    if (!this.selectedFile) return;

    this.uploading = true;
    this.interventionPreventiveService.uploadFile(interventionId, this.selectedFile).subscribe({
      next: () => {
        this.uploading = false;
        this.loadInterventions();
        this.closeModal();
      },
      error: (error) => {
        this.uploading = false;
        console.error('Erreur upload fichier', error);
        alert('Intervention sauvegardée mais erreur lors de l\'upload du fichier');
        this.loadInterventions();
        this.closeModal();
      }
    });
  }

  clearSelectedFile(): void {
    this.selectedFile = null;
    const fileInput = document.getElementById('fichierInterventionPreventive') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  deleteExistingFile(): void {
    if (!this.currentInterventionId) return;

    if (confirm('Êtes-vous sûr de vouloir supprimer ce fichier ?')) {
      this.interventionPreventiveService.deleteFile(this.currentInterventionId).subscribe({
        next: () => {
          this.existingFile = null;
          this.existingFileName = null;
          alert('Fichier supprimé avec succès');
        },
        error: (error) => {
          console.error('Erreur suppression fichier', error);
          alert('Erreur lors de la suppression du fichier');
        }
      });
    }
  }
}
