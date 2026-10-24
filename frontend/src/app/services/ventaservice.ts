import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { appsettings } from '../configuracion/appsettings';
import { ItemCarrito } from '../models/venta';
import { VentaRespuesta } from '../models/venta-respuesta';

// Lo que se envía al backend para cobrar (HU-06 / HU-07)
export interface PagoEnvio {
  metodo: string;              // 'Efectivo' | 'Transferencia'
  canalTransferencia?: string; // 'Yape' | 'Plin' | 'Bancaria'
  numeroOperacion?: string;
  montoRecibido?: number;
  vuelto?: number;
  pagoValidado?: boolean;
}

export interface VentaEnvio {
  idSesion: number;
  idCliente?: number;
  correlativo: string;
  detalles: { idProducto: number; cantidad: number; descuento: number }[];
  pago: PagoEnvio;
}

@Injectable({ providedIn: 'root' })
export class Ventaservice {
  private http = inject(HttpClient);
  private url = appsettings.apiurl + 'ventas';

  // HU-07: registrar la venta (el backend descuenta el stock)
  registrar(venta: VentaEnvio) {
    return this.http.post<VentaRespuesta>(this.url, venta);
  }

  listarPorSesion(idSesion: number) {
    return this.http.get<any[]>(this.url + '/sesion/' + idSesion);
  }

  // arma el payload del carrito para el backend
  armarEnvio(idSesion: number, carrito: ItemCarrito[], pago: PagoEnvio,
             correlativo: string, idCliente?: number): VentaEnvio {
    return {
      idSesion,
      idCliente: idCliente && idCliente > 0 ? idCliente : undefined,
      correlativo,
      detalles: carrito.map(i => ({
        idProducto: i.idProducto,
        cantidad: i.cantidad,
        descuento: i.descuento
      })),
      pago
    };
  }
}
