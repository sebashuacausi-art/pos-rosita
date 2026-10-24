package pe.edu.upn.sowad.posrosita.model;

import jakarta.persistence.*;

@Entity
@Table(name = "caja")
public class CajaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_caja")
    private Integer idCaja;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_estado", nullable = false)
    private EstadoModel estado;

    @Column(name = "numero_caja", nullable = false, unique = true)
    private Integer numeroCaja;

    @Column(name = "nombre_caja", nullable = false, length = 30)
    private String nombreCaja;

    public Integer getIdCaja() { return idCaja; }
    public void setIdCaja(Integer idCaja) { this.idCaja = idCaja; }
    public EstadoModel getEstado() { return estado; }
    public void setEstado(EstadoModel estado) { this.estado = estado; }
    public Integer getNumeroCaja() { return numeroCaja; }
    public void setNumeroCaja(Integer numeroCaja) { this.numeroCaja = numeroCaja; }
    public String getNombreCaja() { return nombreCaja; }
    public void setNombreCaja(String nombreCaja) { this.nombreCaja = nombreCaja; }
}
