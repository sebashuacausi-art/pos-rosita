import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { Sesionservice } from '../../services/sesionservice';
import { Cierreservice, EstadoCierre } from '../../services/cierreservice';

/**
 * Cierre de caja y X out (RF-41 a RF-44).
 * Paso 1: checklist de ventas del día ("Marcar todas").
 * Paso 2: cuadre del fondo final -> cierra e "imprime" el X out.
 * Si la caja ya cerró: solo reimpresión del X out + últimos 5 cierres.
 */
@Component({
  selector: 'app-cierre',
  imports: [CommonModule, FormsModule],
  templateUrl: './cierre.html',
  styleUrl: './cierre.css'
})
export class Cierre implements OnInit {
  sesion = inject(Sesionservice);
  private cierreService = inject(Cierreservice);

  estado = signal<EstadoCierre | null>(null);
  cargando = signal(false);
  error = signal('');

  paso = signal<1 | 2>(1);
  marcadas = signal<Set<number>>(new Set());
  fondoIngresado = signal('');
  confirmando = signal(false);
  aviso = signal<{ texto: string; tipo: 'ok' | 'error' } | null>(null);
  mostrarAnteriores = signal(false);

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    const s = this.sesion.sesionCaja();
    if (!s) {
      this.error.set('No hay una caja activa para cerrar.');
      return;
    }
    this.cargando.set(true);
    this.error.set('');
    this.cierreService.detalle(s.idSesion).subscribe({
      next: (r) => {
        this.estado.set(r);
        this.cargando.set(false);
        // RF-44: si ya cerró, se queda en la pantalla de X out
        if (r.cerrada) {
          this.paso.set(2);
        }
      },
      error: (err) => {
        this.error.set(err?.error?.mensaje ?? 'No se pudo cargar el cierre.');
        this.cargando.set(false);
      }
    });
  }

  // ---------- Paso 1: checklist (RF-42) ----------
  get ventas(): EstadoCierre['checklist'] {
    return this.estado()?.checklist ?? [];
  }

  get todasMarcadas(): boolean {
    const v = this.ventas;
    return v.length === 0 || v.every(x => this.marcadas().has(x.idVenta));
  }

  get marcadasCount(): number {
    return this.marcadas().size;
  }

  alternar(id: number) {
    this.marcadas.update(set => {
      const n = new Set(set);
      if (n.has(id)) n.delete(id); else n.add(id);
      return n;
    });
    this.aviso.set(null);
  }

  marcarTodas(event: Event) {
    const marcado = (event.target as HTMLInputElement).checked;
    this.marcadas.set(marcado ? new Set(this.ventas.map(v => v.idVenta)) : new Set());
    this.aviso.set(null);
  }

  get bloqueos(): string[] {
    return this.estado()?.bloqueos ?? [];
  }

  aceptarPaso1() {
    if (!this.todasMarcadas) {
      this.aviso.set({ texto: 'Marque todas las ventas del día.', tipo: 'error' });
      return;
    }
    if (this.bloqueos.length > 0) {
      this.aviso.set({ texto: 'Completa los pasos pendientes antes de continuar.', tipo: 'error' });
      return;
    }
    this.aviso.set(null);
    this.paso.set(2);
  }

  volverPaso1() {
    this.paso.set(1);
    this.aviso.set(null);
  }

  // ---------- Paso 2: fondo final (RF-43) ----------
  get fondoEsperado(): number {
    return this.estado()?.fondoEsperado ?? 0;
  }

  get diferencia(): number | null {
    const t = this.fondoIngresado().trim();
    if (t === '' || Number.isNaN(Number(t))) return null;
    return Math.round((Number(t) - this.fondoEsperado) * 100) / 100;
  }

  cerrarCaja() {
    const est = this.estado();
    if (!est || est.cerrada) return;

    const t = this.fondoIngresado().trim();
    if (t === '' || Number.isNaN(Number(t)) || Number(t) < 0) {
      this.aviso.set({ texto: 'Ingresa un fondo de caja válido.', tipo: 'error' });
      return;
    }
    if (this.diferencia !== 0) {
      this.aviso.set({
        texto: `El fondo no cuadra. Diferencia: S/ ${Math.abs(this.diferencia!).toFixed(2)}.`,
        tipo: 'error'
      });
      return;
    }

    this.confirmando.set(true);
    this.aviso.set(null);
    this.cierreService.cerrar(est.idSesion, Number(t)).subscribe({
      next: (r) => {
        this.estado.set(r);
        this.confirmando.set(false);
        // X out se "imprime" automáticamente al cerrar (RF-44)
        this.imprimirXOut();
        this.sesion.limpiarCaja();
        this.aviso.set({ texto: 'Caja cerrada. X out generado.', tipo: 'ok' });
      },
      error: (err) => {
        this.confirmando.set(false);
        this.aviso.set({
          texto: err?.error?.mensaje ?? 'No se pudo cerrar la caja.',
          tipo: 'error'
        });
      }
    });
  }

  // ---------- X out (RF-44) ----------
  imprimirXOut() {
    const est = this.estado();
    if (!est) return;

    const filas = est.checklist
      .filter(v => v.estado !== 'En espera' && v.estado !== 'Anulada')
      .map(v => `<tr>
        <td>${v.correlativo}</td><td>${v.metodo}</td>
        <td class="der">S/ ${v.total.toFixed(2)}</td></tr>`).join('');

    const html = `<!doctype html><html><head><meta charset="utf-8">
<title>X out · ${est.nombreCaja}</title>
<style>
 body{font-family:'Courier New',monospace;font-size:13px;color:#1E2A4A;padding:18px;max-width:340px;margin:auto}
 h1{font-size:16px;text-align:center;margin:0 0 2px;text-transform:uppercase}
 .sub{text-align:center;font-size:11px;margin-bottom:10px}
 table{width:100%;border-collapse:collapse;margin:8px 0}
 td,th{padding:3px 2px;font-size:12px;border-bottom:1px dashed #cbd5e1}
 th{text-align:left;font-size:11px;text-transform:uppercase}
 .der{text-align:right}
 .tot{display:flex;justify-content:space-between;margin:3px 0}
 .gran{font-weight:bold;font-size:14px;border-top:2px solid #1E2A4A;padding-top:5px;margin-top:5px}
 .pie{text-align:center;font-size:10px;margin-top:12px}
</style></head><body>
<h1>X out · Cierre de caja</h1>
<div class="sub">${est.nombreCaja} · ${est.cajero}<br>${est.fechaCierre ?? ''}</div>
<table><thead><tr><th>Venta</th><th>Método</th><th class="der">Total</th></tr></thead>
<tbody>${filas || '<tr><td colspan="3">Sin ventas</td></tr>'}</tbody></table>
<div class="tot"><span>Fondo inicial</span><span>S/ ${est.fondoInicial.toFixed(2)}</span></div>
<div class="tot"><span>Efectivo</span><span>S/ ${est.efectivoDelDia.toFixed(2)}</span></div>
<div class="tot"><span>Transferencias</span><span>S/ ${est.transferencias.toFixed(2)}</span></div>
<div class="tot"><span>Desembolsos</span><span>− S/ ${est.desembolsos.toFixed(2)}</span></div>
<div class="tot gran"><span>Fondo final</span><span>S/ ${(est.fondoFinal ?? est.fondoEsperado).toFixed(2)}</span></div>
<div class="pie">Documento de uso interno · Librería Bazar Rosita<br>No constituye comprobante de venta</div>
</body></html>`;

    const win = window.open('', '_blank', 'width=420,height=640');
    if (win) {
      win.document.write(html);
      win.document.close();
      win.focus();
      setTimeout(() => win.print(), 250);
    }
  }

  alternarAnteriores() {
    this.mostrarAnteriores.update(v => !v);
  }

  soles(n: number): string {
    return `S/ ${Math.abs(n).toFixed(2)}`;
  }
}
