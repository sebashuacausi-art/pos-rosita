import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Usuarioservice, Usuario } from '../../services/usuarioservice';
import { Sesionservice } from '../../services/sesionservice';
import { UsuarioForm } from '../../components/modals/usuario-form/usuario-form';

/**
 * Gestión de usuarios — módulo exclusivo del Dueño (RNF-06).
 * Lista, crea, edita y activa/desactiva usuarios del sistema.
 */
@Component({
  selector: 'app-usuarios',
  imports: [CommonModule, FormsModule],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css'
})
export class Usuarios {
  private usuariosSvc = inject(Usuarioservice);
  private modal = inject(NgbModal);
  sesion = inject(Sesionservice);

  lista = signal<Usuario[]>([]);
  cargando = signal(false);
  error = signal('');
  aviso = signal<{ tipo: 'ok' | 'error'; texto: string } | null>(null);
  buscando = '';

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.cargando.set(true);
    this.error.set('');
    this.usuariosSvc.listar().subscribe({
      next: (usuarios) => {
        this.lista.set(usuarios);
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
        this.error.set('No se pudieron cargar los usuarios.');
      }
    });
  }

  get usuariosFiltrados(): Usuario[] {
    const q = this.buscando.trim().toLowerCase();
    if (!q) return this.lista();
    return this.lista().filter(u =>
      `${u.idUsuario} ${u.nombres} ${u.apellidoPaterno} ${u.rol} ${u.estado}`
        .toLowerCase().includes(q)
    );
  }

  get totalActivos(): number {
    return this.lista().filter(u => u.estado === 'Activo').length;
  }

  nuevo() {
    const ref = this.modal.open(UsuarioForm, { centered: true });
    ref.result.then(() => {
      this.cargar();
      this.mostrarAviso('Usuario registrado correctamente.', 'ok');
    }).catch(() => {});
  }

  editar(u: Usuario) {
    const ref = this.modal.open(UsuarioForm, { centered: true });
    ref.componentInstance.usuario = u;
    ref.result.then(() => {
      this.cargar();
      this.mostrarAviso('Usuario actualizado correctamente.', 'ok');
    }).catch(() => {});
  }

  // activar / desactivar (el backend valida auto-desactivación y "último activo")
  cambiarEstado(u: Usuario) {
    const yo = this.sesion.usuario();
    if (!yo) return;

    const desactivar = u.estado === 'Activo';
    const mensaje = desactivar
      ? `¿Desactivar al usuario ${u.nombres} ${u.apellidoPaterno}? No podrá iniciar sesión.`
      : `¿Reactivar al usuario ${u.nombres} ${u.apellidoPaterno}?`;
    if (!window.confirm(mensaje)) return;

    this.usuariosSvc.cambiarEstado(u.idUsuario, yo.idUsuario).subscribe({
      next: () => {
        this.cargar();
        this.mostrarAviso(desactivar ? 'Usuario desactivado.' : 'Usuario reactivado.', 'ok');
      },
      error: (err) => this.mostrarAviso(
        err?.error?.mensaje ?? 'No se pudo cambiar el estado del usuario.', 'error')
    });
  }

  private mostrarAviso(texto: string, tipo: 'ok' | 'error') {
    this.aviso.set({ tipo, texto });
    setTimeout(() => this.aviso.set(null), 3500);
  }
}
