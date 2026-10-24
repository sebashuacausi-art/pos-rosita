import { Component, inject, signal } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Sesionservice } from '../../services/sesionservice';
import { Arqueoservice } from '../../services/arqueoservice';
import { Carritoservice } from '../../services/carritoservice';
import { AperturaCaja } from '../../components/modals/apertura-caja/apertura-caja';

interface OpcionMenu {
  id: string;
  label: string;
  icono: string;
  // "todos" | "dueño" | "vendedor"
  roles: string;
  /**
   * RNF: "Ninguna opción del menú puede ejecutarse con la caja cerrada,
   * salvo el módulo de Reportes del Dueño".
   * true  -> exige caja abierta (si no, abre el modal de apertura)
   * false -> accesible con caja cerrada
   */
  requiereCaja: boolean;
}

@Component({
  selector: 'app-principal',
  imports: [CommonModule, RouterOutlet],
  templateUrl: './principal.html',
  styleUrl: './principal.css'
})
export class Principal {
  sesion = inject(Sesionservice);
  carrito = inject(Carritoservice);
  private router = inject(Router);
  private modal = inject(NgbModal);
  private arqueoService = inject(Arqueoservice);

  menuActivo = signal('nueva-venta');
  aviso = signal('');

  // Menú del prototipo (el POS completo se irá agregando vista a vista)
  opciones: OpcionMenu[] = [
    { id: 'nueva-venta', label: 'Nueva venta', icono: 'bi-cart3', roles: 'todos', requiereCaja: true },
    { id: 'ventas-espera', label: 'Ventas en espera', icono: 'bi-clock-history', roles: 'todos', requiereCaja: true },
    { id: 'sin-comprobante', label: 'Sin comprobante', icono: 'bi-receipt-cutoff', roles: 'todos', requiereCaja: true },
    { id: 'busqueda', label: 'Búsqueda / Devolución', icono: 'bi-search', roles: 'todos', requiereCaja: true },
    { id: 'reservas', label: 'Reservas', icono: 'bi-bookmark-star', roles: 'todos', requiereCaja: true },
    { id: 'promociones', label: 'Promociones', icono: 'bi-tags', roles: 'dueño', requiereCaja: true },
    { id: 'arqueo', label: 'Arqueo de caja', icono: 'bi-calculator', roles: 'todos', requiereCaja: true },
    { id: 'desembolsos', label: 'Desembolsos', icono: 'bi-cash-stack', roles: 'dueño', requiereCaja: true },
    { id: 'cierre', label: 'Cierre de caja', icono: 'bi-box-arrow-right', roles: 'todos', requiereCaja: true },
    // Excepción de la spec: el Dueño ve Reportes con la caja cerrada
    { id: 'reportes', label: 'Reportes', icono: 'bi-bar-chart-line', roles: 'dueño', requiereCaja: false },
    { id: 'usuarios', label: 'Usuarios', icono: 'bi-people', roles: 'dueño', requiereCaja: false }
  ];

  get opcionesVisibles(): OpcionMenu[] {
    const rol = this.sesion.usuario()?.rol?.toLowerCase() ?? '';
    return this.opciones.filter(o => o.roles === 'todos' || o.roles === rol);
  }

  get tituloVista(): string {
    return this.opciones.find(o => o.id === this.menuActivo())?.label ?? 'Nueva venta';
  }

  /**
   * RNF: con la caja cerrada, cualquier opción que muestre registros
   * abre el modal de apertura en lugar de entrar a la vista.
   */
  seleccionar(op: OpcionMenu) {
    if (op.requiereCaja && !this.sesion.cajaAbierta) {
      // al aperturar, se entra a la opción que el usuario pidió
      this.abrirApertura(op);
      return;
    }
    this.menuActivo.set(op.id);
    // Vistas construidas hasta ahora; las demás siguen pendientes
    const construidas = ['nueva-venta', 'arqueo', 'cierre', 'usuarios'];
    if (construidas.includes(op.id)) {
      this.router.navigate(['/panel', op.id === 'nueva-venta' ? '' : op.id]);
    } else if (op.id !== 'reportes') {
      this.mostrarAviso(`Módulo "${op.label}" pendiente de construir`);
    }
  }

  private abrirApertura(destino?: OpcionMenu) {
    const ref = this.modal.open(AperturaCaja, { centered: true, size: 'lg' });
    ref.result.then(() => {
      this.mostrarAviso('Caja aperturada. Ya puede operar.');
      if (destino) this.seleccionar(destino);
    }).catch(() => {});
  }

  cerrarSesion() {
    const s = this.sesion.sesionCaja();
    const rol = this.sesion.usuario()?.rol?.toLowerCase() ?? '';

    // RN-015 / HU-01: el vendedor no puede salir con ventas sin arquear
    if (s && rol === 'vendedor') {
      this.arqueoService.calcular(s.idSesion).subscribe({
        next: (r) => {
          if (r.cantidadVentas > 0 && !r.arqueado) {
            this.mostrarAviso(
              'No puedes cerrar sesión: tu caja tiene ventas pendientes de cuadrar. ' +
              'Realiza el arqueo de caja primero.');
            return;
          }
          this.salirConVentaEnCurso();
        },
        // sin datos de arqueo no se bloquea el cierre de sesión
        error: () => this.salirConVentaEnCurso()
      });
      return;
    }
    this.salirConVentaEnCurso();
  }

  // HU-01: la venta en curso pide confirmación explícita antes de salir
  private salirConVentaEnCurso() {
    if (!this.carrito.vacio) {
      const ok = window.confirm(
        'Hay una venta en curso. Si cierras sesión se perderá. ' +
        '¿Deseas continuar? Puedes dejarla en espera antes de salir.');
      if (!ok) return;
      this.carrito.limpiar();
    }
    // la sesión de caja sigue viva en la base: al volver se restaura
    this.sesion.cerrarSesion();
    this.router.navigate(['/']);
  }

  private mostrarAviso(texto: string) {
    this.aviso.set(texto);
    setTimeout(() => this.aviso.set(''), 3000);
  }
}
