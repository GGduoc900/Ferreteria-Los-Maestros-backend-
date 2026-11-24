package cl.ferreteria.proveedores.controller;

import cl.ferreteria.proveedores.dto.OrdenCompraRequest;
import cl.ferreteria.proveedores.model.OrdenCompra;
import cl.ferreteria.proveedores.service.OrdenCompraService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordenes-compra")
public class OrdenCompraController {
    private final OrdenCompraService service;

    public OrdenCompraController(OrdenCompraService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OrdenCompra> emitir(@RequestBody OrdenCompraRequest body) {
        return ResponseEntity.status(201).body(service.emitir(body));
    }

    @GetMapping
    public List<OrdenCompra> listar() {
        return service.listar();
    }
}
