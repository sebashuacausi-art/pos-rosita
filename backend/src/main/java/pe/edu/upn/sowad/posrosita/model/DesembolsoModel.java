package pe.edu.upn.sowad.posrosita.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Salida de efectivo del día, registrada después de confirmar el arqueo.
 * RF-39 / RF-40.
 */
@Entity
@Table(name = "desembolso")
public class DesembolsoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_desembolso")
    private Integer idDesembolso;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_sesion", nullable = false)
    private SesionCajaModel sesion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioModel usuario;

    @Column(name = "fecha_desembolso", nullable = false)
    private LocalDateTime fechaDesembolso;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "descripcion", nullable = false, length = 200)
    private String descripcion;

    public Integer getIdDesembolso() { return idDesembolso; }
    public void setIdDesembolso(Integer idDesembolso) { this.idDesembolso = idDesembolso; }
    public SesionCajaModel getSesion() { return sesion; }
    public void setSesion(SesionCajaModel sesion) { this.sesion = sesion; }
    public UsuarioModel getUsuario() { return usuario; }
    public void setUsuario(UsuarioModel usuario) { this.usuario = usuario; }
    public LocalDateTime getFechaDesembolso() { return fechaDesembolso; }
    public void setFechaDesembolso(LocalDateTime fechaDesembolso) { this.fechaDesembolso = fechaDesembolso; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
