package pe.edu.upn.sowad.posrosita.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.upn.sowad.posrosita.model.DesembolsoModel;
import pe.edu.upn.sowad.posrosita.model.EstadoModel;
import pe.edu.upn.sowad.posrosita.model.SesionCajaModel;
import pe.edu.upn.sowad.posrosita.model.VentaModel;
import pe.edu.upn.sowad.posrosita.repository.IDesembolsoRepository;
import pe.edu.upn.sowad.posrosita.repository.IEstadoRepository;
import pe.edu.upn.sowad.posrosita.repository.IPagoRepository;
import pe.edu.upn.sowad.posrosita.repository.ISesionCajaRepository;
import pe.edu.upn.sowad.posrosita.repository.IVentaRepository;

/**
 * Arqueo de caja (RF-35 a RF-38).
 *
 * RNF de cálculo:
 *   efectivo esperado = fondo inicial + ventas en efectivo del día - desembolsos
 *   diferencia        = efectivo contado - efectivo esperado
 *     positiva -> sobrante, negativa -> faltante
 *   Solo se confirma el arqueo cuando la diferencia es S/ 0.00.
 *   Las transferencias NO se cuentan físicamente (se validan al cobro),
 *   pero se muestran informativas agrupadas por canal.
 */
@Service
public class ArqueoService {

    @Autowired
    private ISesionCajaRepository sesionRepository;

    @Autowired
    private IVentaRepository ventaRepository;

    @Autowired
    private IPagoRepository pagoRepository;

    @Autowired
    private IDesembolsoRepository desembolsoRepository;

    @Autowired
    private IEstadoRepository estadoRepository;

    // ------------------------------------------------------------------
    // Cálculo del arqueo de una sesión (solo SU caja, nunca global)
    // ------------------------------------------------------------------
    public Map<String, Object> calcular(Integer idSesion) {
        SesionCajaModel sesion = sesionRepository.findById(idSesion)
                .orElseThrow(() -> new RuntimeException("Sesión de caja no encontrada"));

        BigDecimal fondo = sesion.getFondoInicial();
        BigDecimal efectivoDelDia = pagoRepository.efectivoCobradoEn(idSesion);
        BigDecimal desembolsos = desembolsoRepository.totalDesembolsado(idSesion);
        BigDecimal esperado = fondo.add(efectivoDelDia).subtract(desembolsos);

        // resumen informativo de transferencias, agrupado por canal
        List<Map<String, Object>> transferencias = new ArrayList<>();
        BigDecimal totalTransferencias = BigDecimal.ZERO;
        for (Object[] fila : pagoRepository.transferenciasPorCanalEn(idSesion)) {
            String canal = fila[0] == null ? "Sin canal" : String.valueOf(fila[0]);
            long operaciones = ((Number) fila[1]).longValue();
            BigDecimal monto = (BigDecimal) fila[2];
            Map<String, Object> t = new HashMap<>();
            t.put("canal", canal);
            t.put("operaciones", operaciones);
            t.put("total", monto);
            transferencias.add(t);
            totalTransferencias = totalTransferencias.add(monto);
        }

        // pendientes que bloquean el arqueo (registros internos sin consolidar)
        List<VentaModel> sinComprobante = ventaRepository
                .findBySesionIdSesionAndEstadoNombreEstado(idSesion, "Sin comprobante");
        BigDecimal totalSinComprobante = sinComprobante.stream()
                .map(VentaModel::getTotalVenta)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // HU-14: cantidad de ventas del día (resumen informativo)
        List<VentaModel> ventasDeLaSesion = ventaRepository.findBySesionIdSesion(idSesion);
        long ventasNoAnuladas = ventasDeLaSesion.stream()
                .filter(v -> !"Anulada".equals(v.getEstado().getNombreEstado()))
                .count();

        // detalle de desembolsos del día (informativo)
        List<Map<String, Object>> listaDesembolsos = new ArrayList<>();
        for (DesembolsoModel d : desembolsoRepository.findBySesionIdSesion(idSesion)) {
            Map<String, Object> dm = new HashMap<>();
            dm.put("fecha", d.getFechaDesembolso());
            dm.put("monto", d.getMonto());
            dm.put("descripcion", d.getDescripcion());
            listaDesembolsos.add(dm);
        }

        boolean arqueado = "Arqueada".equals(sesion.getEstado().getNombreEstado())
                || "Cerrada".equals(sesion.getEstado().getNombreEstado());

        Map<String, Object> r = new HashMap<>();
        r.put("idSesion", sesion.getIdSesion());
        r.put("nombreCaja", sesion.getCaja().getNombreCaja());
        r.put("estado", sesion.getEstado().getNombreEstado());
        r.put("fechaApertura", sesion.getFechaApertura());
        r.put("fondoInicial", fondo);
        r.put("efectivoDelDia", efectivoDelDia);
        r.put("desembolsos", desembolsos);
        r.put("efectivoEsperado", esperado);
        r.put("efectivoContado", sesion.getEfectivoContado());
        r.put("diferencia", sesion.getDifEfectivo());
        r.put("observacion", sesion.getObservacion());
        r.put("fechaArqueo", sesion.getFechaArqueo());
        r.put("transferencias", transferencias);
        r.put("totalTransferencias", totalTransferencias);
        r.put("pendientesSinComprobante", sinComprobante.size());
        r.put("totalSinComprobante", totalSinComprobante);
        r.put("cantidadVentas", ventasNoAnuladas);
        r.put("desembolsosDetalle", listaDesembolsos);
        r.put("arqueado", arqueado);
        return r;
    }

    /**
     * Confirma el arqueo (RF-37).
     * Valida:
     *  - que no haya registros internos sin consolidar (RF-36),
     *  - que la diferencia sea exactamente S/ 0.00 ("la caja debe cuadrar").
     */
    @Transactional
    public SesionCajaModel confirmar(Integer idSesion, BigDecimal efectivoContado, String observacion) {
        SesionCajaModel sesion = sesionRepository.findById(idSesion)
                .orElseThrow(() -> new RuntimeException("Sesión de caja no encontrada"));

        String estadoActual = sesion.getEstado().getNombreEstado();
        if (!"Abierta".equals(estadoActual)) {
            throw new RuntimeException("La caja ya tiene el arqueo confirmado");
        }

        // RF-36: bloqueo por registros internos sin consolidar
        List<VentaModel> sinComprobante = ventaRepository
                .findBySesionIdSesionAndEstadoNombreEstado(idSesion, "Sin comprobante");
        if (!sinComprobante.isEmpty()) {
            BigDecimal total = sinComprobante.stream()
                    .map(VentaModel::getTotalVenta)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            throw new RuntimeException(
                    "Debes consolidar las " + sinComprobante.size()
                            + " ventas sin comprobante (Total: S/ " + total
                            + ") antes de realizar el arqueo");
        }

        BigDecimal fondo = sesion.getFondoInicial();
        BigDecimal efectivoDelDia = pagoRepository.efectivoCobradoEn(idSesion);
        BigDecimal desembolsos = desembolsoRepository.totalDesembolsado(idSesion);
        BigDecimal esperado = fondo.add(efectivoDelDia).subtract(desembolsos);

        // RN: la caja debe cuadrar
        BigDecimal diferencia = efectivoContado.subtract(esperado);
        if (diferencia.compareTo(BigDecimal.ZERO) != 0) {
            String tipo = diferencia.compareTo(BigDecimal.ZERO) > 0 ? "sobrante" : "faltante";
            throw new RuntimeException(
                    "La caja debe cuadrar antes de confirmar el arqueo ("
                            + tipo + " de S/ " + diferencia.abs().toPlainString() + ")");
        }

        EstadoModel estadoArqueada = estadoRepository
                .findByEntidadAndNombreEstado("SESION_CAJA", "Arqueada")
                .orElseThrow(() -> new RuntimeException("Estado 'Arqueada' no encontrado"));

        sesion.setEfectivoEsperado(esperado);
        sesion.setEfectivoContado(efectivoContado);
        sesion.setDifEfectivo(diferencia);
        sesion.setObservacion(observacion);
        sesion.setFechaArqueo(LocalDateTime.now());
        sesion.setEstado(estadoArqueada);
        return sesionRepository.save(sesion);
    }

    public Optional<SesionCajaModel> porId(Integer id) {
        return sesionRepository.findById(id);
    }
}
