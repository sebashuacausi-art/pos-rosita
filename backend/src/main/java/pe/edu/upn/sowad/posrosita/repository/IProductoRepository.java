package pe.edu.upn.sowad.posrosita.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pe.edu.upn.sowad.posrosita.model.ProductoModel;

@Repository
public interface IProductoRepository extends JpaRepository<ProductoModel, Integer> {
    List<ProductoModel> findByNombreProductoContainingIgnoreCaseOrCodigoProductoContainingIgnoreCase(
            String nombre, String codigo);
    List<ProductoModel> findByCategoriaIdCategoria(Integer idCategoria);
}
