import { CanActivateFn, Router } from '@angular/router';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth.service';

/**
 * Deja pasar solo si hay sesion Y el rol es ADMIN.
 * Si no hay sesion -> login. Si hay sesion pero no es ADMIN -> cartelera.
 */
export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);

  if (!isPlatformBrowser(platformId)) {
    return true;
  }

  if (!authService.isLoggedIn()) {
    return router.createUrlTree(['/login']);
  }

  if (authService.isAdmin()) {
    return true;
  }

  return router.createUrlTree(['/cartelera']);
};
