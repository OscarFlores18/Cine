import { Component, OnInit } from '@angular/core';

// Interfaz para el producto
export interface Producto {
  id?: number;
  nombre: string;
  descripcion: string;
  precio: number;
  imagenUrl: string;
  categoria?: string;
}

@Component({
  selector: 'app-productos',
  templateUrl: './productos.component.html',
  styleUrls: ['./productos.component.css'] // Corregido: styleUrls en plural como arreglo
})
export class ProductosComponent implements OnInit {

  productos: Producto[] = [
    {
      id: 1,
      nombre: 'Combo Mega Pochoclos',
      descripcion: '1 Balde gigante de pochoclos + 2 Gaseosas grandes a elección.',
      precio: 14500,
      categoria: 'Combos',
      imagenUrl: 'https://images.unsplash.com/photo-1578849278619-e73505e9610f?q=80&w=500&auto=format&fit=crop'
    },
    {
      id: 2,
      nombre: 'Combo Nachos Supreme',
      descripcion: 'Portión de nachos crujientes con salsa cheddar tibia + 1 Gaseosa.',
      precio: 11200,
      categoria: 'Snacks',
      imagenUrl: 'https://images.unsplash.com/photo-1513456852971-30c0b8199d4d?q=80&w=500&auto=format&fit=crop'
    },
    {
      id: 3,
      nombre: 'Hot Dog Especial',
      descripcion: 'Pan artesanal, salchicha gigante, papas pay y salsas.',
      precio: 8500,
      categoria: 'Comidas',
      imagenUrl: 'https://images.unsplash.com/photo-1619740455993-9e612b1af08a?q=80&w=500&auto=format&fit=crop'
    }
  ];

  constructor() { }

  ngOnInit(): void {
  }

  agregar(producto: Producto): void {
    console.log('Producto seleccionado:', producto);
    alert(`Agregaste "${producto.nombre}" al carrito`);
  }
}