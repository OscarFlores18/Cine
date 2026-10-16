import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ProductosService } from '../../core/services/productos.service';
import { SalasService } from '../../core/services/salas.service';
import { Pelicula, FuncionResponseDTO } from '../../core/models/cine.models';

@Component({
  selector: 'app-cartelera',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cartelera.component.html',
  styleUrls: ['./cartelera.component.css']
})
export class CarteleraComponent implements OnInit {
  peliculas: Pelicula[] = [];
  peliculaSeleccionada: Pelicula | null = null;
  funciones: FuncionResponseDTO[] = [];
  funcionesFiltradas: FuncionResponseDTO[] = [];
  
  cineSeleccionado: string = 'Abasto';
  diaSeleccionado: string = 'HOY';
  mostrarSelectorCine: boolean = false;
  cinesDisponibles = ['Abasto', 'Alto Avellaneda', 'Palermo', 'Dot Baires'];

  constructor(
    private productosService: ProductosService,
    private salasService: SalasService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cargarPeliculas();
    this.cargarFunciones();
  }

  cargarPeliculas(): void {
    this.productosService.obtenerPeliculas().subscribe(data => {
      this.peliculas = data;
      if (data.length > 0) {
        this.peliculaSeleccionada = data[0];
        this.filtrarFunciones();
      }
    });
  }

  cargarFunciones(): void {
    this.salasService.obtenerCartelera().subscribe(data => {
      this.funciones = data;
      this.filtrarFunciones();
    });
  }

  seleccionarPelicula(pelicula: Pelicula): void {
    this.peliculaSeleccionada = pelicula;
    this.filtrarFunciones();
  }

  seleccionarCine(cine: string): void {
    this.cineSeleccionado = cine;
    this.mostrarSelectorCine = false;
    this.filtrarFunciones();
  }

  seleccionarDia(dia: string): void {
    this.diaSeleccionado = dia;
    this.filtrarFunciones();
  }

  filtrarFunciones(): void {
    if (!this.peliculaSeleccionada) return;
    this.funcionesFiltradas = this.funciones.filter(f => f.peliculaId === this.peliculaSeleccionada?.id);
    if (this.funcionesFiltradas.length === 0 && this.funciones.length > 0) {
      this.funcionesFiltradas = this.funciones;
    }
  }

  irAMapa(funcion: FuncionResponseDTO): void {
    this.router.navigate(['/mapa-asientos', funcion.salaId], {
      queryParams: {
        precio: funcion.precioBase,
        funcionId: funcion.id,
        pelicula: this.peliculaSeleccionada?.titulo
      }
    });
  }
}