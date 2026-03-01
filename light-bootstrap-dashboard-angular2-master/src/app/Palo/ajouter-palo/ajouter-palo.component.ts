import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CommandePasserPar } from 'app/Model/CommandePasserPar';
import { Palo } from 'app/Model/Palo';
import { PaloService } from 'app/Services/palo.service';
import { HttpEventType } from '@angular/common/http';

@Component({
  selector: 'app-ajouter-palo',
  templateUrl: './ajouter-palo.component.html',
  styleUrls: ['./ajouter-palo.component.scss']
})
export class AjouterPaloComponent implements OnInit {
  PaloForm!: FormGroup;
  
  // Variables pour l'upload de fichier
  selectedFile: File | null = null;
  uploadMessage: string | null = null;
  uploadSuccess = false;

  commandePasserParOptions = [
      { label: 'GI_TN', value: CommandePasserPar.GI_TN },
      { label: 'GI_FR', value: CommandePasserPar.GI_FR },
      { label: 'GI_CI', value: CommandePasserPar.GI_CI }
    ];
   constructor(
     private fb: FormBuilder,
     private router: Router,
     private paloService: PaloService
   ) {}
 
   ngOnInit(): void {
     this.PaloForm = this.fb.group({
       client: ['', Validators.required],
       nomDuBoitier: ['', Validators.required],
       numeroSerieBoitier: ['', Validators.required],
       dureeDeLicence: [''],
       nomDuContact: [''],
       commandePasserPar: ['', Validators.required],
       adresseEmailContact: [''],
       sousContrat: [false],
       mailAdmin: ['', [Validators.email]],
       ccMail: this.fb.array([this.fb.control('', [Validators.email])]),
       numero: [''],
       remarque: [''],
       licences: this.fb.array([
         this.createLicenceGroup()
       ])
     });
   }
 
   get ccMail(): FormArray {
     return this.PaloForm.get('ccMail') as FormArray;
   }
 
   get licences(): FormArray {
     return this.PaloForm.get('licences') as FormArray;
   }
 
   createLicenceGroup(): FormGroup {
     return this.fb.group({
       nomDesLicences: ['', Validators.required],
       quantite: ['', Validators.required],
       dateEx: ['', Validators.required]
     });
   }
 
   addLicence() {
     this.licences.push(this.createLicenceGroup());
   }
 
   removeLicence(index: number) {
     this.licences.removeAt(index);
   }
 
   addCcMail() {
     this.ccMail.push(this.fb.control('', [Validators.email]));
   }
 
   removeCcMail(index: number) {
     this.ccMail.removeAt(index);
   }
 
   setCcMail(ccMails: string[]) {
     const ccMailFormArray = this.PaloForm.get('ccMail') as FormArray;
     ccMailFormArray.clear();
     if (ccMails && ccMails.length > 0) {
       ccMails.forEach(email => ccMailFormArray.push(this.fb.control(email, Validators.email)));
     } else {
       ccMailFormArray.push(this.fb.control('', Validators.email));
     }
   }
 
   loadPalo(id: number) {
     this.paloService.getPaloById(id).subscribe(palo => {
       this.PaloForm.patchValue({
         client: palo.client,
         nomDuBoitier: palo.nomDuBoitier,
        numeroSerieBoitier: palo.numeroSerieBoitier,
         dureeDeLicence: palo.dureeDeLicence,
         commandePasserPar: palo.commandePasserPar,
         nomDuContact: palo.nomDuContact,
          sousContrat: palo.sousContrat,
         adresseEmailContact: palo.adresseEmailContact,
         mailAdmin: palo.mailAdmin,
         numero: palo.numero,
         remarque: palo.remarque
       });
 
       // Set licences (clear + patch)
       this.licences.clear();
       if (palo.licences && palo.licences.length > 0) {
         palo.licences.forEach(lic => {
           this.licences.push(this.fb.group({
             nomDesLicences: [lic.nomDesLicences, Validators.required],
             quantite: [lic.quantite, Validators.required],
             dateEx: [this.formatDate(lic.dateEx), Validators.required]
           }));
         });
       }
 
       this.setCcMail(palo.ccMail);
     });
   }
 
   formatDate(date: string | Date): string {
     const d = new Date(date);
     return d.toISOString().substring(0, 10); // 'yyyy-MM-dd'
   }
 
   addPalo() {
     if (this.PaloForm.valid) {
       const newPalo: Palo = {
         paloId: null!,
         client: this.PaloForm.value.client,
         nomDuBoitier: this.PaloForm.value.nomDuBoitier,
         numeroSerieBoitier: this.PaloForm.value.numeroSerieBoitier,
         dureeDeLicence: this.PaloForm.value.dureeDeLicence,
         commandePasserPar: this.PaloForm.value.commandePasserPar,
         nomDuContact: this.PaloForm.value.nomDuContact,
         adresseEmailContact: this.PaloForm.value.adresseEmailContact,
         mailAdmin: this.PaloForm.value.mailAdmin || '',
         ccMail: this.ccMail.value,
         sousContrat: this.PaloForm.value.sousContrat,
         numero: this.PaloForm.value.numero,
         approuve: false,
         remarque: this.PaloForm.value.remarque || '',
         licences: this.licences.value
       };
 
       this.paloService.addPalo(newPalo).subscribe(
         (response: any) => {
           console.log('Réponse serveur:', response);
           
           // Si un fichier est sélectionné, l'uploader après la création
           if (this.selectedFile && response.paloId) {
             this.uploadFileAfterCreation(response.paloId);
           } else {
             window.alert('Palo ajouté avec succès');
             this.router.navigate(['/Afficherpalo']);
           }
         },
         error => {
           console.error('Erreur lors de l\'ajout du Palo', error);
           window.alert('Échec de l\'ajout');
         }
       );
     } else {
       window.alert('Le formulaire est invalide. Veuillez corriger les erreurs.');
     }
   }

   // ==================== GESTION DES FICHIERS ====================
  
   onFileSelected(event: any): void {
     const file: File = event.target.files[0];
     if (file) {
       this.selectedFile = file;
       this.uploadMessage = null;
     }
   }

   uploadFileAfterCreation(paloId: number): void {
     if (!this.selectedFile) {
       window.alert('Palo ajouté avec succès');
       this.router.navigate(['/Afficherpalo']);
       return;
     }

     this.paloService.uploadFile(paloId, this.selectedFile).subscribe({
       next: (event: any) => {
         if (event.type === HttpEventType.Response) {
           if (event.body.success) {
             this.uploadSuccess = true;
             this.uploadMessage = 'Fichier uploadé avec succès!';
             window.alert('Palo et fichier ajoutés avec succès');
             this.router.navigate(['/Afficherpalo']);
           } else {
             this.uploadSuccess = false;
             this.uploadMessage = event.body.message || 'Erreur lors de l\'upload';
             window.alert('Palo ajouté mais erreur lors de l\'upload du fichier');
             this.router.navigate(['/Afficherpalo']);
           }
         }
       },
       error: (error) => {
         this.uploadSuccess = false;
         this.uploadMessage = 'Erreur lors de l\'upload: ' + (error.error?.message || error.message);
         console.error('Erreur upload:', error);
         window.alert('Palo ajouté mais erreur lors de l\'upload du fichier: ' + this.uploadMessage);
         this.router.navigate(['/Afficherpalo']);
       }
     });
   }

   onCancel(): void {
      this.router.navigate(['/Afficherpalo']);
    }
 }
 