import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../AuthService';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loginForm: FormGroup;
  errorMessage: string = '';
  successMessage: string = '';
  isSubmitting = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  onSubmit() {
  this.errorMessage = '';
  this.successMessage = '';
  this.isSubmitting = true;

  const email = this.loginForm.get('email')?.value;
  const password = this.loginForm.get('password')?.value;

  if (!email || !password) {
    this.errorMessage = 'Veuillez remplir tous les champs.';
    this.isSubmitting = false;
    return;
  }

  this.authService.login({ email, password }).subscribe({
    next: (response) => {
      this.successMessage = 'Connexion réussie ! Redirection...';
      this.isSubmitting = false;

      // Redirection ici vers dashboard (ou autre)
      setTimeout(() => {
        this.router.navigate(['/dashboard']);
      }, 1500);
    },
    error: (error) => {
      this.errorMessage = error;
      this.isSubmitting = false;
    }
  });
}




}
