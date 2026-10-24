package pe.edu.upn.sowad.posrosita.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pe.edu.upn.sowad.posrosita.model.TipoPagoModel;

@Repository
public interface ITipoPagoRepository extends JpaRepository<TipoPagoModel, Integer> {
    Optional<TipoPagoModel> findByMetodoPago(String metodoPago);
}
