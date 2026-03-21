import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ContratService } from 'app/Services/contrat.service';
import { ClientService, Client } from '../../Services/client.service';
import { PRODUIT_LIST } from '../../Model/NomProduit';

@Component({
  selector: 'app-ajouter-contrat',
  templateUrl: './ajouter-contrat.component.html',
  styleUrls: ['./ajouter-contrat.component.scss']
})
export class AjouterContratComponent implements OnInit {
  clients: Client[] = [];
  contratForm!: FormGroup;
  nomProduitOptions = PRODUIT_LIST;

  constructor(
    private fb: FormBuilder,
    private contratService: ContratService,
    private router: Router,
    private clientService: ClientService) { }

  ngOnInit(): void {
    this.clientService.getAllClients().subscribe(data => this.clients = data);
    this.initForm();
    this.watchDateFin();
  }

  initForm(): void {
    this.contratForm = this.fb.group({
      client: ['', Validators.required],
      objetContrat: ['', Validators.required],
      nbInterventionsPreventives: ['', Validators.required],
      nbInterventionsCuratives: ['', Validators.required],
      dateDebut: ['', Validators.required],
      dateFin: ['', Validators.required],
      renouvelable: [false],
      remarque: [''],
      emailCommercial: ['', Validators.email],
      ccMail: this.fb.array([]),
      nomProduit: ['']
    });
  }

  watchDateFin(): void {
    this.contratForm.get('dateFin')?.valueChanges.subscribe((val: string) => {
      const renouvelable = this.contratForm.get('renouvelable');
      if (val) {
        renouvelable?.setValue(false, { emitEvent: false });
        renouvelable?.disable({ emitEvent: false });
      } else {
        renouvelable?.enable({ emitEvent: false });
      }
    });
  }

  // Getter pour le FormArray des emails CC
  get ccMailArray(): FormArray {
    return this.contratForm.get('ccMail') as FormArray;
  }

  // Ajouter un email CC
  addCcMail(): void {
    this.ccMailArray.push(this.fb.control('', Validators.email));
  }

  // Supprimer un email CC
  removeCcMail(index: number): void {
    this.ccMailArray.removeAt(index);
  }

  addContrat(): void {
    if (this.contratForm.valid) {
      const formValue = this.contratForm.value;
      const contrat = {
        ...formValue,
        ccMail: formValue.ccMail.filter((email: string) => email && email.trim() !== '')
      };
      this.contratService.addContrat(contrat).subscribe(
        () => {
          alert('Contrat ajouté avec succès');
          this.router.navigate(['/contrats']);
        },
        (error) => {
          console.error('Erreur lors de l\'ajout du contrat', error);
          alert('Erreur lors de l\'ajout du contrat');
        }
      );
    }
  }

  cancel(): void {
    this.router.navigate(['/contrats']);
  }
}
