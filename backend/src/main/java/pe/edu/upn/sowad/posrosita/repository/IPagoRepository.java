package pe.edu.upn.sowad.posrosita.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pe.edu.upn.sowad.posrosita.model.PagoModel;

@Repository
public interface IPagoRepository extends JpaRepository<PagoModel, Integer> {
    List<PagoModel> findByVentaIdVenta(Integer idVenta);

    // RN-023: unicidad del numero de operacion dentro del mismo canal
    Optional<PagoModel> findByNumeroOperacionAndCanalTransferencia(String numeroOperacion, String canal);
    boolean existsByNumeroOperacionAndCanalTransferencia(String numeroOperacion, String canal);

    /**
     * Arqueo: total cobrado en EFECTIVO dentro de una sesión de caja.
     * Es el que entra en el cálculo del "efectivo esperado".
     */
    @Query("""
            SELECT COALESCE(SUM(p.monto), 0)
            FROM PagoModel p
            JOIN p.venta v
            WHERE v.sesion.idSesion = :idSesion
              AND p.metodo.metodoPago = 'Efectivo'
              AND v.estado.nombreEstado <> 'Anulada'
            """)
    BigDecimal efectivoCobradoEn(@Param("idSesion") Integer idSesion);

    /**
     * Arqueo: transferencias por canal, con su cantidad de operaciones y total.
     * (RNF: cada canal aparece con su propio total)
     */
    @Query("""
            SELECT p.canalTransferencia, COUNT(p), COALESCE(SUM(p.monto), 0)
            FROM PagoModel p
            JOIN p.venta v
            WHERE v.sesion.idSesion = :idSesion
              AND p.metodo.metodoPago = 'Transferencia'
              AND v.estado.nombreEstado <> 'Anulada'
            GROUP BY p.canalTransferencia
            ORDER BY p.canalTransferencia
            """)
    List<Object[]> transferenciasPorCanalEn(@Param("idSesion") Integer idSesion);

    /** X out: todos los pagos de la sesión (para mostrar método por venta). */
    @Query("""
            SELECT p
            FROM PagoModel p
            JOIN p.venta v
            WHERE v.sesion.idSesion = :idSesion
            """)
    List<PagoModel> pagosDeSesion(@Param("idSesion") Integer idSesion);
}
