import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { appsettings } from '../configuracion/appsettings';

// Usuario listado por el Dueño (RNF-05: nunca viaja la clave)
export interface Usuario {
  idUsuario: number;
  nombres: string;
  apellidoPaterno: string;
  idRol: number;
  rol: string;
  estado: string;
}

export interface Rol {
  idRol: number;
  nombreRol: string;
}

export interface UsuarioGuardar {
  idUsuario: number;
  nombres: string;
  apellidoPaterno: string;
  clave: string;
  idRol: number;
}

@Injectable({ providedIn: 'root' })
export class Usuarioservice {
  private http = inject(HttpClient);
  private url = appsettings.apiurl + 'usuarios';

  listar() {
    return this.http.get<Usuario[]>(this.url);
  }

  roles() {
    return this.http.get<Rol[]>(`${this.url}/roles`);
  }

  crear(datos: UsuarioGuardar) {
    return this.http.post<Usuario>(this.url, datos);
  }

  editar(id: number, datos: UsuarioGuardar) {
    return this.http.put<Usuario>(`${this.url}/${id}`, datos);
  }

  // el idQuePide evita que el Dueño se desactive a sí mismo (el backend lo valida)
  cambiarEstado(id: number, idQuePide: number) {
    return this.http.put<Usuario>(`${this.url}/${id}/cambiar-estado`, { idUsuario: idQuePide });
  }
}
