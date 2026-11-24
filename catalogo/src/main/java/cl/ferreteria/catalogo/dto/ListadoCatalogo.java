package cl.ferreteria.catalogo.dto;

import java.util.List;

public record ListadoCatalogo(int total, List<DetalleProducto> productos) {}
