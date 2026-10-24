package pe.edu.upn.sowad.posrosita.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pe.edu.upn.sowad.posrosita.model.ClienteModel;

@Repository
public interface IClienteRepository extends JpaRepository<ClienteModel, Integer> {
    // HU-05: autocompletado por documento
    Optional<ClienteModel> findByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);
}
