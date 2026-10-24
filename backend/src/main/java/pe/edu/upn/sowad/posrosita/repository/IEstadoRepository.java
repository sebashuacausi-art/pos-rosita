package pe.edu.upn.sowad.posrosita.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pe.edu.upn.sowad.posrosita.model.EstadoModel;

@Repository
public interface IEstadoRepository extends JpaRepository<EstadoModel, Integer> {
    Optional<EstadoModel> findByEntidadAndNombreEstado(String entidad, String nombreEstado);
}
