package pe.edu.upn.sowad.posrosita.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pe.edu.upn.sowad.posrosita.model.StockModel;

@Repository
public interface IStockRepository extends JpaRepository<StockModel, Integer> {
    Optional<StockModel> findByProductoIdProducto(Integer idProducto);
}
