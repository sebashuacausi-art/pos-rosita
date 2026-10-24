import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Principal } from './pages/principal/principal';
import { NuevaVenta } from './pages/nuevaventa/nuevaventa';
import { Arqueo } from './pages/arqueo/arqueo';
import { Cierre } from './pages/cierre/cierre';
import { Usuarios } from './pages/usuarios/usuarios';

export const routes: Routes = [
  { path: '', component: Login },
  {
    path: 'panel',
    component: Principal,
    children: [
      // ruta por defecto del panel: el POS
      { path: '', component: NuevaVenta },
      { path: 'nueva-venta', component: NuevaVenta },
      { path: 'arqueo', component: Arqueo },
      { path: 'cierre', component: Cierre },
      { path: 'usuarios', component: Usuarios }
    ]
  },
  { path: '**', redirectTo: '' }
];
