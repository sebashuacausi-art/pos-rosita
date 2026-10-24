package pe.edu.upn.sowad.posrosita.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.upn.sowad.posrosita.model.ProductoModel;
import pe.edu.upn.sowad.posrosita.model.StockModel;
import pe.edu.upn.sowad.posrosita.service.ProductoService;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // HU-03: listado general (con su stock) para el catalogo del POS
    @GetMapping
    public List<Map<String, Object>> listar() {
        return armar(productoService.listarTodos());
    }

    // HU-03: busqueda por nombre o SKU
    @GetMapping("/buscar")
    public List<Map<String, Object>> buscar(@RequestParam(name = "q", required = false) String q) {
        return armar(productoService.buscar(q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> porId(@PathVariable Integer id) {
        Optional<ProductoModel> opt = productoService.porId(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(uno(opt.get()));
    }

    // stock disponible de un producto (cantidad_actual - cantidad_reservada)
    @GetMapping("/{id}/stock")
    public ResponseEntity<Map<String, Object>> stock(@PathVariable Integer id) {
        Optional<StockModel> opt = productoService.stockDe(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        StockModel s = opt.get();
        int disponible = s.getCantidadActual() - s.getCantidadReservada();
        return ResponseEntity.ok(Map.of(
                "cantidadActual", s.getCantidadActual(),
                "cantidadReservada", s.getCantidadReservada(),
                "stockMinimo", s.getStockMinimo(),
                "disponible", disponible));
    }

    private List<Map<String, Object>> armar(List<ProductoModel> lista) {
        List<Map<String, Object>> salida = new ArrayList<>();
        for (ProductoModel p : lista) {
            salida.add(uno(p));
        }
        return salida;
    }

    private Map<String, Object> uno(ProductoModel p) {
        int disponible = 0;
        int reservado = 0;
        int minimo = 0;
        Optional<StockModel> s = productoService.stockDe(p.getIdProducto());
        if (s.isPresent()) {
            reservado = s.get().getCantidadReservada();
            minimo = s.get().getStockMinimo();
            disponible = s.get().getCantidadActual() - reservado;
        }
        return Map.of(
                "idProducto", p.getIdProducto(),
                "codigoProducto", p.getCodigoProducto(),
                "nombreProducto", p.getNombreProducto(),
                "descripcion", p.getDescripcion(),
                "precioUnitario", p.getPrecioUnitario(),
                "idCategoria", p.getCategoria().getIdCategoria(),
                "categoria", p.getCategoria().getNombreCategoria(),
                "disponible", disponible,
                "cantidadReservada", reservado,
                "stockMinimo", minimo);
    }
}
