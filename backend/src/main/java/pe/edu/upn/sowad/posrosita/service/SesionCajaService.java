package pe.edu.upn.sowad.posrosita.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.upn.sowad.posrosita.model.CajaModel;
import pe.edu.upn.sowad.posrosita.model.EstadoModel;
import pe.edu.upn.sowad.posrosita.model.SesionCajaModel;
import pe.edu.upn.sowad.posrosita.model.UsuarioModel;
import pe.edu.upn.sowad.posrosita.repository.ICajaRepository;
import pe.edu.upn.sowad.posrosita.repository.IEstadoRepository;
import pe.edu.upn.sowad.posrosita.repository.ISesionCajaRepository;
import pe.edu.upn.sowad.posrosita.repository.IUsuarioRepository;

@Service
public class SesionCajaService {

    @Autowired
    private ISesionCajaRepository sesionRepository;

    @Autowired
    private ICajaRepository cajaRepository;

    @Autowired
    private IEstadoRepository estadoRepository;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    public List<SesionCajaModel> listarAbiertas() {
        return sesionRepository.findByEstadoNombreEstado("Abierta");
    }

    public Optional<SesionCajaModel> porId(Integer id) {
        return sesionRepository.findById(id);
    }

    /** Estados en los que la caja sigue OCUPADA (aún no cerrada). */
    private static final List<String> ESTADOS_ACTIVOS = List.of("Abierta", "Arqueada");

    /**
     * Sesión activa de la caja: 'Abierta' o 'Arqueada'.
     * Una caja arqueada sigue ocupada hasta el cierre.
     */
    public Optional<SesionCajaModel> abiertaDeCaja(Integer idCaja) {
        return sesionRepository.findByCajaIdCajaAndEstadoNombreEstadoIn(idCaja, ESTADOS_ACTIVOS);
    }

    /**
     * RNF multi-caja: la sesión activa que pertenece a ESTE usuario.
     * Se usa al iniciar sesión para restaurar la caja del operador
     * sin depender del almacenamiento del navegador.
     */
    public Optional<SesionCajaModel> abiertaDeUsuario(Integer idUsuario) {
        return sesionRepository.findByUsuarioIdUsuarioAndEstadoNombreEstadoIn(idUsuario, ESTADOS_ACTIVOS);
    }

    public List<CajaModel> listarCajas() {
        return cajaRepository.findAllByOrderByNumeroCajaAsc();
    }

    /**
     * Cajas que este usuario puede operar segun su rol.
     * RNF: "La Caja 1 corresponde siempre al Dueño y las cajas 2, 3 y 4 a los Vendedores."
     */
    public List<CajaModel> cajasDeUsuario(Integer idUsuario) {
        String rol = rolDe(idUsuario);
        boolean esDueno = esDueno(rol);
        List<CajaModel> todas = listarCajas();
        return todas.stream()
                .filter(c -> (c.getNumeroCaja() == 1) == esDueno)
                .toList();
    }

    /**
     * HU-02 / RN-015 + RNF multi-caja.
     * Reglas aplicadas al aperturar:
     *  1. La caja le corresponde al rol del usuario (Caja 1 = Dueño, 2-4 = Vendedores).
     *  2. Un usuario no puede tener dos cajas abiertas a la vez.
     *  3. Una caja con sesión abierta no se vuelve a aperturar.
     *  4. Una caja cerrada en el día no puede reabrirse.
     */
    @Transactional
    public SesionCajaModel aperturar(Integer idUsuario, Integer numeroCaja, BigDecimal fondoInicial) {
        CajaModel caja = cajaRepository.findByNumeroCaja(numeroCaja)
                .orElseThrow(() -> new RuntimeException("La caja " + numeroCaja + " no existe"));

        UsuarioModel usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 1. la caja debe corresponder al rol
        String rol = usuario.getRol().getNombreRol();
        boolean cajaDelDueno = caja.getNumeroCaja() == 1;
        if (cajaDelDueno && !esDueno(rol)) {
            throw new RuntimeException("La Caja 1 corresponde al Dueño; elija una caja de vendedor");
        }
        if (!cajaDelDueno && esDueno(rol)) {
            throw new RuntimeException("El Dueño opera en la Caja 1");
        }

        // 2. un usuario = una caja abierta
        Optional<SesionCajaModel> propia = abiertaDeUsuario(idUsuario);
        if (propia.isPresent()) {
            throw new RuntimeException(
                    "Ya tiene la " + propia.get().getCaja().getNombreCaja() + " abierta; "
                            + "córreela antes de aperturar otra");
        }

        // 3. la caja no puede estar abierta ya
        if (abiertaDeCaja(caja.getIdCaja()).isPresent()) {
            throw new RuntimeException("La " + caja.getNombreCaja() + " ya está abierta");
        }

        // 4. caja cerrada en el día no se reabre
        if (sesionRepository.fueCerradaDesde(caja.getIdCaja(), LocalDate.now().atStartOfDay())) {
            throw new RuntimeException("La caja del día ya fue cerrada y no puede volver a abrirse");
        }

        EstadoModel estadoAbierta = estadoRepository
                .findByEntidadAndNombreEstado("SESION_CAJA", "Abierta")
                .orElseThrow(() -> new RuntimeException("Estado 'Abierta' no encontrado"));

        SesionCajaModel sesion = new SesionCajaModel();
        sesion.setUsuario(usuario);
        sesion.setCaja(caja);
        sesion.setEstado(estadoAbierta);
        sesion.setFechaApertura(LocalDateTime.now());
        sesion.setFondoInicial(fondoInicial);
        return sesionRepository.save(sesion);
    }

    private String rolDe(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .map(u -> u.getRol().getNombreRol())
                .orElse("");
    }

    private boolean esDueno(String rol) {
        return "Dueño".equalsIgnoreCase(rol) || "DUENO".equalsIgnoreCase(rol);
    }
}
