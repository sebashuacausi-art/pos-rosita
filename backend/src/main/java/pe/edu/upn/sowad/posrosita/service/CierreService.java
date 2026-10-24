package pe.edu.upn.sowad.posrosita.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.upn.sowad.posrosita.model.EstadoModel;
import pe.edu.upn.sowad.posrosita.model.PagoModel;
import pe.edu.upn.sowad.posrosita.model.SesionCajaModel;
import pe.edu.upn.sowad.posrosita.model.VentaModel;
import pe.edu.upn.sowad.posrosita.repository.IDesembolsoRepository;
import pe.edu.upn.sowad.posrosita.repository.IEstadoRepository;
import pe.edu.upn.sowad.posrosita.repository.IPagoRepository;
import pe.edu.upn.sowad.posrosita.repository.ISesionCajaRepository;
import pe.edu.upn.sowad.posrosita.repository.IVentaRepository;

/**
 * Cierre de caja y X out (RF-41 a RF-44).
 *
 * RF-41: lista los pendientes (ventas sin consolidar, ventas en espera,
 *        arqueo sin confirmar) y bloquea el avance del cierre.
 * RF-42: checklist de ventas del día con "Marcar todas" (lado cliente).
 * RF-43: valida que el fondo final coincida con
 *        fondo inicial + efectivo del día - desembolsos.
 * RF-44: un único cierre por día y por caja; al cerrar queda la info
 *        del X out y los últimos 5 cierres son de solo lectura.
 */
@Service
public class CierreService {

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

    private static final java.time.format.DateTimeFormatter F =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ------------------------------------------------------------------
    // RF-41: estado del cierre (pendientes + resumen + X out + anteriores)
    // ------------------------------------------------------------------
    public Map<String, Object> detalle(Integer idSesion) {
        SesionCajaModel sesion = sesionRepository.findById(idSesion)
                .orElseThrow(() -> new RuntimeException("Sesión de caja no encontrada"));

        boolean cerrada = "Cerrada".equals(sesion.getEstado().getNombreEstado());
        boolean arqueada = "Arqueada".equals(sesion.getEstado().getNombreEstado()) || cerrada;

        List<VentaModel> ventas = ventaRepository.findBySesionIdSesion(idSesion);

        // ---- RF-41: pendientes que bloquean el cierre ----
        List<String> bloqueos = new ArrayList<>();
        long sinComprobante = ventas.stream()
                .filter(v -> "Sin comprobante".equals(v.getEstado().getNombreEstado()))
                .count();
        long enEspera = ventas.stream()
                .filter(v -> "En espera".equals(v.getEstado().getNombreEstado()))
                .count();
        if (sinComprobante > 0) {
            bloqueos.add("Consolidar las ventas sin comprobante (" + sinComprobante + ")");
        }
        if (enEspera > 0) {
            bloqueos.add("Resolver las ventas en espera (" + enEspera + ")");
        }
        if (!arqueada) {
            bloqueos.add("Confirmar el arqueo de caja");
        }

        // ---- resumen económico de la caja ----
        BigDecimal fondoInicial = sesion.getFondoInicial();
        BigDecimal efectivoDelDia = pagoRepository.efectivoCobradoEn(idSesion);
        BigDecimal desembolsos = desembolsoRepository.totalDesembolsado(idSesion);
        BigDecimal totalTransferencias = pagoRepository.transferenciasPorCanalEn(idSesion).stream()
                .map(f -> (BigDecimal) f[2])
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal fondoEsperado = fondoInicial.add(efectivoDelDia).subtract(desembolsos);

        // ---- RF-42: checklist de ventas del día ----
        List<Map<String, Object>> checklist = new ArrayList<>();
        Map<Integer, String> metodoPorVenta = new HashMap<>();
        for (PagoModel p : pagoRepository.pagosDeSesion(idSesion)) {
            metodoPorVenta.put(p.getVenta().getIdVenta(), p.getMetodo().getMetodoPago());
        }
        for (VentaModel v : ventas) {
            Map<String, Object> m = new HashMap<>();
            m.put("idVenta", v.getIdVenta());
            m.put("correlativo", v.getCorrelativoVenta());
            m.put("fecha", v.getFechaVenta() == null ? "" : F.format(v.getFechaVenta()));
            m.put("total", v.getTotalVenta());
            m.put("estado", v.getEstado().getNombreEstado());
            m.put("metodo", metodoPorVenta.getOrDefault(v.getIdVenta(), "—"));
            checklist.add(m);
        }

        // ---- RF-44: X out si ya está cerrada + últimos 5 cierres ----
        Map<String, Object> r = new HashMap<>();
        r.put("idSesion", idSesion);
        r.put("nombreCaja", sesion.getCaja().getNombreCaja());
        r.put("numeroCaja", sesion.getCaja().getNumeroCaja());
        r.put("cajero", sesion.getUsuario().getNombres() + " " + sesion.getUsuario().getApellidoPaterno());
        r.put("estado", sesion.getEstado().getNombreEstado());
        r.put("cerrada", cerrada);
        r.put("bloqueos", bloqueos);
        r.put("checklist", checklist);
        r.put("fondoInicial", fondoInicial);
        r.put("efectivoDelDia", efectivoDelDia);
        r.put("transferencias", totalTransferencias);
        r.put("desembolsos", desembolsos);
        r.put("fondoEsperado", fondoEsperado);

        if (cerrada) {
            r.put("fechaCierre", sesion.getFechaCierre() == null ? null : F.format(sesion.getFechaCierre()));
            r.put("fondoFinal", sesion.getFondoFinal());
            r.put("difFondo", sesion.getDifFondo());
        }

        r.put("anteriores", ultimosCierres(sesion.getCaja().getIdCaja()));
        return r;
    }

    /** RF-44: últimos 5 cierres de la caja (solo lectura). */
    public List<Map<String, Object>> ultimosCierres(Integer idCaja) {
        List<Map<String, Object>> salida = new ArrayList<>();
        List<SesionCajaModel> cierres = sesionRepository
                .findByCajaIdCajaAndEstadoNombreEstadoOrderByFechaCierreDesc(idCaja, "Cerrada");
        for (SesionCajaModel s : cierres.stream().limit(5).toList()) {
            Map<String, Object> m = new HashMap<>();
            m.put("idSesion", s.getIdSesion());
            m.put("fechaCierre", s.getFechaCierre() == null ? null : F.format(s.getFechaCierre()));
            m.put("fondoInicial", s.getFondoInicial());
            m.put("fondoFinal", s.getFondoFinal());
            m.put("difFondo", s.getDifFondo());
            m.put("cajero", s.getUsuario().getNombres() + " " + s.getUsuario().getApellidoPaterno());
            // total cobrado de esa sesión (para el listado de cierres)
            m.put("efectivo", pagoRepository.efectivoCobradoEn(s.getIdSesion()));
            m.put("transferencias", pagoRepository.transferenciasPorCanalEn(s.getIdSesion()).stream()
                    .map(f -> (BigDecimal) f[2])
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            salida.add(m);
        }
        return salida;
    }

    // ------------------------------------------------------------------
    // RF-43: cerrar la caja
    // ------------------------------------------------------------------
    @Transactional
    public Map<String, Object> cerrar(Integer idSesion, BigDecimal fondoFinal) {
        SesionCajaModel sesion = sesionRepository.findById(idSesion)
                .orElseThrow(() -> new RuntimeException("Sesión de caja no encontrada"));

        // RF-44: un único cierre por día y por caja (solo lectura si ya cerró)
        if ("Cerrada".equals(sesion.getEstado().getNombreEstado())) {
            throw new RuntimeException("La caja del día ya fue cerrada; el X out solo puede reimprimirse");
        }

        // RF-41: los pendientes bloquean el cierre
        List<String> bloqueos = (List<String>) detalle(idSesion).get("bloqueos");
        if (!bloqueos.isEmpty()) {
            throw new RuntimeException("Completa los pasos pendientes antes de cerrar: "
                    + String.join("; ", bloqueos));
        }

        // fondo inválido -> "Ingresa un fondo de caja válido"
        if (fondoFinal == null || fondoFinal.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Ingresa un fondo de caja válido");
        }

        // RF-43: el fondo debe coincidir con fondo inicial + efectivo - desembolsos
        BigDecimal efectivo = pagoRepository.efectivoCobradoEn(idSesion);
        BigDecimal desembolsos = desembolsoRepository.totalDesembolsado(idSesion);
        BigDecimal esperado = sesion.getFondoInicial().add(efectivo).subtract(desembolsos);
        BigDecimal diferencia = fondoFinal.subtract(esperado);
        if (diferencia.abs().compareTo(new BigDecimal("0.009")) > 0) {
            String tipo = diferencia.compareTo(BigDecimal.ZERO) > 0 ? "sobrante" : "faltante";
            throw new RuntimeException("El fondo no cuadra (" + tipo + " de S/ "
                    + diferencia.abs().toPlainString() + ")");
        }

        EstadoModel estadoCerrada = estadoRepository
                .findByEntidadAndNombreEstado("SESION_CAJA", "Cerrada")
                .orElseThrow(() -> new RuntimeException("Estado 'Cerrada' no encontrado"));

        sesion.setFondoFinal(fondoFinal);
        sesion.setDifFondo(diferencia);
        sesion.setFechaCierre(LocalDateTime.now());
        sesion.setEstado(estadoCerrada);
        sesionRepository.save(sesion);

        // el X out se arma con los datos ya persistidos
        Map<String, Object> r = detalle(idSesion);
        r.put("cerrado", true);
        return r;
    }

    /** Día de cierre de una caja (para el control de unicidad por día). */
    public boolean cajaCerradaHoy(Integer idCaja) {
        return sesionRepository.fueCerradaDesde(idCaja, LocalDate.now().atStartOfDay());
    }
}
