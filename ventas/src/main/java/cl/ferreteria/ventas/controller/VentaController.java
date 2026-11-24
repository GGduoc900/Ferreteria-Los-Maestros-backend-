package cl.ferreteria.ventas.controller;

import cl.ferreteria.shared.seguridad.UsuarioActual;
import cl.ferreteria.ventas.dto.CambioEstadoRequest;
import cl.ferreteria.ventas.dto.CrearVentaRequest;
import cl.ferreteria.ventas.model.Venta;
import cl.ferreteria.ventas.service.VentaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ventas")
@Tag(name = "Ventas", description = "Registrar ventas y seguir su estado (requiere sesión)")
public class VentaController {
    private final VentaService service;

    public VentaController(VentaService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Registrar venta { items: [{ codigo, cantidad }] }")
    public ResponseEntity<Venta> crear(@RequestBody CrearVentaRequest body, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(201).body(service.crear(body, UsuarioActual.de(jwt)));
    }

    @GetMapping
    @Operation(summary = "Ventas: CLIENTE ve las suyas, ADMIN todas")
    public List<Venta> listar(@AuthenticationPrincipal Jwt jwt) {
        return service.listar(UsuarioActual.de(jwt));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una venta")
    public Venta obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.obtener(id, UsuarioActual.de(jwt));
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado del pedido (ADMIN)")
    public Venta cambiarEstado(
            @PathVariable Long id, @RequestBody CambioEstadoRequest body, @AuthenticationPrincipal Jwt jwt) {
        return service.cambiarEstado(id, body.estado(), UsuarioActual.de(jwt));
    }

    @PostMapping("/{id}/anular")
    @Operation(summary = "Anular venta y devolver el stock")
    public Venta anular(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.anular(id, UsuarioActual.de(jwt));
    }

    @GetMapping("/{id}/comprobante")
    @Operation(summary = "Comprobante (datos de la venta)")
    public Venta comprobante(
            @PathVariable Long id,
            @RequestParam(defaultValue = "json") String formato,
            @AuthenticationPrincipal Jwt jwt) {
        return service.comprobante(id, UsuarioActual.de(jwt));
    }
}
