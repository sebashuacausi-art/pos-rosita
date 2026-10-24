package pe.edu.upn.sowad.posrosita.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pe.edu.upn.sowad.posrosita.model.DetalleVentaModel;

@Repository
public interface IDetalleVentaRepository extends JpaRepository<DetalleVentaModel, Integer> {
    List<DetalleVentaModel> findByVentaIdVenta(Integer idVenta);
}
