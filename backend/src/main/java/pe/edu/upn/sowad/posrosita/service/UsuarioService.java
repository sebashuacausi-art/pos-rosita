package pe.edu.upn.sowad.posrosita.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pe.edu.upn.sowad.posrosita.dto.UsuarioRequestDTO;
import pe.edu.upn.sowad.posrosita.dto.UsuarioResponseDTO;
import pe.edu.upn.sowad.posrosita.model.EstadoModel;
import pe.edu.upn.sowad.posrosita.model.RolModel;
import pe.edu.upn.sowad.posrosita.model.UsuarioModel;
import pe.edu.upn.sowad.posrosita.repository.IEstadoRepository;
import pe.edu.upn.sowad.posrosita.repository.IRolRepository;
import pe.edu.upn.sowad.posrosita.repository.IUsuarioRepository;

@Service
public class UsuarioService {

    // Mismo salt que usa el script db_rosita_v4.sql (RNF-05: hash + sal, nunca texto plano)
    private static final String SALT = "salt_rosita_";

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private IRolRepository rolRepository;

    @Autowired
    private IEstadoRepository estadoRepository;

    public Optional<UsuarioModel> porId(Integer id) {
        return usuarioRepository.findById(id);
    }

    // HU-01: busca por el ID numerico (campo "usuario")
    public Optional<UsuarioModel> porUsuario(String usuario) {
        return usuarioRepository.findByUsuario(usuario);
    }

    // ---------------- Gestión de usuarios (Dueño) ----------------

    public List<UsuarioModel> listarTodos() {
        return usuarioRepository.findAll();
    }

    public UsuarioResponseDTO aResponse(UsuarioModel u) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setIdUsuario(u.getIdUsuario());
        dto.setNombres(u.getNombres());
        dto.setApellidoPaterno(u.getApellidoPaterno());
        dto.setIdRol(u.getRol().getIdRol());
        dto.setRol(u.getRol().getNombreRol());
        dto.setEstado(u.getEstado().getNombreEstado());
        return dto;
    }

    public UsuarioModel crear(UsuarioRequestDTO dto) {
        validar(dto, true, null);

        RolModel rol = rolRepository.findById(dto.getIdRol())
                .orElseThrow(() -> new RuntimeException("El rol indicado no existe"));

        UsuarioModel u = new UsuarioModel();
        u.setUsuario(String.valueOf(dto.getIdUsuario()));
        u.setNombres(dto.getNombres().trim());
        u.setApellidoPaterno(dto.getApellidoPaterno().trim());
        u.setRol(rol);
        // RNF-05: se guarda hasheada con sal
        u.setContrasena(hash(dto.getClave().trim()));

        EstadoModel activo = estadoRepository.findByEntidadAndNombreEstado("USUARIO", "Activo")
                .orElseThrow(() -> new RuntimeException("No existe el estado Activo de usuario"));
        u.setEstado(activo);

        return usuarioRepository.save(u);
    }

    public UsuarioModel editar(Integer id, UsuarioRequestDTO dto) {
        UsuarioModel u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        validar(dto, false, id);

        RolModel rol = rolRepository.findById(dto.getIdRol())
                .orElseThrow(() -> new RuntimeException("El rol indicado no existe"));

        u.setNombres(dto.getNombres().trim());
        u.setApellidoPaterno(dto.getApellidoPaterno().trim());
        u.setRol(rol);
        // si la clave viene vacia se conserva la actual
        if (dto.getClave() != null && !dto.getClave().trim().isEmpty()) {
            u.setContrasena(hash(dto.getClave().trim()));
        }
        return usuarioRepository.save(u);
    }

    // Activa o desactiva el usuario (no permite quedarse sin usuarios activos ni tocarse a uno mismo)
    public UsuarioModel cambiarEstado(Integer id, Integer idQuePide) {
        if (id.equals(idQuePide)) {
            throw new RuntimeException("No puede desactivar su propio usuario");
        }
        UsuarioModel u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean activo = u.getEstado().getNombreEstado().equals("Activo");
        if (activo) {
            long activos = usuarioRepository.findAll().stream()
                    .filter(x -> x.getEstado().getNombreEstado().equals("Activo")).count();
            if (activos <= 1) {
                throw new RuntimeException("Debe existir al menos un usuario activo");
            }
        }
        String nuevo = activo ? "Inactivo" : "Activo";
        u.setEstado(estadoRepository.findByEntidadAndNombreEstado("USUARIO", nuevo)
                .orElseThrow(() -> new RuntimeException("Estado de usuario no encontrado")));
        return usuarioRepository.save(u);
    }

    private void validar(UsuarioRequestDTO dto, boolean esCreacion, Integer idPropio) {
        if (dto.getIdUsuario() == null || dto.getIdUsuario() < 1) {
            throw new RuntimeException("El ID de usuario es obligatorio y debe ser positivo");
        }
        if (dto.getNombres() == null || dto.getNombres().trim().isEmpty()) {
            throw new RuntimeException("Los nombres son obligatorios");
        }
        if (dto.getApellidoPaterno() == null || dto.getApellidoPaterno().trim().isEmpty()) {
            throw new RuntimeException("El apellido paterno es obligatorio");
        }
        if (dto.getIdRol() == null) {
            throw new RuntimeException("Debe indicar el rol del usuario");
        }
        if (esCreacion && (dto.getClave() == null || dto.getClave().trim().isEmpty())) {
            throw new RuntimeException("La clave es obligatoria");
        }
        if (dto.getClave() != null && !dto.getClave().trim().isEmpty()
                && dto.getClave().trim().length() < 6) {
            throw new RuntimeException("La clave debe tener al menos 6 caracteres");
        }

        // el ID numerico no puede repetirse (es el login)
        Optional<UsuarioModel> repetido = usuarioRepository.findByUsuario(String.valueOf(dto.getIdUsuario()));
        if (repetido.isPresent() && !repetido.get().getIdUsuario().equals(idPropio)) {
            throw new RuntimeException("Ya existe un usuario con ese ID");
        }
    }

    public String hash(String clave) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest((SALT + clave).getBytes(StandardCharsets.UTF_8));
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
