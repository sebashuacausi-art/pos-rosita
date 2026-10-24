import { Component, inject, Input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Usuarioservice, Rol, Usuario, UsuarioGuardar } from '../../../services/usuarioservice';

/**
 * Formulario de creación / edición de usuarios (solo Dueño).
 * RNF-05: la clave nunca se muestra ni se devuelve desde el servidor;
 * al editar, dejarla vacía conserva la clave actual.
 */
@Component({
  selector: 'app-usuario-form',
  imports: [CommonModule, FormsModule],
  templateUrl: './usuario-form.html',
  styleUrl: './usuario-form.css'
})
export class UsuarioForm {
  private usuariosSvc = inject(Usuarioservice);
  activa = inject(NgbActiveModal);

  // usuario a editar (null = creación); se inyecta con componentInstance
  @Input() usuario: Usuario | null = null;

  // el ID es el login numérico, no se puede editar después de creado
  esEdicion = signal(false);

  roles = signal<Rol[]>([]);
  cargando = signal(false);
  error = signal('');
  mostrarClave = signal(false);

  form: UsuarioGuardar = { idUsuario: 0, nombres: '', apellidoPaterno: '', clave: '', idRol: 0 };

  ngOnInit() {
    const u = this.usuario;
    if (u) {
      this.esEdicion.set(true);
      this.form = {
        idUsuario: u.idUsuario,
        nombres: u.nombres,
        apellidoPaterno: u.apellidoPaterno,
        clave: '',
        idRol: u.idRol
      };
    }

    this.usuariosSvc.roles().subscribe({
      next: (lista) => {
        this.roles.set(lista);
        if (!this.form.idRol && lista.length > 0) {
          // por defecto el rol más común: Vendedor
          const vendedor = lista.find(r => r.nombreRol === 'Vendedor');
          this.form.idRol = (vendedor ?? lista[0]).idRol;
        }
      },
      error: () => this.error.set('No se pudieron cargar los roles.')
    });
  }

  guardar() {
    this.error.set('');

    if (this.form.idUsuario < 1) {
      this.error.set('Ingrese un ID numérico de acceso (ej. 777888).');
      return;
    }
    if (!this.form.nombres.trim() || !this.form.apellidoPaterno.trim()) {
      this.error.set('Nombres y apellido paterno son obligatorios.');
      return;
    }
    if (!this.esEdicion() && !this.form.clave.trim()) {
      this.error.set('Ingrese una clave de al menos 6 caracteres.');
      return;
    }
    if (this.form.clave.trim() && this.form.clave.trim().length < 6) {
      this.error.set('La clave debe tener al menos 6 caracteres.');
      return;
    }
    if (!this.form.idRol) {
      this.error.set('Seleccione el rol del usuario.');
      return;
    }

    this.cargando.set(true);
    const peticion = this.esEdicion()
      ? this.usuariosSvc.editar(this.form.idUsuario, this.form)
      : this.usuariosSvc.crear(this.form);

    peticion.subscribe({
      next: () => {
        this.cargando.set(false);
        this.activa.close('guardado');
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(err?.error?.mensaje ?? 'No se pudo guardar el usuario.');
      }
    });
  }
}
