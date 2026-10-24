package pe.edu.upn.sowad.posrosita.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tipo_pago")
public class TipoPagoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metodo")
    private Integer idMetodo;

    @Column(name = "metodo_pago", nullable = false, length = 20, unique = true)
    private String metodoPago;

    public Integer getIdMetodo() { return idMetodo; }
    public void setIdMetodo(Integer idMetodo) { this.idMetodo = idMetodo; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
}
