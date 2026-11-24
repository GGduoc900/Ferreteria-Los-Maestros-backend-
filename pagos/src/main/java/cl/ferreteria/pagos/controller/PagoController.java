package cl.ferreteria.pagos.controller;

import cl.ferreteria.pagos.dto.CambioEstadoPago;
import cl.ferreteria.pagos.dto.MetodoPagoDto;
import cl.ferreteria.pagos.dto.PagoRequest;
import cl.ferreteria.pagos.dto.ResultadoValidacion;
import cl.ferreteria.pagos.model.Pago;
import cl.ferreteria.pagos.service.PagoService;
import cl.ferreteria.shared.seguridad.UsuarioActual;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pagos")
@Tag(name = "Pagos", description = "Pasarela simulada integrada con ventas")
public class PagoController {
    private final PagoService service;

    public PagoController(PagoService service) {
        this.service = service;
    }

    @GetMapping("/metodos-pago")
    @Operation(summary = "Métodos de pago activos (público)")
    public List<MetodoPagoDto> metodosPago() {
        return service.metodosPago();
    }

    @PostMapping
    @Operation(summary = "Pagar una venta { idVenta, metodo, monto?, referencia? }")
    public ResponseEntity<Pago> crear(@RequestBody PagoRequest body, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(201).body(service.crear(body, UsuarioActual.de(jwt)));
    }

    @GetMapping
    @Operation(summary = "Pagos: CLIENTE ve los suyos, ADMIN todos")
    public List<Pago> listar(@AuthenticationPrincipal Jwt jwt) {
        return service.listar(UsuarioActual.de(jwt));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un pago")
    public Pago obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.obtener(id, UsuarioActual.de(jwt));
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Confirmar o rechazar un pago pendiente (ADMIN)")
    public Pago cambiarEstado(@PathVariable Long id, @RequestBody CambioEstadoPago body) {
        return service.cambiarEstado(id, body.estado());
    }

    @PostMapping("/validar")
    @Operation(summary = "Revisa si el pago sería aceptado, sin registrarlo")
    public ResultadoValidacion validar(@RequestBody PagoRequest body, @AuthenticationPrincipal Jwt jwt) {
        return service.validar(body, UsuarioActual.de(jwt));
    }
}
