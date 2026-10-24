package pe.edu.upn.sowad.posrosita.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago")
public class PagoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Integer idPago;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_venta")
    private VentaModel venta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_metodo", nullable = false)
    private TipoPagoModel metodo;

    @Column(name = "canal_transferencia", length = 10)
    private String canalTransferencia;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "monto_recibido", precision = 10, scale = 2)
    private BigDecimal montoRecibido;

    @Column(name = "vuelto", precision = 10, scale = 2)
    private BigDecimal vuelto;

    @Column(name = "numero_operacion", length = 30)
    private String numeroOperacion;

    @Column(name = "pago_validado", nullable = false)
    private Boolean pagoValidado;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    public Integer getIdPago() { return idPago; }
    public void setIdPago(Integer idPago) { this.idPago = idPago; }
    public VentaModel getVenta() { return venta; }
    public void setVenta(VentaModel venta) { this.venta = venta; }
    public TipoPagoModel getMetodo() { return metodo; }
    public void setMetodo(TipoPagoModel metodo) { this.metodo = metodo; }
    public String getCanalTransferencia() { return canalTransferencia; }
    public void setCanalTransferencia(String canalTransferencia) { this.canalTransferencia = canalTransferencia; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public BigDecimal getMontoRecibido() { return montoRecibido; }
    public void setMontoRecibido(BigDecimal montoRecibido) { this.montoRecibido = montoRecibido; }
    public BigDecimal getVuelto() { return vuelto; }
    public void setVuelto(BigDecimal vuelto) { this.vuelto = vuelto; }
    public String getNumeroOperacion() { return numeroOperacion; }
    public void setNumeroOperacion(String numeroOperacion) { this.numeroOperacion = numeroOperacion; }
    public Boolean getPagoValidado() { return pagoValidado; }
    public void setPagoValidado(Boolean pagoValidado) { this.pagoValidado = pagoValidado; }
    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }
}
