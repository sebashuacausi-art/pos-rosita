import { Component, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { VentaRespuesta } from '../../../models/venta-respuesta';
import { Sesionservice } from '../../../services/sesionservice';

@Component({
  selector: 'app-boleta',
  imports: [CommonModule],
  templateUrl: './boleta.html',
  styleUrl: './boleta.css'
})
export class Boleta {
  activa = inject(NgbActiveModal);
  sesion = inject(Sesionservice);

  venta = input.required<VentaRespuesta>();
  montoRecibido = input<number>(0);
  vuelto = input<number>(0);
  metodoPago = input<string>('Efectivo');
  canalPago = input<string>('');
  numeroOperacion = input<string>('');

  // fecha de emisión del comprobante
  readonly hoy = new Date().toLocaleString('es-PE', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });

  // HU-08: impresión del comprobante
  imprimir() {
    window.print();
  }
}
