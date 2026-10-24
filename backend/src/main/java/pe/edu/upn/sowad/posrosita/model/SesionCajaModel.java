package pe.edu.upn.sowad.posrosita.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sesion_caja")
public class SesionCajaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sesion")
    private Integer idSesion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioModel usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_caja", nullable = false)
    private CajaModel caja;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_estado", nullable = false)
    private EstadoModel estado;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fondo_inicial", nullable = false, precision = 10, scale = 2)
    private BigDecimal fondoInicial;

    @Column(name = "fecha_arqueo")
    private LocalDateTime fechaArqueo;

    @Column(name = "efectivo_esperado", precision = 10, scale = 2)
    private BigDecimal efectivoEsperado;

    @Column(name = "efectivo_contado", precision = 10, scale = 2)
    private BigDecimal efectivoContado;

    @Column(name = "dif_efectivo", precision = 10, scale = 2)
    private BigDecimal difEfectivo;

    @Column(name = "observacion", length = 200)
    private String observacion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "fondo_final", precision = 10, scale = 2)
    private BigDecimal fondoFinal;

    @Column(name = "dif_fondo", precision = 10, scale = 2)
    private BigDecimal difFondo;

    public Integer getIdSesion() { return idSesion; }
    public void setIdSesion(Integer idSesion) { this.idSesion = idSesion; }
    public UsuarioModel getUsuario() { return usuario; }
    public void setUsuario(UsuarioModel usuario) { this.usuario = usuario; }
    public CajaModel getCaja() { return caja; }
    public void setCaja(CajaModel caja) { this.caja = caja; }
    public EstadoModel getEstado() { return estado; }
    public void setEstado(EstadoModel estado) { this.estado = estado; }
    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; }
    public BigDecimal getFondoInicial() { return fondoInicial; }
    public void setFondoInicial(BigDecimal fondoInicial) { this.fondoInicial = fondoInicial; }
    public LocalDateTime getFechaArqueo() { return fechaArqueo; }
    public void setFechaArqueo(LocalDateTime fechaArqueo) { this.fechaArqueo = fechaArqueo; }
    public BigDecimal getEfectivoEsperado() { return efectivoEsperado; }
    public void setEfectivoEsperado(BigDecimal efectivoEsperado) { this.efectivoEsperado = efectivoEsperado; }
    public BigDecimal getEfectivoContado() { return efectivoContado; }
    public void setEfectivoContado(BigDecimal efectivoContado) { this.efectivoContado = efectivoContado; }
    public BigDecimal getDifEfectivo() { return difEfectivo; }
    public void setDifEfectivo(BigDecimal difEfectivo) { this.difEfectivo = difEfectivo; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }
    public BigDecimal getFondoFinal() { return fondoFinal; }
    public void setFondoFinal(BigDecimal fondoFinal) { this.fondoFinal = fondoFinal; }
    public BigDecimal getDifFondo() { return difFondo; }
    public void setDifFondo(BigDecimal difFondo) { this.difFondo = difFondo; }
}
