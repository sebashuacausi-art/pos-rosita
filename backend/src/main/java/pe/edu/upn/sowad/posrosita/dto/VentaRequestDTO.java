package pe.edu.upn.sowad.posrosita.dto;

import java.util.List;

public class VentaRequestDTO {
    private Integer idSesion;
    private String correlativo;
    private Integer idCliente;
    private List<DetalleVentaDTO> detalles;
    private PagoDTO pago;

    public Integer getIdSesion() { return idSesion; }
    public void setIdSesion(Integer idSesion) { this.idSesion = idSesion; }
    public String getCorrelativo() { return correlativo; }
    public void setCorrelativo(String correlativo) { this.correlativo = correlativo; }
    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }
    public List<DetalleVentaDTO> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVentaDTO> detalles) { this.detalles = detalles; }
    public PagoDTO getPago() { return pago; }
    public void setPago(PagoDTO pago) { this.pago = pago; }
}
