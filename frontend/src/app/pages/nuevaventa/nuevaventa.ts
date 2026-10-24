import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { Sesionservice } from '../../services/sesionservice';
import { Productoservice } from '../../services/productoservice';
import { Carritoservice } from '../../services/carritoservice';
import { Producto } from '../../models/producto';
import { ItemCarrito } from '../../models/venta';
import { VentaRespuesta } from '../../models/venta-respuesta';

import { AperturaCaja } from '../../components/modals/apertura-caja/apertura-caja';
import { Cobro } from '../../components/modals/cobro/cobro';
import { Boleta } from '../../components/modals/boleta/boleta';
import { VentaRegistrada } from '../../components/modals/venta-registrada/venta-registrada';

@Component({
  selector: 'app-nuevaventa',
  imports: [CommonModule, FormsModule],
  templateUrl: './nuevaventa.html',
  styleUrl: './nuevaventa.css'
})
export class NuevaVenta {
  sesion = inject(Sesionservice);
  private productoService = inject(Productoservice);
  private modal = inject(NgbModal);

  // catálogo
  productos = signal<Producto[]>([]);
  cargandoCatalogo = signal(false);
  textoBusqueda = signal('');
  categoriaSel = signal<number | null>(null);

  // carrito compartido (sobrevive a la navegación entre módulos)
  carrito = inject(Carritoservice).items;

  // avisos
  aviso = signal<{ texto: string; tipo: 'ok' | 'error' } | null>(null);

  // correlativo consecutivo simulado en el cliente
  correlativo = signal('V-000001');

  categorias = computed(() => {
    const mapa = new Map<number, string>();
    for (const p of this.productos()) mapa.set(p.idCategoria, p.categoria);
    return Array.from(mapa.entries()).map(([id, nombre]) => ({ id, nombre }));
  });

  productosVisibles = computed(() => {
    const texto = this.textoBusqueda().trim().toLowerCase();
    const cat = this.categoriaSel();
    return this.productos().filter(p => {
      const coincideTexto = texto === '' ||
        p.nombreProducto.toLowerCase().includes(texto) ||
        p.codigoProducto.toLowerCase().includes(texto);
      const coincideCat = cat === null || p.idCategoria === cat;
      return coincideTexto && coincideCat;
    });
  });

  total = computed(() =>
    this.carrito().reduce((suma, i) =>
      suma + (i.precioUnitario * i.cantidad) - i.descuento, 0)
  );

  totalUnidades = computed(() =>
    this.carrito().reduce((suma, i) => suma + i.cantidad, 0)
  );

  ngOnInit() {
    this.cargarCatalogo();
  }

  cargarCatalogo() {
    this.cargandoCatalogo.set(true);
    this.productoService.listar().subscribe({
      next: (lista) => {
        this.productos.set(lista);
        this.cargandoCatalogo.set(false);
      },
      error: () => {
        this.cargandoCatalogo.set(false);
        this.mostrarAviso('No se pudo cargar el catálogo. ¿Está el backend activo?', 'error');
      }
    });
  }

  buscar() {
    const texto = this.textoBusqueda().trim();
    this.cargandoCatalogo.set(true);
    this.productoService.buscar(texto).subscribe({
      next: (lista) => {
        this.productos.set(lista);
        this.cargandoCatalogo.set(false);
      },
      error: () => this.cargandoCatalogo.set(false)
    });
  }

  filtrarPorCategoria(id: number | null) {
    this.categoriaSel.set(id);
  }

  // HU-03 / RN-001: agrega al carrito validando stock
  agregar(p: Producto) {
    if (p.disponible <= 0) {
      this.mostrarAviso(`${p.nombreProducto} no tiene stock disponible`, 'error');
      return;
    }

    const existe = this.carrito().find(i => i.idProducto === p.idProducto);
    if (existe) {
      if (existe.cantidad + 1 > p.disponible) {
        this.mostrarAviso(
          `Solo hay ${p.disponible} unidades de ${p.nombreProducto} (RN-001)`, 'error');
        return;
      }
      this.carrito.update(c => c.map(i =>
        i.idProducto === p.idProducto ? { ...i, cantidad: i.cantidad + 1 } : i));
    } else {
      this.carrito.update(c => [...c, {
        idProducto: p.idProducto,
        codigoProducto: p.codigoProducto,
        nombreProducto: p.nombreProducto,
        precioUnitario: p.precioUnitario,
        cantidad: 1,
        descuento: 0,
        disponible: p.disponible
      }]);
    }
  }

  cambiarCantidad(item: ItemCarrito, delta: number) {
    const nueva = item.cantidad + delta;
    if (nueva <= 0) {
      this.quitar(item);
      return;
    }
    if (nueva > item.disponible) {
      this.mostrarAviso(
        `Solo hay ${item.disponible} unidades disponibles (RN-001)`, 'error');
      return;
    }
    this.carrito.update(c => c.map(i =>
      i.idProducto === item.idProducto ? { ...i, cantidad: nueva } : i));
  }

  quitar(item: ItemCarrito) {
    this.carrito.update(c => c.filter(i => i.idProducto !== item.idProducto));
  }

  vaciarCarrito() {
    this.carrito.set([]);
  }

  // HU-02: no se puede vender con caja cerrada
  get cajaAbierta(): boolean {
    return this.sesion.cajaAbierta;
  }

  abrirAperturaCaja() {
    const ref = this.modal.open(AperturaCaja, { centered: true, size: 'lg' });
    ref.result.then(() => {
      this.mostrarAviso('Caja aperturada. Ya puede registrar ventas.', 'ok');
    }).catch(() => {});
  }

  // HU-06 / HU-07: abre el cobro
  cobrar() {
    if (this.carrito().length === 0) {
      this.mostrarAviso('Agregue productos antes de cobrar (RN-012)', 'error');
      return;
    }
    if (!this.cajaAbierta) {
      this.abrirAperturaCaja();
      return;
    }

    const ref = this.modal.open(Cobro, { centered: true, size: 'lg' });
    ref.componentInstance.carrito = this.carrito();
    ref.componentInstance.total = this.total();
    ref.componentInstance.correlativo = this.correlativo();

    ref.result.then((venta: VentaRespuesta) => {
      this.carrito.set([]);
      this.siguienteCorrelativo();
      this.cargarCatalogo();
      this.mostrarBoleta(venta);
    }).catch(() => {});
  }

  // HU-08: muestra la boleta y luego el mensaje de éxito
  private mostrarBoleta(venta: VentaRespuesta) {
    const refBoleta = this.modal.open(Boleta, { centered: true, size: 'lg' });
    refBoleta.componentInstance.venta = venta;

    refBoleta.result.then(() => {
      const refExito = this.modal.open(VentaRegistrada, { centered: true, size: 'md' });
      refExito.componentInstance.venta = venta;
      refExito.result.catch(() => {});
    }).catch(() => {});
  }

  private siguienteCorrelativo() {
    const actual = parseInt(this.correlativo().split('-')[1], 10) || 0;
    this.correlativo.set('V-' + String(actual + 1).padStart(6, '0'));
  }

  private mostrarAviso(texto: string, tipo: 'ok' | 'error') {
    this.aviso.set({ texto, tipo });
    setTimeout(() => this.aviso.set(null), 3200);
  }
}
