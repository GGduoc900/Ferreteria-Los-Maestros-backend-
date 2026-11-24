package cl.ferreteria.catalogo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Forma en que inventario entrega un producto (GET /productos). Solo se usa internamente. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductoInventario(
        String codigo,
        String imagen,
        String categoria,
        String subcategoria,
        String nombre,
        String marca,
        String unidad,
        Integer precioCompra,
        Integer precioVenta,
        Integer stock,
        Integer stockMinimo) {}
