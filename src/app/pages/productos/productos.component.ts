import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ProductosService, ProductoPayload } from '../../core/services/productos.service';
import { Producto } from '../../core/models/cine.models';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-productos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './productos.component.html',
  styleUrl: './productos.component.css'
})
export class ProductosComponent implements OnInit {

  productos: Producto[] = [];
  cargando: boolean = true;
  errorMsg: string = '';

  // Categorías válidas del microservicio (enum CategoriaProducto).
  categorias: string[] = ['COMBO', 'COMIDA', 'BEBIDA', 'MERCHANDISING'];

  // Estado del formulario de administración (solo ADMIN).
  mostrarFormulario: boolean = false;
  editandoId: number | null = null;
  guardando: boolean = false;
  formError: string = '';
  form: { nombre: string; descripcion: string; precio: number | null; stock: number | null; categoria: string } = this.formVacio();

  constructor(
    private productosService: ProductosService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cargarProductos();
  }

  // Solo el ADMIN puede gestionar (alta/baja/edición) el catálogo.
  get esAdmin(): boolean {
    return this.authService.isAdmin();
  }

  cargarProductos(): void {
    this.cargando = true;
    this.errorMsg = '';
    this.productosService.getProductos().subscribe({
      next: (data) => {
        this.productos = data;
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al obtener productos:', err);
        this.errorMsg = 'No se pudieron cargar los productos del backend.';
        this.cargando = false;
      }
    });
  }

  // Ver el catálogo es libre, pero reservar requiere iniciar sesión.
  agregar(producto: Producto): void {
    if (!this.authService.isLoggedIn()) {
      alert('Iniciá sesión para poder reservar productos.');
      this.router.navigate(['/login']);
      return;
    }
    alert(`¡Agregaste "${producto.nombre}" al carrito!`);
  }

  // ---------- Administración (solo ADMIN) ----------

  abrirCrear(): void {
    this.editandoId = null;
    this.form = this.formVacio();
    this.formError = '';
    this.mostrarFormulario = true;
  }

  abrirEditar(p: Producto): void {
    this.editandoId = p.id ?? null;
    this.form = {
      nombre: p.nombre,
      descripcion: p.descripcion,
      precio: p.precio,
      stock: p.stock ?? 0,
      categoria: p.categoria ?? 'COMIDA'
    };
    this.formError = '';
    this.mostrarFormulario = true;
  }

  cancelar(): void {
    this.mostrarFormulario = false;
    this.formError = '';
  }

  guardar(): void {
    if (!this.form.nombre || !this.form.nombre.trim()) {
      this.formError = 'El nombre es obligatorio.';
      return;
    }
    if (this.form.precio === null || this.form.precio <= 0) {
      this.formError = 'El precio debe ser mayor a 0.';
      return;
    }
    if (this.form.stock === null || this.form.stock < 0) {
      this.formError = 'El stock no puede ser negativo.';
      return;
    }

    const payload: ProductoPayload = {
      nombre: this.form.nombre.trim(),
      descripcion: this.form.descripcion,
      precio: this.form.precio,
      stock: this.form.stock,
      categoria: this.form.categoria
    };

    this.guardando = true;
    const peticion = this.editandoId !== null
      ? this.productosService.actualizarProducto(this.editandoId, payload)
      : this.productosService.crearProducto(payload);

    peticion.subscribe({
      next: () => {
        this.guardando = false;
        this.mostrarFormulario = false;
        this.formError = '';
        this.cargarProductos();
      },
      error: (err) => {
        this.guardando = false;
        if (err?.status === 409) {
          this.formError = 'Ya existe un producto con ese nombre.';
        } else if (err?.status === 403) {
          this.formError = 'No tenés permisos de administrador.';
        } else {
          this.formError = 'No se pudo guardar el producto.';
        }
      }
    });
  }

  eliminar(p: Producto): void {
    if (p.id === undefined || p.id === null) {
      return;
    }
    if (!confirm(`¿Dar de baja "${p.nombre}"?`)) {
      return;
    }
    this.productosService.eliminarProducto(p.id).subscribe({
      next: () => this.cargarProductos(),
      error: () => alert('No se pudo eliminar el producto.')
    });
  }

  private formVacio() {
    return { nombre: '', descripcion: '', precio: null, stock: null, categoria: 'COMIDA' };
  }
}
