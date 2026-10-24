// HU-03: producto del catálogo con su stock disponible
export interface Producto {
  idProducto: number;
  codigoProducto: string;
  nombreProducto: string;
  descripcion: string;
  precioUnitario: number;
  idCategoria: number;
  categoria: string;
  // cantidad_actual - cantidad_reservada
  disponible: number;
  cantidadReservada: number;
  stockMinimo: number;
}
