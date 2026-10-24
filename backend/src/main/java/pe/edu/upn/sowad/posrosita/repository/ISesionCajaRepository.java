package pe.edu.upn.sowad.posrosita.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pe.edu.upn.sowad.posrosita.model.SesionCajaModel;

@Repository
public interface ISesionCajaRepository extends JpaRepository<SesionCajaModel, Integer> {

    // RN-015: buscar la sesion abierta de una caja
    Optional<SesionCajaModel> findByCajaIdCajaAndEstadoNombreEstado(Integer idCaja, String nombreEstado);

    // RNF (multi-caja): cada usuario opera sobre UNA sola caja por sesion,
    // por eso se busca la sesion abierta que pertenece al usuario.
    Optional<SesionCajaModel> findByUsuarioIdUsuarioAndEstadoNombreEstado(Integer idUsuario, String nombreEstado);

    /**
     * Sesión ACTIVA = 'Abierta' o 'Arqueada' (aun no cerrada).
     * Una caja arqueada sigue ocupada hasta su cierre.
     */
    Optional<SesionCajaModel> findByCajaIdCajaAndEstadoNombreEstadoIn(Integer idCaja, List<String> estados);

    Optional<SesionCajaModel> findByUsuarioIdUsuarioAndEstadoNombreEstadoIn(Integer idUsuario, List<String> estados);

    List<SesionCajaModel> findByEstadoNombreEstado(String estado);
    List<SesionCajaModel> findByCajaIdCaja(Integer idCaja);

    // RF-44: últimos cierres de una caja (solo lectura)
    List<SesionCajaModel> findByCajaIdCajaAndEstadoNombreEstadoOrderByFechaCierreDesc(
            Integer idCaja, String nombreEstado);

    /**
     * RNF: "Una caja cerrada en el dia no puede reabrirse".
     * Devuelve true si la caja ya tuvo un cierre registrado desde la fecha indicada.
     */
    @Query("""
            SELECT COUNT(s) > 0
            FROM SesionCajaModel s
            WHERE s.caja.idCaja = :idCaja
              AND s.estado.nombreEstado = 'Cerrada'
              AND s.fechaCierre >= :desde
            """)
    boolean fueCerradaDesde(@Param("idCaja") Integer idCaja, @Param("desde") LocalDateTime desde);
}
