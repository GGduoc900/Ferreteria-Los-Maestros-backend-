package cl.ferreteria.ventas.service;

import cl.ferreteria.shared.seguridad.UsuarioActual;
import cl.ferreteria.shared.web.ClienteInterno;
import cl.ferreteria.ventas.dto.CrearVentaRequest;
import cl.ferreteria.ventas.dto.ItemRequest;
import cl.ferreteria.ventas.dto.LineaStock;
import cl.ferreteria.ventas.dto.MovimientoStockRequest;
import cl.ferreteria.ventas.model.EstadoVenta;
import cl.ferreteria.ventas.model.ItemVenta;
import cl.ferreteria.ventas.model.Venta;
import cl.ferreteria.ventas.repository.VentaRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Lógica de ventas.
 *
 * <ul>
 *   <li>Al crear: inventario valida y descuenta el stock de todos los productos en una sola
 *       operación y devuelve los precios vigentes (el cliente nunca envía precios).
 *   <li>Los precios incluyen IVA (19 %): neto = total / 1,19 y IVA = total − neto.
 *   <li>El dueño de la venta es el correo que viene en el JWT. Un CLIENTE solo ve y anula las suyas.
 *   <li>Al anular, el stock vuelve a inventario.
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class VentaService {
    private static final double FACTOR_IVA = 1.19;

    private final VentaRepository repo;
    private final ClienteInterno cliente;
    private final RestClient inventario;

    public VentaService(
            VentaRepository repo, ClienteInterno cliente, @Value("${servicios.inventario.url}") String urlInventario) {
        this.repo = repo;
        this.cliente = cliente;
        this.inventario = cliente.para(urlInventario);
    }

    @Transactional
    public Venta crear(CrearVentaRequest request, UsuarioActual usuario) {
        List<ItemRequest> pedidos = request.items() != null ? request.items() : request.productos();
        if (pedidos == null || pedidos.isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un producto");
        }
        // 1) Inventario descuenta el stock (todo o nada) y entrega los precios vigentes
        List<LineaStock> lineas = moverStock("/stock/descontar", pedidos);

        // 2) Se arma la venta con esos precios
        Venta venta = new Venta();
        venta.setCorreoCliente(usuario.correo());
        venta.setEstado(EstadoVenta.PENDIENTE_PAGO);
        venta.setFecha(Instant.now());
        venta.setItems(new ArrayList<>(lineas.stream().map(l -> new ItemVenta(l.codigo(), l.nombre(), l.cantidad(), l.precioUnitario())).toList()));
        int total = venta.getItems().stream().mapToInt(ItemVenta::getSubtotal).sum();
        int neto = (int) Math.round(total / FACTOR_IVA);
        venta.setTotal(total);
        venta.setNeto(neto);
        venta.setIva(total - neto);

        try {
            venta = repo.save(venta);
            venta.setFolio(String.format("B-%06d", venta.getId()));
            return repo.save(venta);
        } catch (RuntimeException e) {
            // Compensación: si no se pudo guardar la venta, se devuelve el stock descontado
            moverStock("/stock/reponer", pedidos);
            throw e;
        }
    }

    public List<Venta> listar(UsuarioActual usuario) {
        return usuario.puedeVerTodo()
                ? repo.findAllByOrderByFechaDesc()
                : repo.findByCorreoClienteOrderByFechaDesc(usuario.correo());
    }

    public Venta obtener(Long id, UsuarioActual usuario) {
        Venta venta = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Venta no encontrada"));
        if (!usuario.puedeVerTodo() && !venta.getCorreoCliente().equalsIgnoreCase(usuario.correo())) {
            // Se responde 404 y no 403 para no revelar que la venta existe
            throw new NoSuchElementException("Venta no encontrada");
        }
        return venta;
    }

    /** Solo ADMIN (o el servicio de pagos). */
    @Transactional
    public Venta cambiarEstado(Long id, String estado, UsuarioActual usuario) {
        if (estado == null) throw new IllegalArgumentException("Falta el campo estado");
        Venta venta = obtener(id, usuario);
        EstadoVenta nuevo;
        try {
            nuevo = EstadoVenta.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de venta no válido: " + estado);
        }
        if (nuevo == EstadoVenta.ANULADA) return anular(id, usuario);
        if (venta.getEstado() == EstadoVenta.ANULADA || venta.getEstado() == EstadoVenta.ENTREGADA) {
            throw new IllegalStateException("La venta ya está " + venta.getEstado() + " y no puede cambiar");
        }
        venta.setEstado(nuevo);
        return repo.save(venta);
    }

    @Transactional
    public Venta anular(Long id, UsuarioActual usuario) {
        Venta venta = obtener(id, usuario);
        if (venta.getEstado() == EstadoVenta.ANULADA) throw new IllegalStateException("La venta ya está anulada");
        if (venta.getEstado() == EstadoVenta.ENTREGADA) throw new IllegalStateException("No se puede anular una venta entregada");
        if (!usuario.puedeVerTodo() && venta.getEstado() != EstadoVenta.PENDIENTE_PAGO) {
            throw new AccessDeniedException("Solo puedes anular ventas que aún no están pagadas");
        }
        moverStock(
                "/stock/reponer",
                venta.getItems().stream().map(i -> new ItemRequest(i.getCodigo(), i.getCantidad())).toList());
        venta.setEstado(EstadoVenta.ANULADA);
        return repo.save(venta);
    }

    public Venta comprobante(Long id, UsuarioActual usuario) {
        return obtener(id, usuario);
    }

    private List<LineaStock> moverStock(String ruta, List<ItemRequest> items) {
        List<LineaStock> lineas =
                cliente.llamar(
                        "inventario",
                        () ->
                                inventario
                                        .post()
                                        .uri(ruta)
                                        .body(new MovimientoStockRequest(items))
                                        .retrieve()
                                        .body(new ParameterizedTypeReference<List<LineaStock>>() {}));
        return lineas == null ? List.of() : lineas;
    }
}
