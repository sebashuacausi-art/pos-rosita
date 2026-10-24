import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { appsettings } from '../configuracion/appsettings';
import { Usuario } from '../models/usuario';

@Injectable({ providedIn: 'root' })
export class Authservice {
  private http = inject(HttpClient);
  private url = appsettings.apiurl + 'auth';

  // HU-01: login por ID numérico (ej. 123456 / 654321)
  login(usuario: string, clave: string) {
    return this.http.post<Usuario>(this.url + '/login', { usuario, clave });
  }
}
