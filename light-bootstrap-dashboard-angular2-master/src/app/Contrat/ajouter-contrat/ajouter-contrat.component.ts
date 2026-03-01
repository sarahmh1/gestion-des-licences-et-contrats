import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ContratService } from 'app/Services/contrat.service';

@Component({
  selector: 'app-ajouter-contrat',
  templateUrl: './ajouter-contrat.component.html',
  styleUrls: ['./ajouter-contrat.component.scss']
})
export class AjouterContratComponent implements OnInit {
  contratForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private contratService: ContratService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
  }

  initForm(): void {
    this.contratForm = this.fb.group({
      client: ['', Validators.required],
      objetContrat: ['', Validators.required],
      nbInterventionsPreventives: [0, [Validators.required, Validators.min(0)]],
      nbInterventionsCuratives: [0, [Validators.required, Validators.min(0)]],
      dateDebut: ['', Validators.required],
      dateFin: ['', Validators.required],
      renouvelable: [false],
      remarque: [''],
      emailCommercial: ['', Validators.email],
      ccMail: this.fb.array([])
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
