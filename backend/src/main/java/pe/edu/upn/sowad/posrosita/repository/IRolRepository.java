package pe.edu.upn.sowad.posrosita.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pe.edu.upn.sowad.posrosita.model.RolModel;

@Repository
public interface IRolRepository extends JpaRepository<RolModel, Integer> {
    Optional<RolModel> findByNombreRol(String nombreRol);
}
