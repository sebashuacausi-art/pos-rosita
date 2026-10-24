import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { appsettings } from '../configuracion/appsettings';
import { Caja, SesionCaja } from '../models/venta';

@Injectable({ providedIn: 'root' })
export class Cajaservice {
  private http = inject(HttpClient);
  private url = appsettings.apiurl + 'cajas';

  // todas las cajas (uso interno / administración)
  listar() {
    return this.http.get<Caja[]>(this.url);
  }

  /**
   * Cajas que ESTE usuario puede operar según su rol.
   * RNF: la Caja 1 es del Dueño; las 2, 3 y 4 de los Vendedores.
   */
  cajasDeUsuario(idUsuario: number) {
    return this.http.get<Caja[]>(`${this.url}/usuario/${idUsuario}`);
  }

  /**
   * Sesión de caja real del usuario, leída desde la base al iniciar sesión
   * (la fuente de verdad es la base, no el navegador).
   */
  sesionDeUsuario(idUsuario: number) {
    return this.http.get<{ abierta: boolean } & Partial<SesionCaja>>(
      `${this.url}/usuario/${idUsuario}/sesion`
    );
  }

  // HU-02: apertura de caja con fondo inicial
  aperturar(idUsuario: number, numeroCaja: number, fondoInicial: number) {
    return this.http.post<{ abierta: boolean } & SesionCaja>(this.url + '/aperturar', {
      idUsuario,
      numeroCaja,
      fondoInicial
    });
  }

  // estado de la sesión de una caja
  abierta(numeroCaja: number) {
    return this.http.get<{ abierta: boolean } & Partial<SesionCaja>>(
      this.url + '/' + numeroCaja + '/abierta'
    );
  }
}
