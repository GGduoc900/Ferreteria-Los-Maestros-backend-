package cl.ferreteria.catalogo.controller;

import cl.ferreteria.catalogo.dto.DescripcionRequest;
import cl.ferreteria.catalogo.dto.DetalleProducto;
import cl.ferreteria.catalogo.dto.ListadoCatalogo;
import cl.ferreteria.catalogo.model.DescripcionProducto;
import cl.ferreteria.catalogo.service.CatalogoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Catálogo", description = "Vitrina pública de productos (lectura sin sesión)")
public class CatalogoController {
    private final CatalogoService service;

    public CatalogoController(CatalogoService service) {
        this.service = service;
    }

    @GetMapping("/productos")
    @Operation(summary = "Listar productos del catálogo", description = "Filtros: texto (ignora tildes), categoria, disponible")
    public ListadoCatalogo listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Boolean disponible) {
        return service.listar(texto, categoria, disponible);
    }

    @GetMapping("/productos/buscar")
    @Operation(summary = "Buscar por texto")
    public ListadoCatalogo buscar(@RequestParam String texto) {
        return service.listar(texto, null, null);
    }

    @GetMapping("/productos/categoria/{categoria}")
    @Operation(summary = "Productos de una categoría")
    public ListadoCatalogo porCategoria(@PathVariable String categoria) {
        return service.listar(null, categoria, null);
    }

    @GetMapping("/productos/{codigo}")
    @Operation(summary = "Detalle público de un producto")
    public DetalleProducto detalle(@PathVariable String codigo) {
        return service.detalle(codigo);
    }

    @GetMapping("/categorias")
    @Operation(summary = "Categorías disponibles")
    public List<String> categorias() {
        return service.categorias();
    }

    @PutMapping("/productos/{codigo}/descripcion")
    @Operation(summary = "Editar la descripción comercial (solo ADMIN)")
    public DescripcionProducto editarDescripcion(@PathVariable String codigo, @RequestBody DescripcionRequest body) {
        return service.editarDescripcion(codigo, body.descripcion());
    }
}
