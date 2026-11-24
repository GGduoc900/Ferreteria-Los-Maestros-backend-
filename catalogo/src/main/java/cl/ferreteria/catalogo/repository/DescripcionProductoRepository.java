package cl.ferreteria.catalogo.repository;

import cl.ferreteria.catalogo.model.DescripcionProducto;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DescripcionProductoRepository extends JpaRepository<DescripcionProducto, String> {}
