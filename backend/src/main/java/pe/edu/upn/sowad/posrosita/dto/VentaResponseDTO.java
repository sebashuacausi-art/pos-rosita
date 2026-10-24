package pe.edu.upn.sowad.posrosita.dto;

import java.math.BigDecimal;
import java.util.List;

import pe.edu.upn.sowad.posrosita.model.DetalleVentaModel;

public class VentaResponseDTO {
    private Integer idVenta;
    private String correlativo;
    private String estado;
    private BigDecimal subtotal;
    private BigDecimal descuentoTotal;
    private BigDecimal total;
    private Boolean tieneComprobante;
    private List<DetalleVentaModel> detalles;

    public Integer getIdVenta() { return idVenta; }
    public void setIdVenta(Integer idVenta) { this.idVenta = idVenta; }
    public String getCorrelativo() { return correlativo; }
    public void setCorrelativo(String correlativo) { this.correlativo = correlativo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDescuentoTotal() { return descuentoTotal; }
    public void setDescuentoTotal(BigDecimal descuentoTotal) { this.descuentoTotal = descuentoTotal; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public Boolean getTieneComprobante() { return tieneComprobante; }
    public void setTieneComprobante(Boolean tieneComprobante) { this.tieneComprobante = tieneComprobante; }
    public List<DetalleVentaModel> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVentaModel> detalles) { this.detalles = detalles; }
}
