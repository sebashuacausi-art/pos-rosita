// Lo que el backend responde al registrar la venta (HU-07)
export interface DetalleVentaRespuesta {
  nombreProducto: string;
  codigoProducto: string;
  cantidad: number;
  precioUnitario: number;
  descuento: number;
  subtotal: number;
}

export interface VentaRespuesta {
  idVenta: number;
  correlativo: string;
  estado: string;
  subtotal: number;
  descuentoTotal: number;
  total: number;
  tieneComprobante: boolean;
  detalles: DetalleVentaRespuesta[];
}
