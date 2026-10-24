import { Component, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { VentaRespuesta } from '../../../models/venta-respuesta';

@Component({
  selector: 'app-venta-registrada',
  imports: [CommonModule],
  templateUrl: './venta-registrada.html',
  styleUrl: './venta-registrada.css'
})
export class VentaRegistrada {
  activa = inject(NgbActiveModal);

  venta = input.required<VentaRespuesta>();

  nuevaVenta() {
    this.activa.close('nueva');
  }
}
