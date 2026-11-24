package cl.ferreteria.inventario.repository;

import cl.ferreteria.inventario.model.Producto;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, String> {

    /**
     * Lee el producto bloqueando su fila hasta que termine la transacción (SELECT ... FOR UPDATE),
     * para que dos ventas simultáneas no descuenten el mismo stock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.codigo = :codigo")
    Optional<Producto> bloquearPorCodigo(@Param("codigo") String codigo);
}
