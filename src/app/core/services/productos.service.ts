import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Pelicula, Producto, Page } from '../models/cine.models';

// Cuerpo que espera el microservicio para crear/modificar un producto.
export interface ProductoPayload {
  nombre: string;
  descripcion: string;
  precio: number;
  stock: number;
  categoria: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductosService {
  // Todas las peticiones pasan por el Gateway (8081), que valida el JWT
  // y rutea a modulo-productos. Pegarle directo a 8082 da 403.
  private apiUrl = 'http://localhost:8081';

  private mockPeliculas: Pelicula[] = [
    {
      id: 1,
      titulo: 'RESIDENT EVIL: NOCHE CERO',
      genero: 'Terror / Acción',
      duracion: 114,
      director: 'Zach Cregger',
      sinopsis: 'Llega la aterradora reinvención de la saga de terror más aclamada de los videojuegos, ambientada en las oscuras instalaciones de la Corporación Umbrella.',
      clasificacionEdad: 'R-17',
      calificacion: 4.8,
      poster: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=600&auto=format&fit=crop',
      reparto: 'Zachary Levi, Kaya Scodelario',
      formatoDisponible: 'IMAX 3D / DBOX'
    },
    {
      id: 2,
      titulo: 'SPIDER-MAN: BEYOND THE SPIDER-VERSE',
      genero: 'Animación / Acción',
      duracion: 140,
      director: 'Joaquim Dos Santos',
      sinopsis: 'Miles Morales emprende un viaje épico a través del Multiverso para salvar a sus seres queridos de una amenaza cataclísmica imprevista.',
      clasificacionEdad: 'SAM 13',
      calificacion: 4.9,
      poster: 'https://images.unsplash.com/photo-1635805737707-575885ab0820?q=80&w=600&auto=format&fit=crop',
      reparto: 'Shameik Moore, Hailee Steinfeld',
      formatoDisponible: '2D / 3D / ATMOS'
    },
    {
      id: 3,
      titulo: 'OPPENHEIMER',
      genero: 'Drama / Historia',
      duracion: 180,
      director: 'Christopher Nolan',
      sinopsis: 'La historia del físico teórico J. Robert Oppenheimer y su papel fundamental en el desarrollo de las primeras armas nucleares.',
      clasificacionEdad: 'SAM 16',
      calificacion: 5.0,
      poster: 'https://images.unsplash.com/photo-1440404653325-ab127d49abc1?q=80&w=600&auto=format&fit=crop',
      reparto: 'Cillian Murphy, Emily Blunt, Matt Damon',
      formatoDisponible: 'IMAX 70MM / 2D'
    }
  ];

  constructor(private http: HttpClient) {}

  // Catálogo de productos real del microservicio (via Gateway).
  // Es LECTURA PÚBLICA: funciona sin iniciar sesión.
  // El backend devuelve un Page, así que se toma .content (y se tolera
  // el caso de que alguna versión devuelva un array plano).
  getProductos(): Observable<Producto[]> {
    return this.http.get<Page<Producto> | Producto[]>(`${this.apiUrl}/productos?size=100`).pipe(
      map(res => Array.isArray(res) ? res : (res?.content ?? []))
    );
  }

  // --- Administración del catálogo (solo ADMIN, lo valida el Gateway) ---

  crearProducto(payload: ProductoPayload): Observable<Producto> {
    return this.http.post<Producto>(`${this.apiUrl}/productos`, payload);
  }

  actualizarProducto(id: number, payload: ProductoPayload): Observable<Producto> {
    return this.http.put<Producto>(`${this.apiUrl}/productos/${id}`, payload);
  }

  eliminarProducto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/productos/${id}`);
  }

  // No hay microservicio de películas todavía: se mantiene el mock.
  obtenerPeliculas(): Observable<Pelicula[]> {
    return this.http.get<Pelicula[]>(`${this.apiUrl}/peliculas`).pipe(
      catchError(() => of(this.mockPeliculas))
    );
  }
}
