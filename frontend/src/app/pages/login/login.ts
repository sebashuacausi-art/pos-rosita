import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Authservice } from '../../services/authservice';
import { Sesionservice } from '../../services/sesionservice';
import { Cajaservice } from '../../services/cajaservice';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  private auth = inject(Authservice);
  private sesion = inject(Sesionservice);
  private cajaService = inject(Cajaservice);
  private router = inject(Router);

  usuario = '';
  clave = '';
  cargando = signal(false);
  error = signal('');

  // HU-01: el ID es numérico (ej. 123456 vendedor / 654321 dueño)
  ingresar() {
    if (!this.usuario.trim() || !this.clave.trim()) {
      this.error.set('Ingrese su ID y su clave.');
      return;
    }

    this.cargando.set(true);
    this.error.set('');

    this.auth.login(this.usuario.trim(), this.clave).subscribe({
      next: (u) => {
        // la sesión de caja se restaura desde la BASE, no del navegador:
        // si este usuario ya tenía una caja abierta, continúa con ella.
        this.cajaService.sesionDeUsuario(u.idUsuario).subscribe({
          next: (s) => {
            if (s.abierta && s.idSesion) {
              this.sesion.abrirCaja({
                idSesion: s.idSesion!,
                idCaja: s.idCaja!,
                nombreCaja: s.nombreCaja!,
                fondoInicial: s.fondoInicial!,
                fechaApertura: String(s.fechaApertura ?? '')
              });
            } else {
              this.sesion.limpiarCaja();
            }
            this.sesion.iniciarSesion(u);
            this.cargando.set(false);
            this.router.navigate(['/panel']);
          },
          error: () => {
            // sin datos de caja igual deja entrar; ofrecerá la apertura
            this.sesion.limpiarCaja();
            this.sesion.iniciarSesion(u);
            this.cargando.set(false);
            this.router.navigate(['/panel']);
          }
        });
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(
          err?.error?.mensaje ?? 'ID o clave incorrectos.'
        );
      }
    });
  }
}
