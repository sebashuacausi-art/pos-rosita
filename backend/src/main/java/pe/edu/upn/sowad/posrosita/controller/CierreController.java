package pe.edu.upn.sowad.posrosita.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

import pe.edu.upn.sowad.posrosita.service.CierreService;

/**
 * Cierre de caja y X out (RF-41 a RF-44).
 * Valida los pendientes del día, cierra la caja con el fondo final
 * y deja el X out disponible para reimpresión + últimos 5 cierres.
 */
@RestController
@RequestMapping("/api/cierre")
@CrossOrigin(origins = { "http://localhost:4200" })
public class CierreController {

    @Autowired
    private CierreService cierreService;

    /** Estado del cierre: pendientes, checklist, resumen, X out y anteriores. */
    @GetMapping("/{idSesion}")
    public ResponseEntity<?> detalle(@PathVariable Integer idSesion) {
        try {
            return ResponseEntity.ok(cierreService.detalle(idSesion));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        }
    }

    /** RF-43: cerrar la caja e "imprimir" el X out. */
    @PostMapping("/{idSesion}/cerrar")
    public ResponseEntity<?> cerrar(@PathVariable Integer idSesion, @RequestBody Map<String, Object> body) {
        try {
            String fondoStr = body.get("fondoFinal") == null ? "" : String.valueOf(body.get("fondoFinal")).trim();
            BigDecimal fondoFinal = fondoStr.isEmpty() ? null : new BigDecimal(fondoStr);
            Map<String, Object> r = cierreService.cerrar(idSesion, fondoFinal);
            return ResponseEntity.ok(r);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Ingresa un fondo de caja válido"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
        }
    }

    /** RF-44: últimos 5 cierres de una caja (solo lectura). */
    @GetMapping("/caja/{idCaja}/anteriores")
    public List<Map<String, Object>> anteriores(@PathVariable Integer idCaja) {
        return cierreService.ultimosCierres(idCaja);
    }
}
