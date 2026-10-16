export enum EstadoButaca {
  DISPONIBLE = 'DISPONIBLE',
  OCUPADO = 'OCUPADO'
}

export enum TipoSala {
  SALA_2D = 'SALA_2D',
  SALA_3D = 'SALA_3D',
  IMAX = 'IMAX',
  VIP = 'VIP'
}

export enum TipoConfiteria {
  COMIDA = 'COMIDA',
  BEBIDA = 'BEBIDA',
  COMBO = 'COMBO',
  MERCHANDISING = 'MERCHANDISING'
}

export interface Pelicula {
  id: number;
  titulo: string;
  genero: string;
  duracion: number;
  director: string;
  guionista?: string;
  sinopsis: string;
  clasificacionEdad: string;
  calificacion: number;
  poster: string;
  reparto?: string;
  formatoDisponible?: string;
}

export interface Confiteria {
  id: number;
  nombre: string;
  descripcion: string;
  precio: number;
  estado: string;
  tipo: TipoConfiteria;
  imagen?: string;
}

export interface Sala {
  id: number;
  nombre: string;
  capacidad: number;
  tipo: TipoSala;
}

export interface FuncionResponseDTO {
  id: number;
  salaId: number;
  nombreSala: string;
  peliculaId: number;
  fechaHora: string;
  precioBase: number;
}

export interface ButacaDTO {
  id: number;
  salaId: number;
  fila: string;
  numero: number;
  estado: EstadoButaca;
  seleccionada?: boolean;
}

export interface ActualizarEstadoButacaDTO {
  estado: EstadoButaca;
}

export interface Producto {
  id?: number;
  nombre: string;
  descripcion: string;
  precio: number;
  stock?: number;
  activo?: boolean;
  imagenUrl?: string;
  categoria?: string;
  creadoEn?: string;
  actualizadoEn?: string;
}

// Respuesta paginada de Spring Data (GET /productos devuelve Page<ProductoSalidaDTO>).
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}