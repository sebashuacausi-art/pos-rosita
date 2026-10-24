import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { appsettings } from '../configuracion/appsettings';

// Respuesta del cálculo en vivo del arqueo (RF-35 / RF-36)
export interface ArqueoCalculo {
  idSesion: number;
  nombreCaja: string;
  estado: string;
  fechaApertura: string;
  fondoInicial: number;
  efectivoDelDia: number;
  desembolsos: number;
  efectivoEsperado: number;
  efectivoContado: number | null;
  diferencia: number | null;
  observacion: string | null;
  fechaArqueo: string | null;
  transferencias: { canal: string; operaciones: number; total: number }[];
  totalTransferencias: number;
  pendientesSinComprobante: number;
  totalSinComprobante: number;
  cantidadVentas: number;
  desembolsosDetalle: { fecha: string; monto: number; descripcion: string }[];
  arqueado: boolean;
}

@Injectable({ providedIn: 'root' })
export class Arqueoservice {
  private http = inject(HttpClient);
  private url = appsettings.apiurl + 'arqueo';

  // cálculo del arqueo de la sesión de caja actual
  calcular(idSesion: number) {
    return this.http.get<ArqueoCalculo>(`${this.url}/${idSesion}`);
  }

  // RF-37: confirmar solo si la caja cuadra (diferencia S/ 0.00)
  confirmar(idSesion: number, efectivoContado: number, observacion: string) {
    return this.http.post<{
      arqueado: boolean;
      estado: string;
      efectivoEsperado: number;
      efectivoContado: number;
      difEfectivo: number;
      fechaArqueo: string;
    }>(`${this.url}/${idSesion}/confirmar`, { efectivoContado, observacion });
  }
}
