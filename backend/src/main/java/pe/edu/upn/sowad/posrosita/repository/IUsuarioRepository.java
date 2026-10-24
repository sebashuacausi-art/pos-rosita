package pe.edu.upn.sowad.posrosita.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pe.edu.upn.sowad.posrosita.model.UsuarioModel;

@Repository
public interface IUsuarioRepository extends JpaRepository<UsuarioModel, Integer> {
    // HU-01: login por ID numerico (el campo "usuario" guarda el ID, ej. 654321)
    Optional<UsuarioModel> findByUsuario(String usuario);
}
