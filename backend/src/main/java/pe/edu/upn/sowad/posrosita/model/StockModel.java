package pe.edu.upn.sowad.posrosita.model;

import jakarta.persistence.*;

@Entity
@Table(name = "stock")
public class StockModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_stock")
    private Integer idStock;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_producto", nullable = false, unique = true)
    private ProductoModel producto;

    @Column(name = "cantidad_actual", nullable = false)
    private Integer cantidadActual;

    @Column(name = "cantidad_reservada", nullable = false)
    private Integer cantidadReservada;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    public Integer getIdStock() { return idStock; }
    public void setIdStock(Integer idStock) { this.idStock = idStock; }
    public ProductoModel getProducto() { return producto; }
    public void setProducto(ProductoModel producto) { this.producto = producto; }
    public Integer getCantidadActual() { return cantidadActual; }
    public void setCantidadActual(Integer cantidadActual) { this.cantidadActual = cantidadActual; }
    public Integer getCantidadReservada() { return cantidadReservada; }
    public void setCantidadReservada(Integer cantidadReservada) { this.cantidadReservada = cantidadReservada; }
    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }
}
