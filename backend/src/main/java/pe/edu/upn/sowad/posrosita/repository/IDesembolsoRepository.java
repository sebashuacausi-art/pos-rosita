package pe.edu.upn.sowad.posrosita.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pe.edu.upn.sowad.posrosita.model.DesembolsoModel;

@Repository
public interface IDesembolsoRepository extends JpaRepository<DesembolsoModel, Integer> {

    List<DesembolsoModel> findBySesionIdSesion(Integer idSesion);

    /**
     * RNF cálculo del arqueo:
     * "efectivo esperado = fondo inicial + ventas en efectivo del día - desembolsos"
     */
    @Query(value = "SELECT COALESCE(SUM(monto), 0) FROM desembolso WHERE id_sesion = :idSesion", nativeQuery = true)
    BigDecimal totalDesembolsado(@Param("idSesion") Integer idSesion);
}
