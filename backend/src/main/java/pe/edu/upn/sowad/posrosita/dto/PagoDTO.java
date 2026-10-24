package pe.edu.upn.sowad.posrosita.dto;

import java.math.BigDecimal;

public class PagoDTO {
    private String metodo;              // Efectivo | Transferencia
    private String canalTransferencia;  // Yape | Plin | Bancaria (solo transferencia)
    private String numeroOperacion;
    private BigDecimal montoRecibido;
    private BigDecimal vuelto;
    private Boolean pagoValidado;

    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public String getCanalTransferencia() { return canalTransferencia; }
    public void setCanalTransferencia(String canalTransferencia) { this.canalTransferencia = canalTransferencia; }
    public String getNumeroOperacion() { return numeroOperacion; }
    public void setNumeroOperacion(String numeroOperacion) { this.numeroOperacion = numeroOperacion; }
    public BigDecimal getMontoRecibido() { return montoRecibido; }
    public void setMontoRecibido(BigDecimal montoRecibido) { this.montoRecibido = montoRecibido; }
    public BigDecimal getVuelto() { return vuelto; }
    public void setVuelto(BigDecimal vuelto) { this.vuelto = vuelto; }
    public Boolean getPagoValidado() { return pagoValidado; }
    public void setPagoValidado(Boolean pagoValidado) { this.pagoValidado = pagoValidado; }
}
