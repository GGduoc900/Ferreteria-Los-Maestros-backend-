package cl.ferreteria.pagos.service;

import cl.ferreteria.pagos.dto.CambioEstadoVenta;
import cl.ferreteria.pagos.dto.MetodoPagoDto;
import cl.ferreteria.pagos.dto.PagoRequest;
import cl.ferreteria.pagos.dto.ResultadoValidacion;
import cl.ferreteria.pagos.dto.VentaRemota;
import cl.ferreteria.pagos.model.EstadoPago;
import cl.ferreteria.pagos.model.Pago;
import cl.ferreteria.pagos.repository.PagoRepository;
import cl.ferreteria.shared.seguridad.UsuarioActual;
import cl.ferreteria.shared.web.ClienteInterno;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Pasarela de pago simulada (no se conecta a un banco), integrada con ventas:
 *
 * <ol>
 *   <li>Consulta la venta a ventas: debe existir, ser del cliente y estar PENDIENTE_PAGO.
 *   <li>El monto debe ser exactamente el total de la venta.
 *   <li>Efectivo y tarjeta quedan APROBADO y la venta pasa a PAGADA. La transferencia queda
 *       PENDIENTE (exige número de referencia) hasta que un ADMIN la confirme.
 * </ol>
 */
@Service
@Transactional(readOnly = true)
public class PagoService {
    private static final List<MetodoPagoDto> METODOS =
            List.of(
                    new MetodoPagoDto("EFECTIVO", "Efectivo", true),
                    new MetodoPagoDto("TARJETA_DEBITO", "Tarjeta de débito", true),
                    new MetodoPagoDto("TARJETA_CREDITO", "Tarjeta de crédito", true),
                    new MetodoPagoDto("TRANSFERENCIA", "Transferencia", true));

    private final PagoRepository repo;
    private final ClienteInterno cliente;
    private final RestClient ventas;

    public PagoService(PagoRepository repo, ClienteInterno cliente, @Value("${servicios.ventas.url}") String urlVentas) {
        this.repo = repo;
        this.cliente = cliente;
        this.ventas = cliente.para(urlVentas);
    }

    public List<MetodoPagoDto> metodosPago() {
        return METODOS;
    }

    public ResultadoValidacion validar(PagoRequest request, UsuarioActual usuario) {
        VentaRemota venta = ventaPagable(request, usuario);
        return new ResultadoValidacion(true, estadoSegunMetodo(request).name(), venta.total());
    }

    @Transactional
    public Pago crear(PagoRequest request, UsuarioActual usuario) {
        VentaRemota venta = ventaPagable(request, usuario);
        Pago pago = new Pago();
        pago.setIdVenta(venta.id());
        pago.setCorreoCliente(venta.correoCliente());
        pago.setMetodo(request.metodo().toUpperCase());
        pago.setMonto(venta.total());
        pago.setReferencia(request.referencia());
        pago.setEstado(estadoSegunMetodo(request));
        pago.setFecha(Instant.now());
        pago = repo.save(pago);
        if (pago.getEstado() == EstadoPago.APROBADO) marcarVentaPagada(venta.id());
        return pago;
    }

    /** ADMIN confirma (o rechaza) una transferencia pendiente. */
    @Transactional
    public Pago cambiarEstado(Long id, String estado) {
        Pago pago = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Pago no encontrado"));
        if (pago.getEstado() != EstadoPago.PENDIENTE) throw new IllegalStateException("El pago ya fue " + pago.getEstado());
        EstadoPago nuevo;
        try {
            nuevo = EstadoPago.valueOf(estado == null ? "" : estado.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado no válido: usa APROBADO o RECHAZADO");
        }
        pago.setEstado(nuevo);
        pago = repo.save(pago);
        if (nuevo == EstadoPago.APROBADO) marcarVentaPagada(pago.getIdVenta());
        return pago;
    }

    public List<Pago> listar(UsuarioActual usuario) {
        return usuario.puedeVerTodo() ? repo.findAll() : repo.findByCorreoClienteOrderByFechaDesc(usuario.correo());
    }

    public Pago obtener(Long id, UsuarioActual usuario) {
        Pago pago = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Pago no encontrado"));
        if (!usuario.puedeVerTodo() && !pago.getCorreoCliente().equalsIgnoreCase(usuario.correo())) {
            throw new NoSuchElementException("Pago no encontrado");
        }
        return pago;
    }

    // ------------------------------------------------------------------ apoyo

    private VentaRemota ventaPagable(PagoRequest r, UsuarioActual usuario) {
        if (r.idVenta() == null) throw new IllegalArgumentException("Falta el campo idVenta");
        if (r.metodo() == null || METODOS.stream().noneMatch(m -> m.codigo().equalsIgnoreCase(r.metodo()))) {
            throw new IllegalArgumentException("Método de pago no válido");
        }
        if ("TRANSFERENCIA".equalsIgnoreCase(r.metodo()) && (r.referencia() == null || r.referencia().isBlank())) {
            throw new IllegalArgumentException("La transferencia requiere número de referencia");
        }
        VentaRemota venta =
                cliente.llamar("ventas", () -> ventas.get().uri("/ventas/{id}", r.idVenta()).retrieve().body(VentaRemota.class));
        if (venta == null) throw new NoSuchElementException("Venta no encontrada");
        if (!usuario.puedeVerTodo() && !venta.correoCliente().equalsIgnoreCase(usuario.correo())) {
            throw new NoSuchElementException("Venta no encontrada");
        }
        if (!"PENDIENTE_PAGO".equals(venta.estado())) {
            throw new IllegalStateException("La venta " + venta.folio() + " no está pendiente de pago (" + venta.estado() + ")");
        }
        if (r.monto() != null && !r.monto().equals(venta.total())) {
            throw new IllegalArgumentException("El monto debe ser el total de la venta: " + venta.total());
        }
        return venta;
    }

    private static EstadoPago estadoSegunMetodo(PagoRequest r) {
        return "TRANSFERENCIA".equalsIgnoreCase(r.metodo()) ? EstadoPago.PENDIENTE : EstadoPago.APROBADO;
    }

    private void marcarVentaPagada(Long idVenta) {
        cliente.llamar(
                "ventas",
                () ->
                        ventas.put()
                                .uri("/ventas/{id}/estado", idVenta)
                                .body(new CambioEstadoVenta("PAGADA", "Pago aprobado"))
                                .retrieve()
                                .toBodilessEntity());
    }
}
