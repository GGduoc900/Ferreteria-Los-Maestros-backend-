package cl.ferreteria.inventario.controller;

import cl.ferreteria.inventario.dto.LineaStock;
import cl.ferreteria.inventario.dto.MovimientoStockRequest;
import cl.ferreteria.inventario.dto.ResumenInventario;
import cl.ferreteria.inventario.service.ProductoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock")
@Tag(name = "Stock", description = "Operaciones de stock usadas por otros microservicios (rol SERVICIO o ADMIN)")
public class StockController {
    private final ProductoService service;

    public StockController(ProductoService service) {
        this.service = service;
    }

    @GetMapping("/resumen")
    @Operation(summary = "Totales del inventario: productos, stock bajo, agotados y valor a costo")
    public ResumenInventario resumen() {
        return service.resumen();
    }

    @PostMapping("/descontar")
    @Operation(summary = "Descontar stock de forma atómica (ventas)")
    public List<LineaStock> descontar(@RequestBody MovimientoStockRequest body) {
        return service.descontar(body.items());
    }

    @PostMapping("/reponer")
    @Operation(summary = "Devolver stock (anulación de venta o recepción de compra)")
    public List<LineaStock> reponer(@RequestBody MovimientoStockRequest body) {
        return service.reponer(body.items());
    }
}
