package pe.edu.upn.sowad.posrosita.controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.upn.sowad.posrosita.dto.VentaRequestDTO;
import pe.edu.upn.sowad.posrosita.dto.VentaResponseDTO;
import pe.edu.upn.sowad.posrosita.model.DetalleVentaModel;
import pe.edu.upn.sowad.posrosita.model.VentaModel;
import pe.edu.upn.sowad.posrosita.service.VentaService;

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin(origins = "http://localhost:4200")
public class VentaController {

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    private VentaService ventaService;

    // HU-06 / HU-07: registrar la venta (transaccional, con descuento de stock)
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody VentaRequestDTO req) {
        try {
            VentaResponseDTO resp = ventaService.registrar(req);
            Map<String, Object> out = new HashMap<>();
            out.put("idVenta", resp.getIdVenta());
            out.put("correlativo", resp.getCorrelativo());
            out.put("estado", resp.getEstado());
            out.put("subtotal", resp.getSubtotal());
            out.put("descuentoTotal", resp.getDescuentoTotal());
            out.put("total", resp.getTotal());
            out.put("tieneComprobante", resp.getTieneComprobante());
            List<Map<String, Object>> dets = new ArrayList<>();
            if (resp.getDetalles() != null) {
                for (DetalleVentaModel d : resp.getDetalles()) {
                    Map<String, Object> dm = new HashMap<>();
                    dm.put("nombreProducto", d.getProducto().getNombreProducto());
                    dm.put("codigoProducto", d.getProducto().getCodigoProducto());
                    dm.put("cantidad", d.getCantidad());
                    dm.put("precioUnitario", d.getPrecioUnitario());
                    dm.put("descuento", d.getDescuento());
                    dm.put("subtotal", d.getSubtotal());
                    dets.add(dm);
                }
            }
            out.put("detalles", dets);
            return ResponseEntity.ok(out);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
        }
    }

    // ventas de una sesion de caja (para el arqueo y cierre)
    @GetMapping("/sesion/{idSesion}")
    public List<Map<String, Object>> porSesion(@PathVariable Integer idSesion) {
        List<Map<String, Object>> salida = new ArrayList<>();
        for (VentaModel v : ventaService.listarPorSesion(idSesion)) {
            salida.add(uno(v));
        }
        return salida;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> porId(@PathVariable Integer id) {
        Optional<VentaModel> opt = ventaService.porId(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> m = uno(opt.get());
        List<Map<String, Object>> dets = new ArrayList<>();
        for (DetalleVentaModel d : ventaService.detallesDe(id)) {
            Map<String, Object> dm = new HashMap<>();
            dm.put("nombreProducto", d.getProducto().getNombreProducto());
            dm.put("codigoProducto", d.getProducto().getCodigoProducto());
            dm.put("cantidad", d.getCantidad());
            dm.put("precioUnitario", d.getPrecioUnitario());
            dm.put("descuento", d.getDescuento());
            dm.put("subtotal", d.getSubtotal());
            dets.add(dm);
        }
        m.put("detalles", dets);
        return ResponseEntity.ok(m);
    }

    private Map<String, Object> uno(VentaModel v) {
        Map<String, Object> m = new HashMap<>();
        m.put("idVenta", v.getIdVenta());
        m.put("correlativo", v.getCorrelativoVenta());
        m.put("fechaVenta", v.getFechaVenta() == null ? null : v.getFechaVenta().format(F));
        m.put("subtotal", v.getSubtotal());
        m.put("descuentoTotal", v.getDescuentoTotal());
        m.put("total", v.getTotalVenta());
        m.put("estado", v.getEstado().getNombreEstado());
        m.put("usuario", v.getUsuario().getNombres() + " " + v.getUsuario().getApellidoPaterno());
        return m;
    }
}
