import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Cajaservice } from '../../../services/cajaservice';
import { Sesionservice } from '../../../services/sesionservice';
import { Caja, SesionCaja } from '../../../models/venta';

/**
 * HU-02: apertura de caja.
 * Paso 1 -> elegir cuál caja aperturar.
 * Paso 2 -> confirmar y registrar el fondo inicial.
 */
@Component({
  selector: 'app-apertura-caja',
  imports: [CommonModule, FormsModule],
  templateUrl: './apertura-caja.html',
  styleUrl: './apertura-caja.css'
})
export class AperturaCaja {
  private cajaService = inject(Cajaservice);
  sesion = inject(Sesionservice);
  activa = inject(NgbActiveModal);

  paso = signal<1 | 2>(1);
  cargando = signal(false);
  error = signal('');

  cajas = signal<Caja[]>([]);
  cajaElegida = signal<Caja | null>(null);
  fondoInicial: number | null = null;

  ngOnInit() {
    const u = this.sesion.usuario();
    if (!u) return;

    // RNF: solo las cajas de SU rol (Dueño -> Caja 1; Vendedor -> 2, 3, 4)
    this.cajaService.cajasDeUsuario(u.idUsuario).subscribe({
      next: (lista) => {
        this.cajas.set(lista);
        // si su caja está libre, va directo al paso 2
        const libres = lista.filter(c => !c.abierta);
        if (libres.length === 1) {
          this.elegir(libres[0]);
        }
      },
      error: () => this.error.set('No se pudieron cargar las cajas.')
    });
  }

  elegir(c: Caja) {
    this.cajaElegida.set(c);
    this.paso.set(2);
  }

  volver() {
    this.paso.set(1);
    this.error.set('');
  }

  // RN-015: confirma y apertura la caja
  aceptar() {
    const c = this.cajaElegida();
    if (!c) return;

    if (this.fondoInicial === null || this.fondoInicial < 0) {
      this.error.set('Ingrese un fondo de caja válido (S/ 0.00 en adelante).');
      return;
    }

    const u = this.sesion.usuario();
    if (!u) return;

    this.cargando.set(true);
    this.error.set('');

    this.cajaService.aperturar(u.idUsuario, c.numeroCaja, this.fondoInicial).subscribe({
      next: (s: SesionCaja) => {
        this.sesion.abrirCaja(s);
        this.cargando.set(false);
        this.activa.close('aperturada');
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(err?.error?.mensaje ?? 'No se pudo aperturar la caja.');
      }
    });
  }
}
