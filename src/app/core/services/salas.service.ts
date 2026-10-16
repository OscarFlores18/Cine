import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { FuncionResponseDTO, Sala, ButacaDTO, ActualizarEstadoButacaDTO, EstadoButaca, TipoSala } from '../models/cine.models';

@Injectable({
  providedIn: 'root'
})
export class SalasService {
  private apiUrl = 'http://localhost:8081';

  private mockSalas: Sala[] = [
    { id: 1, nombre: 'Sala 1 - IMAX', capacidad: 120, tipo: TipoSala.IMAX },
    { id: 2, nombre: 'Sala 2 - 3D Atmos', capacidad: 90, tipo: TipoSala.SALA_3D },
    { id: 3, nombre: 'Sala 3 - VIP Motion', capacidad: 60, tipo: TipoSala.VIP }
  ];

  private mockFunciones: FuncionResponseDTO[] = [
    { id: 101, salaId: 1, nombreSala: 'Sala 1 - IMAX', peliculaId: 1, fechaHora: '2026-10-14T18:30:00', precioBase: 8500 },
    { id: 102, salaId: 1, nombreSala: 'Sala 1 - IMAX', peliculaId: 1, fechaHora: '2026-10-14T21:45:00', precioBase: 8500 },
    { id: 103, salaId: 2, nombreSala: 'Sala 2 - 3D Atmos', peliculaId: 1, fechaHora: '2026-10-14T20:00:00', precioBase: 7000 },
    { id: 104, salaId: 3, nombreSala: 'Sala 3 - VIP Motion', peliculaId: 2, fechaHora: '2026-10-14T19:15:00', precioBase: 11000 },
    { id: 105, salaId: 1, nombreSala: 'Sala 1 - IMAX', peliculaId: 3, fechaHora: '2026-10-14T22:00:00', precioBase: 9000 }
  ];

  constructor(private http: HttpClient) {}

  obtenerCartelera(): Observable<FuncionResponseDTO[]> {
    return this.http.get<FuncionResponseDTO[]>(`${this.apiUrl}/funciones/cartelera`).pipe(
      catchError(() => of(this.mockFunciones))
    );
  }

  obtenerSalas(): Observable<Sala[]> {
    return this.http.get<Sala[]>(`${this.apiUrl}/salas`).pipe(
      catchError(() => of(this.mockSalas))
    );
  }

  obtenerButacasPorSala(salaId: number): Observable<ButacaDTO[]> {
    return this.http.get<ButacaDTO[]>(`${this.apiUrl}/butacas/sala/${salaId}`).pipe(
      catchError(() => of(this.generarMapaInmersivo(salaId)))
    );
  }

  actualizarEstadoButaca(id: number, dto: ActualizarEstadoButacaDTO): Observable<ButacaDTO> {
    return this.http.patch<ButacaDTO>(`${this.apiUrl}/butacas/${id}/estado`, dto);
  }

  private generarMapaInmersivo(salaId: number): ButacaDTO[] {
    const filas = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'];
    const asientosPorFila = 12;
    const butacas: ButacaDTO[] = [];
    let idCounter = 1;

    filas.forEach(fila => {
      for (let num = 1; num <= asientosPorFila; num++) {
        const esOcupado = (fila === 'C' && (num === 5 || num === 6)) ||
                          (fila === 'F' && (num === 7 || num === 8 || num === 9)) ||
                          (fila === 'H' && (num === 2 || num === 3)) ||
                          (fila === 'J' && (num === 11 || num === 12));

        butacas.push({
          id: idCounter++,
          salaId: salaId,
          fila: fila,
          numero: num,
          estado: esOcupado ? EstadoButaca.OCUPADO : EstadoButaca.DISPONIBLE
        });
      }
    });

    return butacas;
  }
}