package pe.edu.upn.sowad.posrosita.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.upn.sowad.posrosita.model.ClienteModel;
import pe.edu.upn.sowad.posrosita.repository.IClienteRepository;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "http://localhost:4200")
public class ClienteController {

    @Autowired
    private IClienteRepository clienteRepository;

    // HU-05: autocompletado por tipo y numero de documento
    @GetMapping("/buscar")
    public ResponseEntity<Map<String, Object>> buscar(
            @RequestParam(name = "tipo", required = false) String tipo,
            @RequestParam(name = "numero", required = false) String numero) {

        Map<String, Object> resp = new HashMap<>();
        resp.put("encontrado", false);

        if (tipo == null || tipo.trim().isEmpty() || numero == null || numero.trim().isEmpty()) {
            return ResponseEntity.ok(resp);
        }

        Optional<ClienteModel> opt = clienteRepository
                .findByTipoDocumentoAndNumeroDocumento(tipo.trim(), numero.trim());

        if (opt.isPresent()) {
            ClienteModel c = opt.get();
            resp.put("encontrado", true);
            resp.put("idCliente", c.getIdCliente());
            resp.put("tipoDocumento", c.getTipoDocumento());
            resp.put("numeroDocumento", c.getNumeroDocumento());
            String nombre = c.getRazonSocial();
            if (nombre == null || nombre.trim().isEmpty()) {
                nombre = ((c.getNombreCliente() != null ? c.getNombreCliente() : "") + " "
                        + (c.getApellidoPaterno() != null ? c.getApellidoPaterno() : "") + " "
                        + (c.getApellidoMaterno() != null ? c.getApellidoMaterno() : "")).trim();
            }
            resp.put("nombre", nombre);
            resp.put("direccion", c.getDireccion());
        }
        return ResponseEntity.ok(resp);
    }
}
