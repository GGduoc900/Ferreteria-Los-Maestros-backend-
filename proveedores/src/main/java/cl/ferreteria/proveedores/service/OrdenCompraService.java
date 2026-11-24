package cl.ferreteria.proveedores.service;

import cl.ferreteria.proveedores.dto.ItemOrdenRequest;
import cl.ferreteria.proveedores.dto.OrdenCompraRequest;
import cl.ferreteria.proveedores.model.ItemOrdenCompra;
import cl.ferreteria.proveedores.model.OrdenCompra;
import cl.ferreteria.proveedores.repository.OrdenCompraRepository;
import cl.ferreteria.proveedores.repository.ProveedorRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class OrdenCompraService {
    private final OrdenCompraRepository repo;
    private final ProveedorRepository proveedores;

    public OrdenCompraService(OrdenCompraRepository repo, ProveedorRepository proveedores) {
        this.repo = repo;
        this.proveedores = proveedores;
    }

    @Transactional
    public OrdenCompra emitir(OrdenCompraRequest request) {
        if (request.idProveedor() == null || !proveedores.existsById(request.idProveedor())) {
            throw new NoSuchElementException("Proveedor no encontrado");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("La orden debe tener al menos un producto");
        }
        for (ItemOrdenRequest i : request.items()) {
            if (i.codigo() == null || i.cantidad() == null || i.cantidad() <= 0 || i.costoUnitario() == null || i.costoUnitario() < 0) {
                throw new IllegalArgumentException("Cada producto necesita código, cantidad > 0 y costo unitario ≥ 0");
            }
        }
        OrdenCompra orden = new OrdenCompra();
        orden.setIdProveedor(request.idProveedor());
        orden.setEstado("EMITIDA");
        orden.setFecha(Instant.now());
        List<ItemOrdenCompra> items = new ArrayList<>();
        if (request.items() != null) {
            for (ItemOrdenRequest i : request.items()) {
                items.add(new ItemOrdenCompra(i.codigo(), i.cantidad(), i.costoUnitario()));
            }
        }
        orden.setItems(items);
        return repo.save(orden);
    }

    public List<OrdenCompra> listar() {
        return repo.findAll();
    }
}
