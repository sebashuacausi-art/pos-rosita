package pe.edu.upn.sowad.posrosita.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pe.edu.upn.sowad.posrosita.model.CajaModel;

@Repository
public interface ICajaRepository extends JpaRepository<CajaModel, Integer> {
    Optional<CajaModel> findByNumeroCaja(Integer numeroCaja);
    List<CajaModel> findAllByOrderByNumeroCajaAsc();
}
