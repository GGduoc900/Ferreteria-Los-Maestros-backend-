package cl.ferreteria.inventario.controller;

import cl.ferreteria.inventario.dto.AjusteStockMinimoRequest;
import cl.ferreteria.inventario.dto.AjusteStockRequest;
import cl.ferreteria.inventario.dto.ListadoProductos;
import cl.ferreteria.inventario.dto.StockProducto;
import cl.ferreteria.inventario.model.Producto;
import cl.ferreteria.inventario.service.ProductoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/productos")
@Tag(name = "Productos", description = "CRUD de productos del inventario (solo ADMIN)")
public class ProductoController {
    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar productos", description = "Filtros opcionales: categoria, texto y soloStockBajo")
    public ListadoProductos listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String texto,
            @RequestParam(defaultValue = "false") boolean soloStockBajo) {
        List<Producto> productos = service.listar(categoria, texto, soloStockBajo);
        return new ListadoProductos(productos.size(), productos);
    }

    @GetMapping("/{codigo}")
    @Operation(summary = "Obtener un producto por código")
    @ApiResponse(responseCode = "404", description = "No existe")
    public Producto obtener(@PathVariable String codigo) {
        return service.obtener(codigo);
    }

    @PostMapping
    @Operation(summary = "Crear producto")
    @ApiResponse(responseCode = "201", description = "Creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "409", description = "El código ya existe")
    public ResponseEntity<Producto> crear(@RequestBody Producto producto) {
        Producto creado = service.crear(producto);
        return ResponseEntity.created(URI.create("/productos/" + creado.getCodigo())).body(creado);
    }

    @PutMapping("/{codigo}")
    @Operation(summary = "Actualizar producto (el código no cambia)")
    public Producto actualizar(@PathVariable String codigo, @RequestBody Producto producto) {
        return service.actualizar(codigo, producto);
    }

    @DeleteMapping("/{codigo}")
    @Operation(summary = "Eliminar producto")
    @ApiResponse(responseCode = "204", description = "Eliminado")
    public ResponseEntity<Void> eliminar(@PathVariable String codigo) {
        service.eliminar(codigo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{codigo}/stock")
    @Operation(summary = "Consultar stock y si está bajo el mínimo")
    public StockProducto consultarStock(@PathVariable String codigo) {
        return service.consultarStock(codigo);
    }

    @PutMapping("/{codigo}/stock")
    @Operation(summary = "Ajuste manual de stock")
    public StockProducto ajustarStock(@PathVariable String codigo, @RequestBody AjusteStockRequest body) {
        return service.ajustarStock(codigo, body.stock());
    }

    @PutMapping("/{codigo}/stock-minimo")
    @Operation(summary = "Cambiar el stock mínimo")
    public StockProducto ajustarStockMinimo(
            @PathVariable String codigo, @RequestBody AjusteStockMinimoRequest body) {
        return service.ajustarStockMinimo(codigo, body.stockMinimo());
    }
}
