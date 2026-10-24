import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { appsettings } from '../configuracion/appsettings';

// Fila del checklist de ventas del día (RF-42)
export interface VentaChecklist {
  idVenta: number;
  correlativo: string;
  fecha: string;
  total: number;
  estado: string;
  metodo: string;
}

// Cierre previo de la caja (RF-44, solo lectura)
export interface CierreAnterior {
  idSesion: number;
  fechaCierre: string | null;
  fondoInicial: number;
  fondoFinal: number | null;
  difFondo: number | null;
  cajero: string;
  efectivo: number;
  transferencias: number;
}

// Estado completo del cierre (RF-41 a RF-44)
export interface EstadoCierre {
  idSesion: number;
  nombreCaja: string;
  numeroCaja: number;
  cajero: string;
  estado: string;
  cerrada: boolean;
  bloqueos: string[];
  checklist: VentaChecklist[];
  fondoInicial: number;
  efectivoDelDia: number;
  transferencias: number;
  desembolsos: number;
  fondoEsperado: number;
  fechaCierre?: string | null;
  fondoFinal?: number | null;
  difFondo?: number | null;
  anteriores: CierreAnterior[];
}

@Injectable({ providedIn: 'root' })
export class Cierreservice {
  private http = inject(HttpClient);
  private url = appsettings.apiurl + 'cierre';

  // RF-41: pendientes + checklist + resumen + X out + anteriores
  detalle(idSesion: number) {
    return this.http.get<EstadoCierre>(`${this.url}/${idSesion}`);
  }

  // RF-43: cerrar la caja con el fondo final (exige cuadre exacto)
  cerrar(idSesion: number, fondoFinal: number) {
    return this.http.post<EstadoCierre & { cerrado: boolean }>(
      `${this.url}/${idSesion}/cerrar`,
      { fondoFinal }
    );
  }

  // RF-44: últimos 5 cierres de una caja
  anteriores(idCaja: number) {
    return this.http.get<CierreAnterior[]>(`${this.url}/caja/${idCaja}/anteriores`);
  }
}
