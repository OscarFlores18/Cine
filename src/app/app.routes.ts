import { Routes } from '@angular/router';
import { CarteleraComponent } from './pages/cartelera/cartelera.component';
import { MapaAsientosComponent } from './pages/mapa-asientos/mapa-asientos.component';

export const routes: Routes = [
  { path: '', redirectTo: 'cartelera', pathMatch: 'full' },
  { path: 'cartelera', component: CarteleraComponent },
  { path: 'mapa-asientos/:salaId', component: MapaAsientosComponent },
  // La confiteria es publica: se puede ver el catalogo sin iniciar sesion.
  // (Administrar el catalogo si requiere ser ADMIN.)
  {
    path: 'productos',
    loadComponent: () => import('./pages/productos/productos.component').then(m => m.ProductosComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent)
  },
  // El comodin SIEMPRE va al final.
  { path: '**', redirectTo: 'cartelera' }
];
