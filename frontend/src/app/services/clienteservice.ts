import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { appsettings } from '../configuracion/appsettings';
import { Cliente } from '../models/venta';

@Injectable({ providedIn: 'root' })
export class Clienteservice {
  private http = inject(HttpClient);
  private url = appsettings.apiurl + 'clientes';

  // HU-05: autocompletado de cliente por documento
  buscar(tipo: string, numero: string) {
    return this.http.get<Cliente>(this.url + '/buscar', {
      params: { tipo, numero }
    });
  }
}
