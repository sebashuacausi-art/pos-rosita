package pe.edu.upn.sowad.posrosita.controller;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.upn.sowad.posrosita.model.UsuarioModel;
import pe.edu.upn.sowad.posrosita.service.UsuarioService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    // Mismo salt que usa el script db_rosita_v4.sql para hashear las claves
    private static final String SALT = "salt_rosita_";

    @Autowired
    private UsuarioService usuarioService;

    // HU-01: login por ID numerico + clave
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String id = body.get("usuario");
        String clave = body.get("clave");

        if (id == null || id.trim().isEmpty() || clave == null || clave.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Ingrese el ID y la clave"));
        }

        Optional<UsuarioModel> opt = usuarioService.porUsuario(id.trim());
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "ID o clave incorrectos"));
        }

        UsuarioModel u = opt.get();

        // la clave en BD es SHA2(CONCAT('salt_rosita_', clave), 256) en hex
        if (!hash(clave).equals(u.getContrasena())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "ID o clave incorrectos"));
        }

        // RNF-06: el usuario desactivado no puede ingresar
        if (!"Activo".equals(u.getEstado().getNombreEstado())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Su usuario está inactivo. Consulte al Dueño"));
        }

        Map<String, Object> resp = new java.util.LinkedHashMap<>();
        resp.put("idUsuario", u.getIdUsuario());
        resp.put("nombres", u.getNombres());
        resp.put("apellidoPaterno", u.getApellidoPaterno());
        resp.put("rol", u.getRol().getNombreRol());
        return ResponseEntity.ok(resp);
    }

    private String hash(String clave) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest((SALT + clave).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : h) {
                sb.append(String.format("%02x", b & 0xFF));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo encriptar la clave", e);
        }
    }
}
