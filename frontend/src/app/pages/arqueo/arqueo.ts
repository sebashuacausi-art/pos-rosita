import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { Sesionservice } from '../../services/sesionservice';
import { Arqueoservice, ArqueoCalculo } from '../../services/arqueoservice';

/**
 * Arqueo de caja (RF-35 a RF-38).
 * Compara el efectivo físico con el esperado y muestra el total
 * informativo de transferencias por canal.
 * Solo se confirma cuando la diferencia es S/ 0.00.
 */
@Component({
  selector: 'app-arqueo',
  imports: [CommonModule, FormsModule],
  templateUrl: './arqueo.html',
  styleUrl: './arqueo.css'
})
export class Arqueo implements OnInit {
  sesion = inject(Sesionservice);
  private arqueoService = inject(Arqueoservice);

  calculo = signal<ArqueoCalculo | null>(null);
  cargando = signal(false);
  error = signal('');

  efectivoContado = signal('');
  observacion = signal('');
  confirmando = signal(false);
  confirmado = signal(false);
  aviso = signal<{ texto: string; tipo: 'ok' | 'error' } | null>(null);

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    const s = this.sesion.sesionCaja();
    if (!s) {
      this.error.set('No hay una caja abierta para hacer el arqueo.');
      return;
    }
    this.cargando.set(true);
    this.error.set('');
    this.arqueoService.calcular(s.idSesion).subscribe({
      next: (r) => {
        this.calculo.set(r);
        this.confirmado.set(r.arqueado);
        // si ya estaba arqueado, se recupera el conteo confirmado
        if (r.arqueado && r.efectivoContado !== null) {
          this.efectivoContado.set(String(r.efectivoContado));
        }
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.mensaje ?? 'No se pudo calcular el arqueo.');
        this.cargando.set(false);
      }
    });
  }

  // diferencia en vivo: contado - esperado
  get diferencia(): number | null {
    const texto = this.efectivoContado().trim();
    if (texto === '') return null;
    const val = Number(texto);
    if (Number.isNaN(val)) return null;
    const calc = this.calculo();
    if (!calc) return null;
    return Math.round((val - calc.efectivoEsperado) * 100) / 100;
  }

  get cuadra(): boolean {
    return this.diferencia === 0;
  }

  get bloqueado(): boolean {
    const c = this.calculo();
    return !c || c.pendientesSinComprobante > 0;
  }

  confirmar() {
    const texto = this.efectivoContado().trim();
    if (texto === '' || Number.isNaN(Number(texto))) {
      this.aviso.set({ texto: 'Ingresa el efectivo físico contado.', tipo: 'error' });
      return;
    }
    if (!this.cuadra) {
      this.aviso.set({ texto: 'La caja debe cuadrar antes de confirmar el arqueo.', tipo: 'error' });
      return;
    }
    const s = this.sesion.sesionCaja();
    if (!s) return;

    this.confirmando.set(true);
    this.aviso.set(null);
    this.arqueoService.confirmar(s.idSesion, Number(texto), this.observacion().trim()).subscribe({
      next: () => {
        this.confirmado.set(true);
        this.confirmando.set(false);
        this.aviso.set({ texto: 'Arqueo confirmado. Caja habilitada para desembolso y cierre.', tipo: 'ok' });
        this.cargar();
      },
      error: (err) => {
        this.confirmando.set(false);
        this.aviso.set({
          texto: err?.error?.mensaje ?? 'No se pudo confirmar el arqueo.',
          tipo: 'error'
        });
      }
    });
  }

  // formato S/ 0.00
  soles(n: number): string {
    return `S/ ${Math.abs(n).toFixed(2)}`;
  }

  abs(n: number): number {
    return Math.abs(n);
  }

  canalBonito(canal: string): string {
    const c = (canal || '').toLowerCase();
    if (c === 'yape') return 'Yape';
    if (c === 'plin') return 'Plin';
    if (c === 'bancaria' || c === 'transferencia bancaria') return 'Transferencia bancaria';
    return canal || 'Sin canal';
  }
}
