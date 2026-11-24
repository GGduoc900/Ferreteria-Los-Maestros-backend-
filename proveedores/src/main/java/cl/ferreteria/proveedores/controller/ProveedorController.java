package cl.ferreteria.proveedores.controller;

import cl.ferreteria.proveedores.dto.ProveedorRequest;
import cl.ferreteria.proveedores.model.Proveedor;
import cl.ferreteria.proveedores.service.ProveedorService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/proveedores")
public class ProveedorController {
    private final ProveedorService service;

    public ProveedorController(ProveedorService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Proveedor> crear(@RequestBody ProveedorRequest body) {
        return ResponseEntity.status(201).body(service.crear(body));
    }

    @GetMapping
    public List<Proveedor> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Proveedor obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PutMapping("/{id}")
    public Proveedor editar(@PathVariable Long id, @RequestBody ProveedorRequest body) {
        return service.editar(id, body);
    }
}
