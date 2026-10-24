import { Component, inject, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ItemCarrito } from '../../../models/venta';
import { Ventaservice, VentaEnvio } from '../../../services/ventaservice';
import { Clienteservice } from '../../../services/clienteservice';
import { Sesionservice } from '../../../services/sesionservice';
import { VentaRespuesta } from '../../../models/venta-respuesta';

/** Métodos de pago válidos (Excel v1.3 y db_rosita_v4: sin Tarjeta/POS) */
type Metodo = 'Efectivo' | 'Transferencia';
type Canal = 'Yape' | 'Plin' | 'Bancaria';

@Component({
  selector: 'app-cobro',
  imports: [CommonModule, FormsModule],
  templateUrl: './cobro.html',
  styleUrl: './cobro.css'
})
export class Cobro {
  private ventaService = inject(Ventaservice);
  private clienteService = inject(Clienteservice);
  private sesion = inject(Sesionservice);
  activa = inject(NgbActiveModal);

  // lo que llega desde NuevaVenta
  carrito = input.required<ItemCarrito[]>();
  total = input.required<number>();
  correlativo = input.required<string>();

  // avanza al modal de boleta
  cobrado = output<VentaRespuesta>();

  metodo: Metodo = 'Efectivo';
  canal: Canal = 'Yape';
  numeroOperacion = '';
  montoRecibido: number | null = null;
  pagoValidado = false;

  // cliente (HU-05)
  tipoDoc = 'DNI';
  numeroDoc = '';
  clienteEncontrado = signal<{ id?: number; nombre: string } | null>(null);
  buscandoCliente = signal(false);

  cargando = signal(false);
  error = signal('');

  get esEfectivo(): boolean {
    return this.metodo === 'Efectivo';
  }

  get vuelto(): number {
    if (!this.esEfectivo || this.montoRecibido === null) return 0;
    const v = this.montoRecibido - this.total();
    return v > 0 ? v : 0;
  }

  get faltaPagar(): boolean {
    return this.esEfectivo && this.montoRecibido !== null && this.montoRecibido < this.total();
  }

  // montos rápidos para el efectivo
  montoRapido(m: number) {
    this.montoRecibido = m;
  }

  // HU-05: buscar cliente por documento
  buscarCliente() {
    if (!this.numeroDoc.trim()) return;
    this.buscandoCliente.set(true);
    this.clienteService.buscar(this.tipoDoc, this.numeroDoc.trim()).subscribe({
      next: (c) => {
        this.buscandoCliente.set(false);
        if (c.encontrado) {
          this.clienteEncontrado.set({ id: c.idCliente, nombre: c.nombre ?? '' });
        } else {
          this.clienteEncontrado.set(null);
        }
      },
      error: () => {
        this.buscandoCliente.set(false);
        this.clienteEncontrado.set(null);
      }
    });
  }

  // RN: validar antes de cobrar
  cobrar() {
    if (this.esEfectivo) {
      if (this.montoRecibido === null || this.montoRecibido < this.total()) {
        this.error.set('El monto recibido es menor que el total a pagar.');
        return;
      }
    } else {
      if (!this.numeroOperacion.trim()) {
        this.error.set('Ingrese el número de operación de la transferencia (RN-023).');
        return;
      }
      if (!this.pagoValidado) {
        this.error.set('Marque que el pago fue validado antes de continuar.');
        return;
      }
    }

    const s = this.sesion.sesionCaja();
    if (!s) {
      this.error.set('No hay una sesión de caja abierta.');
      return;
    }

    this.cargando.set(true);
    this.error.set('');

    const envio: VentaEnvio = this.ventaService.armarEnvio(
      s.idSesion,
      this.carrito(),
      {
        metodo: this.metodo,
        canalTransferencia: this.esEfectivo ? undefined : this.canal,
        numeroOperacion: this.esEfectivo ? undefined : this.numeroOperacion.trim(),
        montoRecibido: this.esEfectivo ? this.montoRecibido ?? 0 : this.total(),
        vuelto: this.esEfectivo ? this.vuelto : 0,
        pagoValidado: this.esEfectivo ? true : this.pagoValidado
      },
      this.correlativo(),
      this.clienteEncontrado()?.id
    );

    this.ventaService.registrar(envio).subscribe({
      next: (resp) => {
        this.cargando.set(false);
        this.cobrado.emit(resp);
        this.activa.close(resp);
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(err?.error?.mensaje ?? 'No se pudo registrar la venta.');
      }
    });
  }
}
