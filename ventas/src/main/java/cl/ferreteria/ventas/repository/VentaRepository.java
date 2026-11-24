package cl.ferreteria.ventas.repository;

import cl.ferreteria.ventas.model.Venta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findByCorreoClienteOrderByFechaDesc(String correoCliente);

    List<Venta> findAllByOrderByFechaDesc();
}
