package pe.edu.upn.sowad.posrosita.dto;

import java.math.BigDecimal;

public class DetalleVentaDTO {
    private Integer idProducto;
    private Integer cantidad;
    private BigDecimal descuento;

    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }
}
