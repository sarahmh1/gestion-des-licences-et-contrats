import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from 'app/Services/user.service';
import { User } from 'app/Model/User';

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.scss']
})
export class UserrComponent implements OnInit {
  users: User[] = [];
  filteredUsers: User[] = [];
  pagedUsers: User[] = [];
  userForm: FormGroup;
  isEditMode = false;
  editingUserId: number | null = null;
  showModal = false;
  searchTerm: string = '';
  
  // Propriétés pour la pagination
  currentPage: number = 0;
  pageSize: number = 10;
  totalPages: number = 0;
  pageNumbers: number[] = [];

  constructor(private userService: UserService, private fb: FormBuilder) {
    this.userForm = this.fb.group({
      firstname: ['', Validators.required],
      lastname: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      role: ['', Validators.required],
      password: ['', Validators.required],
      dateOfBirth: ['', Validators.required],
      phoneNumber: ['', Validators.required],
      sex: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.getUsers();
  }

  getUsers() {
    this.userService.getAllUsers().subscribe(data => {
      this.users = data;
      this.filteredUsers = [...this.users];
      this.updatePagination();
    });
  }

  // Méthode de recherche
  onSearch() {
    if (!this.searchTerm) {
      this.filteredUsers = [...this.users];
    } else {
      const searchLower = this.searchTerm.toLowerCase();
      this.filteredUsers = this.users.filter(user =>
        user.firstname?.toLowerCase().includes(searchLower) ||
        user.lastname?.toLowerCase().includes(searchLower) ||
        user.email?.toLowerCase().includes(searchLower) ||
        user.role?.toLowerCase().includes(searchLower) ||
        user.phoneNumber?.toLowerCase().includes(searchLower) ||
        user.sex?.toLowerCase().includes(searchLower)
      );
    }
    this.currentPage = 0;
    this.updatePagination();
  }

  // Méthodes pour la pagination
  updatePagination() {
    this.totalPages = Math.ceil(this.filteredUsers.length / this.pageSize);
    this.pageNumbers = Array(this.totalPages).fill(0).map((x, i) => i);
    this.updatePagedUsers();
  }

  updatePagedUsers() {
    const startIndex = this.currentPage * this.pageSize;
    this.pagedUsers = this.filteredUsers.slice(startIndex, startIndex + this.pageSize);
  }

  changePage(page: number) {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.updatePagedUsers();
    }
  }

  openAddModal() {
    this.isEditMode = false;
    this.editingUserId = null;
    this.userForm.reset();
    this.userForm.get('password')?.setValidators(Validators.required);
    this.userForm.get('password')?.updateValueAndValidity();
    this.showModal = true;
  }

  openEditModal(user: User) {
    this.isEditMode = true;
    this.editingUserId = user.id!;
    this.userForm.patchValue(user);
    this.userForm.get('password')?.clearValidators();
    this.userForm.get('password')?.updateValueAndValidity();
    this.showModal = true;
  }

  submitForm() {
    if (this.userForm.invalid) return;

    const userData = this.userForm.value;

    if (this.isEditMode && this.editingUserId != null) {
      this.userService.updateUser(this.editingUserId, userData).subscribe(() => {
        this.getUsers();
        this.showModal = false;
      });
    } else {
      this.userService.createUser(userData).subscribe(() => {
        this.getUsers();
        this.showModal = false;
      });
    }
  }

  closeModal() {
    this.showModal = false;
  }

  deleteUser(id: number) {
    this.userService.deleteUser(id).subscribe(() => this.getUsers());
  }

  activateUser(id: number) {
    this.userService.activateUser(id).subscribe(() => this.getUsers());
  }

  deactivateUser(id: number) {
    this.userService.deactivateUser(id).subscribe(() => this.getUsers());
  }

  togglePasswordVisibility() {
    const passwordField = document.getElementById('passwordField') as HTMLInputElement;
    const passwordIcon = document.getElementById('passwordIcon');
    
    if (passwordField && passwordIcon) {
      if (passwordField.type === 'password') {
        passwordField.type = 'text';
        passwordIcon.classList.remove('glyphicon-eye-open');
        passwordIcon.classList.add('glyphicon-eye-close');
        passwordIcon.parentElement?.classList.add('password-visible');
      } else {
        passwordField.type = 'password';
        passwordIcon.classList.remove('glyphicon-eye-close');
        passwordIcon.classList.add('glyphicon-eye-open');
        passwordIcon.parentElement?.classList.remove('password-visible');
      }
    }
  }
}