import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface LoginResponse {
  token: string;
  tokenType: string;
  refreshToken: string;
  username: string;
  rol: string;
  expiresInSeconds: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  // Punto de entrada: el Gateway (todas las rutas pasan por aca).
  private apiUrl = 'http://localhost:8081/auth';

  private tokenKey = 'jwt_token';
  private refreshKey = 'refresh_token';
  private userKey = 'username';
  private rolKey = 'rol';

  constructor(private http: HttpClient) {}

  // POST /auth/login -> guarda el token y los datos del usuario.
  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, { username, password }).pipe(
      tap(res => {
        this.setToken(res.token);
        localStorage.setItem(this.refreshKey, res.refreshToken ?? '');
        localStorage.setItem(this.userKey, res.username ?? '');
        localStorage.setItem(this.rolKey, res.rol ?? '');
      })
    );
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  setToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  getUsername(): string | null {
    return localStorage.getItem(this.userKey);
  }

  getRol(): string | null {
    return localStorage.getItem(this.rolKey);
  }

  isAdmin(): boolean {
    return this.getRol() === 'ADMIN';
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.refreshKey);
    localStorage.removeItem(this.userKey);
    localStorage.removeItem(this.rolKey);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }
}
