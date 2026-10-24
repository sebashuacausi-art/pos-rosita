package pe.edu.upn.sowad.posrosita.controller;

import java.math.BigDecimal;
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

import pe.edu.upn.sowad.posrosita.model.CajaModel;
import pe.edu.upn.sowad.posrosita.model.SesionCajaModel;
import pe.edu.upn.sowad.posrosita.service.SesionCajaService;

@RestController
@RequestMapping("/api/cajas")
@CrossOrigin(origins = "http://localhost:4200")
public class SesionCajaController {

    @Autowired
    private SesionCajaService sesionService;

    /**
     * HU-02: cajas que este usuario puede operar según su rol.
     * RNF: la Caja 1 es del Dueño; las 2, 3 y 4 de los Vendedores.
     */
    @GetMapping("/usuario/{idUsuario}")
    public List<Map<String, Object>> cajasDeUsuario(@PathVariable Integer idUsuario) {
        List<Map<String, Object>> salida = new java.util.ArrayList<>();
        for (CajaModel c : sesionService.cajasDeUsuario(idUsuario)) {
            Map<String, Object> m = new HashMap<>();
            m.put("idCaja", c.getIdCaja());
            m.put("numeroCaja", c.getNumeroCaja());
            m.put("nombreCaja", c.getNombreCaja());
            m.put("abierta", sesionService.abiertaDeCaja(c.getIdCaja()).isPresent());
            salida.add(m);
        }
        return salida;
    }

    /**
     * Sesión de caja abierta de ESTE usuario.
     * Se llama al iniciar sesión para restaurar el estado real desde la base
     * (el navegador no es la fuente de verdad).
     */
    @GetMapping("/usuario/{idUsuario}/sesion")
    public ResponseEntity<Map<String, Object>> sesionDeUsuario(@PathVariable Integer idUsuario) {
        Optional<SesionCajaModel> opt = sesionService.abiertaDeUsuario(idUsuario);
        if (opt.isEmpty()) {
            return ResponseEntity.ok(Map.of("abierta", false));
        }
        SesionCajaModel s = opt.get();
        Map<String, Object> m = new HashMap<>();
        m.put("abierta", true);
        m.put("idSesion", s.getIdSesion());
        m.put("idCaja", s.getCaja().getIdCaja());
        m.put("nombreCaja", s.getCaja().getNombreCaja());
        m.put("fondoInicial", s.getFondoInicial());
        m.put("fechaApertura", s.getFechaApertura());
        return ResponseEntity.ok(m);
    }

    // HU-02: apertura de caja con fondo inicial (RN-015 + RNF multi-caja)
    @PostMapping("/aperturar")
    public ResponseEntity<?> aperturar(@RequestBody Map<String, Object> body) {
        try {
            Integer idUsuario = Integer.valueOf(String.valueOf(body.get("idUsuario")));
            Integer numeroCaja = Integer.valueOf(String.valueOf(body.get("numeroCaja")));
            BigDecimal fondo = new BigDecimal(String.valueOf(body.get("fondoInicial")));

            if (fondo.compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", "El fondo de caja no puede ser negativo"));
            }

            SesionCajaModel sesion = sesionService.aperturar(idUsuario, numeroCaja, fondo);
            Map<String, Object> resp = new HashMap<>();
            resp.put("abierta", true);
            resp.put("idSesion", sesion.getIdSesion());
            resp.put("idCaja", sesion.getCaja().getIdCaja());
            resp.put("nombreCaja", sesion.getCaja().getNombreCaja());
            resp.put("fondoInicial", sesion.getFondoInicial());
            resp.put("fechaApertura", sesion.getFechaApertura());
            return ResponseEntity.ok(resp);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Datos de apertura inválidos"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
        }
    }

    // sesion abierta de una caja (para saber si ya se puede vender)
    @GetMapping("/{numeroCaja}/abierta")
    public ResponseEntity<Map<String, Object>> abierta(@PathVariable Integer numeroCaja) {
        Optional<CajaModel> caja = sesionService.listarCajas().stream()
                .filter(c -> c.getNumeroCaja().equals(numeroCaja.intValue()))
                .findFirst();
        if (caja.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Optional<SesionCajaModel> sesion = sesionService.abiertaDeCaja(caja.get().getIdCaja());
        if (sesion.isEmpty()) {
            return ResponseEntity.ok(Map.of("abierta", false));
        }
        SesionCajaModel s = sesion.get();
        Map<String, Object> m = new HashMap<>();
        m.put("abierta", true);
        m.put("idSesion", s.getIdSesion());
        m.put("nombreCaja", s.getCaja().getNombreCaja());
        m.put("fondoInicial", s.getFondoInicial());
        m.put("fechaApertura", s.getFechaApertura());
        m.put("usuario", s.getUsuario().getNombres() + " " + s.getUsuario().getApellidoPaterno());
        return ResponseEntity.ok(m);
    }
}
