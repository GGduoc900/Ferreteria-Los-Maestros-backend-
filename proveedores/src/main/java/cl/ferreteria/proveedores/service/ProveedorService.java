package cl.ferreteria.proveedores.service;

import cl.ferreteria.proveedores.dto.ProveedorRequest;
import cl.ferreteria.proveedores.model.Proveedor;
import cl.ferreteria.proveedores.repository.ProveedorRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class ProveedorService {
    private final ProveedorRepository repo;

    public ProveedorService(ProveedorRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Proveedor crear(ProveedorRequest request) {
        if (request.rut() == null || request.rut().isBlank() || request.nombre() == null || request.nombre().isBlank()) {
            throw new IllegalArgumentException("RUT y nombre son obligatorios");
        }
        if (repo.existsByRut(request.rut().trim())) throw new IllegalStateException("Ya existe un proveedor con ese RUT");
        Proveedor p = new Proveedor();
        copiarDatos(p, request);
        p.setEstado("ACTIVO");
        return repo.save(p);
    }

    public List<Proveedor> listar() {
        return repo.findAll();
    }

    public Proveedor obtener(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Proveedor no encontrado"));
    }

    /** Solo se modifican los campos enviados. */
    @Transactional
    public Proveedor editar(Long id, ProveedorRequest request) {
        Proveedor p = obtener(id);
        copiarDatos(p, request);
        return repo.save(p);
    }

    private void copiarDatos(Proveedor p, ProveedorRequest r) {
        if (r.rut() != null) p.setRut(r.rut());
        if (r.nombre() != null) p.setNombre(r.nombre());
        if (r.correo() != null) p.setCorreo(r.correo());
        if (r.telefono() != null) p.setTelefono(r.telefono());
    }
}
