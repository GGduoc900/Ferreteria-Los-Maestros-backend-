package cl.ferreteria.proveedores.repository;

import cl.ferreteria.proveedores.model.Proveedor;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    boolean existsByRut(String rut);
}
