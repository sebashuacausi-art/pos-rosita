package pe.edu.upn.sowad.posrosita.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pe.edu.upn.sowad.posrosita.model.ProductoModel;
import pe.edu.upn.sowad.posrosita.model.StockModel;
import pe.edu.upn.sowad.posrosita.repository.IProductoRepository;
import pe.edu.upn.sowad.posrosita.repository.IStockRepository;

@Service
public class ProductoService {

    @Autowired
    private IProductoRepository productoRepository;

    @Autowired
    private IStockRepository stockRepository;

    public List<ProductoModel> listarTodos() {
        return productoRepository.findAll();
    }

    // HU-03: busqueda por nombre o SKU (coincidencia parcial, sin tildes ni mayusculas)
    public List<ProductoModel> buscar(String texto) {
        String limpio = texto == null ? "" : texto.trim();
        if (limpio.isEmpty()) {
            return productoRepository.findAll();
        }
        return productoRepository
                .findByNombreProductoContainingIgnoreCaseOrCodigoProductoContainingIgnoreCase(limpio, limpio);
    }

    public List<ProductoModel> porCategoria(Integer idCategoria) {
        return productoRepository.findByCategoriaIdCategoria(idCategoria);
    }

    public Optional<ProductoModel> porId(Integer id) {
        return productoRepository.findById(id);
    }

    public Optional<StockModel> stockDe(Integer idProducto) {
        return stockRepository.findByProductoIdProducto(idProducto);
    }
}
