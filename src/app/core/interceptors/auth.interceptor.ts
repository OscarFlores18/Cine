import { HttpInterceptorFn } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth.service';

/**
 * Agrega el header "Authorization: Bearer <token>" a cada request,
 * para que el Gateway (que valida el JWT) deje pasar la peticion.
 * No lo agrega en el login/refresh: esos endpoints no llevan token.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const platformId = inject(PLATFORM_ID);

  if (!isPlatformBrowser(platformId)) {
    return next(req);
  }

  const token = authService.getToken();
  const esAuthPublico = req.url.includes('/auth/login') || req.url.includes('/auth/refresh');

  if (token && !esAuthPublico) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(req);
};
