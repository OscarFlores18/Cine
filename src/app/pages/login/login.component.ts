import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

  username = '';
  password = '';
  cargando = false;
  errorMsg = '';

  constructor(private authService: AuthService, private router: Router) {}

  ingresar(): void {
    if (!this.username.trim() || !this.password.trim()) {
      this.errorMsg = 'Completá usuario y contraseña.';
      return;
    }

    this.cargando = true;
    this.errorMsg = '';

    this.authService.login(this.username.trim(), this.password).subscribe({
      next: () => {
        this.cargando = false;
        this.router.navigate(['/productos']);
      },
      error: (err) => {
        this.cargando = false;
        this.errorMsg = err?.status === 401
          ? 'Usuario o contraseña incorrectos.'
          : 'No se pudo iniciar sesión. Verificá que el backend esté arriba.';
      }
    });
  }
}
