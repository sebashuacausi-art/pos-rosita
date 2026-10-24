// Item del carrito de compra (UI)
export interface ItemCarrito {
  idProducto: number;
  codigoProducto: string;
  nombreProducto: string;
  precioUnitario: number;
  cantidad: number;
  descuento: number;
  // stock disponible en el momento de agregar
  disponible: number;
}

export interface Caja {
  idCaja: number;
  numeroCaja: number;
  nombreCaja: string;
  abierta: boolean;
}

export interface SesionCaja {
  idSesion: number;
  idCaja: number;
  nombreCaja: string;
  fondoInicial: number;
  fechaApertura: string;
}

// HU-05: cliente buscado por documento
export interface Cliente {
  encontrado: boolean;
  idCliente?: number;
  tipoDocumento?: string;
  numeroDocumento?: string;
  nombre?: string;
  direccion?: string;
}
