package pe.edu.upn.sowad.posrosita.controller;

import java.math.BigDecimal;
import java.util.HashMap;
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

import pe.edu.upn.sowad.posrosita.model.SesionCajaModel;
import pe.edu.upn.sowad.posrosita.service.ArqueoService;

/**
 * Arqueo de caja (RF-35 a RF-38).
 * Compara el efectivo físico con el esperado y muestra el total
 * informativo de transferencias por canal.
 */
@RestController
@RequestMapping("/api/arqueo")
@CrossOrigin(origins = { "http://localhost:4200" })
public class ArqueoController {

    @Autowired
    private ArqueoService arqueoService;

    /** Cálculo en vivo del arqueo de la sesión (RF-35 / RF-36). */
    @GetMapping("/{idSesion}")
    public ResponseEntity<?> calcular(@PathVariable Integer idSesion) {
        try {
            return ResponseEntity.ok(arqueoService.calcular(idSesion));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Confirmar el arqueo (RF-37): exige cuadre exacto y sin pendientes. */
    @PostMapping("/{idSesion}/confirmar")
    public ResponseEntity<?> confirmar(@PathVariable Integer idSesion, @RequestBody Map<String, Object> body) {
        try {
            String contadoStr = String.valueOf(body.getOrDefault("efectivoContado", "0"));
            String observacion = body.get("observacion") == null ? "" : String.valueOf(body.get("observacion"));
            BigDecimal contado = new BigDecimal(contadoStr);

            SesionCajaModel sesion = arqueoService.confirmar(idSesion, contado, observacion.trim());

            Map<String, Object> resp = new HashMap<>();
            resp.put("arqueado", true);
            resp.put("estado", sesion.getEstado().getNombreEstado());
            resp.put("efectivoEsperado", sesion.getEfectivoEsperado());
            resp.put("efectivoContado", sesion.getEfectivoContado());
            resp.put("difEfectivo", sesion.getDifEfectivo());
            resp.put("fechaArqueo", sesion.getFechaArqueo());
            return ResponseEntity.ok(resp);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El efectivo contado no es un número válido"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
        }
    }
}
