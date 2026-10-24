// HU-01: sesión del usuario que inició login
export interface Usuario {
  idUsuario: number;
  nombres: string;
  apellidoPaterno: string;
  // "Dueño" o "Vendedor"
  rol: string;
}
