import { Injectable, signal } from '@angular/core';
import { Usuario } from '../models/usuario';
import { SesionCaja } from '../models/venta';

/**
 * Estado global de la sesión del POS:
 * - quién inició login (HU-01)
 * - si hay caja abierta y con qué sesión (HU-02)
 * Se guarda en localStorage para sobrevivir a recargar la página.
 */
@Injectable({ providedIn: 'root' })
export class Sesionservice {
  private readonly K_USUARIO = 'rosita.usuario';
  private readonly K_SESION = 'rosita.sesionCaja';

  usuario = signal<Usuario | null>(this.leer<Usuario>(this.K_USUARIO));
  sesionCaja = signal<SesionCaja | null>(this.leer<SesionCaja>(this.K_SESION));

  get cajaAbierta(): boolean {
    return this.sesionCaja() !== null;
  }

  iniciarSesion(u: Usuario) {
    this.usuario.set(u);
    localStorage.setItem(this.K_USUARIO, JSON.stringify(u));
  }

  cerrarSesion() {
    this.usuario.set(null);
    this.sesionCaja.set(null);
    localStorage.removeItem(this.K_USUARIO);
    localStorage.removeItem(this.K_SESION);
  }

  abrirCaja(s: SesionCaja) {
    this.sesionCaja.set(s);
    localStorage.setItem(this.K_SESION, JSON.stringify(s));
  }

  // solo cambia el estado local (la caja se cierra desde el backend)
  limpiarCaja() {
    this.sesionCaja.set(null);
    localStorage.removeItem(this.K_SESION);
  }

  private leer<T>(clave: string): T | null {
    try {
      const dato = localStorage.getItem(clave);
      return dato ? (JSON.parse(dato) as T) : null;
    } catch {
      return null;
    }
  }
}
