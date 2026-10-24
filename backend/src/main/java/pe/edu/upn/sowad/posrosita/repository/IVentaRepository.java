package pe.edu.upn.sowad.posrosita.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pe.edu.upn.sowad.posrosita.model.VentaModel;

@Repository
public interface IVentaRepository extends JpaRepository<VentaModel, Integer> {
    Optional<VentaModel> findByCorrelativoVenta(String correlativo);
    List<VentaModel> findBySesionIdSesion(Integer idSesion);
    List<VentaModel> findBySesionIdSesionAndEstadoNombreEstado(Integer idSesion, String nombreEstado);
}
