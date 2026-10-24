package pe.edu.upn.sowad.posrosita.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.upn.sowad.posrosita.dto.DetalleVentaDTO;
import pe.edu.upn.sowad.posrosita.dto.PagoDTO;
import pe.edu.upn.sowad.posrosita.dto.VentaRequestDTO;
import pe.edu.upn.sowad.posrosita.dto.VentaResponseDTO;
import pe.edu.upn.sowad.posrosita.model.DetalleVentaModel;
import pe.edu.upn.sowad.posrosita.model.EstadoModel;
import pe.edu.upn.sowad.posrosita.model.PagoModel;
import pe.edu.upn.sowad.posrosita.model.ProductoModel;
import pe.edu.upn.sowad.posrosita.model.SesionCajaModel;
import pe.edu.upn.sowad.posrosita.model.StockModel;
import pe.edu.upn.sowad.posrosita.model.TipoPagoModel;
import pe.edu.upn.sowad.posrosita.model.VentaModel;
import pe.edu.upn.sowad.posrosita.repository.IDetalleVentaRepository;
import pe.edu.upn.sowad.posrosita.repository.IEstadoRepository;
import pe.edu.upn.sowad.posrosita.repository.IPagoRepository;
import pe.edu.upn.sowad.posrosita.repository.IProductoRepository;
import pe.edu.upn.sowad.posrosita.repository.ISesionCajaRepository;
import pe.edu.upn.sowad.posrosita.repository.IStockRepository;
import pe.edu.upn.sowad.posrosita.repository.ITipoPagoRepository;
import pe.edu.upn.sowad.posrosita.repository.IVentaRepository;

@Service
public class VentaService {

    @Autowired
    private IVentaRepository ventaRepository;

    @Autowired
    private IDetalleVentaRepository detalleRepository;

    @Autowired
    private IProductoRepository productoRepository;

    @Autowired
    private IStockRepository stockRepository;

    @Autowired
    private ISesionCajaRepository sesionRepository;

    @Autowired
    private IEstadoRepository estadoRepository;

    @Autowired
    private ITipoPagoRepository tipoPagoRepository;

    @Autowired
    private IPagoRepository pagoRepository;

    public List<VentaModel> listarPorSesion(Integer idSesion) {
        return ventaRepository.findBySesionIdSesion(idSesion);
    }

    public Optional<VentaModel> porId(Integer id) {
        return ventaRepository.findById(id);
    }

    public List<DetalleVentaModel> detallesDe(Integer idVenta) {
        return detalleRepository.findByVentaIdVenta(idVenta);
    }

    // RN-005: la venta y el descuento de stock son una sola operacion.
    // Si algo falla, se revierte todo (la transaccion de Spring lo garantiza).
    @Transactional
    public VentaResponseDTO registrar(VentaRequestDTO req) {

        SesionCajaModel sesion = sesionRepository.findById(req.getIdSesion())
                .orElseThrow(() -> new RuntimeException("Sesión de caja no encontrada"));

        if (req.getDetalles() == null || req.getDetalles().isEmpty()) {
            throw new RuntimeException("No se puede generar una venta con el carrito vacío (RN-012)");
        }

        // RN-001: revalidar stock de todos los items antes de cobrar
        List<DetalleVentaModel> detalles = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal descuentoTotal = BigDecimal.ZERO;

        for (DetalleVentaDTO d : req.getDetalles()) {
            ProductoModel producto = productoRepository.findById(d.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto " + d.getIdProducto() + " no encontrado"));

            StockModel stock = stockRepository.findByProductoIdProducto(producto.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Sin registro de stock para " + producto.getNombreProducto()));

            int disponible = stock.getCantidadActual() - stock.getCantidadReservada();
            if (d.getCantidad() > disponible) {
                throw new RuntimeException(
                        "Stock insuficiente de " + producto.getNombreProducto()
                                + ": disponible " + disponible + " unidades (RN-001)");
            }

            BigDecimal precio = producto.getPrecioUnitario();
            BigDecimal descuentoLinea = d.getDescuento() == null ? BigDecimal.ZERO : d.getDescuento();
            BigDecimal subtotalLinea = precio.multiply(BigDecimal.valueOf(d.getCantidad())).subtract(descuentoLinea);

            DetalleVentaModel det = new DetalleVentaModel();
            det.setProducto(producto);
            det.setCantidad(d.getCantidad());
            det.setPrecioUnitario(precio);
            det.setDescuento(descuentoLinea);
            det.setSubtotal(subtotalLinea);

            detalles.add(det);
            subtotal = subtotal.add(precio.multiply(BigDecimal.valueOf(d.getCantidad())));
            descuentoTotal = descuentoTotal.add(descuentoLinea);
        }

        BigDecimal total = subtotal.subtract(descuentoTotal);

        // RN-004: estado de la venta segun el monto (sin comprobante / registrada)
        String nombreEstado = total.compareTo(new BigDecimal("5.00")) < 0 ? "Sin comprobante" : "Registrada";
        EstadoModel estadoVenta = estadoRepository.findByEntidadAndNombreEstado("VENTA", nombreEstado)
                .orElseThrow(() -> new RuntimeException("Estado '" + nombreEstado + "' no encontrado"));

        VentaModel venta = new VentaModel();
        venta.setUsuario(sesion.getUsuario());
        venta.setSesion(sesion);
        venta.setEstado(estadoVenta);
        venta.setFechaVenta(LocalDateTime.now());
        venta.setSubtotal(subtotal);
        venta.setDescuentoTotal(descuentoTotal);
        venta.setTotalVenta(total);
        venta.setCorrelativoVenta(req.getCorrelativo());
        VentaModel ventaGuardada = ventaRepository.save(venta);

        for (DetalleVentaModel det : detalles) {
            det.setVenta(ventaGuardada);
            detalleRepository.save(det);

            // descuento inmediato del stock fisico
            StockModel stock = stockRepository.findByProductoIdProducto(det.getProducto().getIdProducto()).get();
            stock.setCantidadActual(stock.getCantidadActual() - det.getCantidad());
            stockRepository.save(stock);
        }

        // registro del pago
        PagoModel pago = new PagoModel();
        pago.setVenta(ventaGuardada);
        TipoPagoModel metodo = tipoPagoRepository.findByMetodoPago(req.getPago().getMetodo())
                .orElseThrow(() -> new RuntimeException("Método de pago no encontrado"));
        pago.setMetodo(metodo);
        pago.setMonto(total);
        pago.setCanalTransferencia(req.getPago().getCanalTransferencia());
        pago.setNumeroOperacion(req.getPago().getNumeroOperacion());
        pago.setMontoRecibido(req.getPago().getMontoRecibido());
        pago.setVuelto(req.getPago().getVuelto());
        pago.setPagoValidado(req.getPago().getPagoValidado() != null && req.getPago().getPagoValidado());
        pago.setFechaPago(LocalDateTime.now());
        pagoRepository.save(pago);

        VentaResponseDTO resp = new VentaResponseDTO();
        resp.setIdVenta(ventaGuardada.getIdVenta());
        resp.setCorrelativo(ventaGuardada.getCorrelativoVenta());
        resp.setEstado(ventaGuardada.getEstado().getNombreEstado());
        resp.setSubtotal(subtotal);
        resp.setDescuentoTotal(descuentoTotal);
        resp.setTotal(total);
        resp.setTieneComprobante("Registrada".equals(nombreEstado));
        resp.setDetalles(detalles);
        return resp;
    }
}
