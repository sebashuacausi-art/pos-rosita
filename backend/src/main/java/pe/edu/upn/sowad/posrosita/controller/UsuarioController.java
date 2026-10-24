package pe.edu.upn.sowad.posrosita.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.upn.sowad.posrosita.dto.UsuarioRequestDTO;
import pe.edu.upn.sowad.posrosita.model.UsuarioModel;
import pe.edu.upn.sowad.posrosita.repository.IRolRepository;
import pe.edu.upn.sowad.posrosita.service.UsuarioService;

// Gestión de usuarios: solo el Dueño (RNF-06)
@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private IRolRepository rolRepository;

    @GetMapping
    public List<Map<String, Object>> listar() {
        return usuarioService.listarTodos().stream()
                .map(this::aMap)
                .collect(Collectors.toList());
    }

    @GetMapping("/roles")
    public List<Map<String, Object>> roles() {
        return rolRepository.findAll().stream()
                .map(r -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("idRol", r.getIdRol());
                    m.put("nombreRol", r.getNombreRol());
                    return m;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> aMap(UsuarioModel u) {
        Map<String, Object> m = new java.util.HashMap<>();
        m.put("idUsuario", u.getIdUsuario());
        m.put("nombres", u.getNombres());
        m.put("apellidoPaterno", u.getApellidoPaterno());
        m.put("idRol", u.getRol().getIdRol());
        m.put("rol", u.getRol().getNombreRol());
        m.put("estado", u.getEstado().getNombreEstado());
        return m;
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody UsuarioRequestDTO dto) {
        try {
            UsuarioModel u = usuarioService.crear(dto);
            return ResponseEntity.ok(usuarioService.aResponse(u));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Integer id, @RequestBody UsuarioRequestDTO dto) {
        try {
            UsuarioModel u = usuarioService.editar(id, dto);
            return ResponseEntity.ok(usuarioService.aResponse(u));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    // activa / desactiva
    @PutMapping("/{id}/cambiar-estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id,
            @RequestBody Map<String, Integer> body) {
        try {
            Integer idQuePide = body.getOrDefault("idUsuario", 0);
            UsuarioModel u = usuarioService.cambiarEstado(id, idQuePide);
            return ResponseEntity.ok(usuarioService.aResponse(u));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }
}
