package cl.ferreteria.pagos.repository;

import cl.ferreteria.pagos.model.Pago;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByCorreoClienteOrderByFechaDesc(String correoCliente);
}
