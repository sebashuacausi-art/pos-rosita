import { Injectable, signal } from '@angular/core';
import { ItemCarrito } from '../models/venta';

/**
 * Carrito de venta compartido.
 * Vive en un servicio para que:
 *  - no se pierda al navegar entre módulos del panel,
 *  - Principal pueda validar "venta en curso" al cerrar sesión (HU-01).
 */
@Injectable({ providedIn: 'root' })
export class Carritoservice {
  items = signal<ItemCarrito[]>([]);

  get vacio(): boolean {
    return this.items().length === 0;
  }

  limpiar() {
    this.items.set([]);
  }
}
