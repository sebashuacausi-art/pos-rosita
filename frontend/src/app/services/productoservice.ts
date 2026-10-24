import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { appsettings } from '../configuracion/appsettings';
import { Producto } from '../models/producto';

@Injectable({ providedIn: 'root' })
export class Productoservice {
  private http = inject(HttpClient);
  private url = appsettings.apiurl + 'productos';

  // HU-03: catálogo completo con stock disponible
  listar() {
    return this.http.get<Producto[]>(this.url);
  }

  // HU-03: búsqueda por nombre o SKU
  buscar(texto: string) {
    return this.http.get<Producto[]>(this.url + '/buscar', {
      params: { q: texto }
    });
  }
}
