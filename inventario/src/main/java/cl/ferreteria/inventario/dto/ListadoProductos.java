package cl.ferreteria.inventario.dto;

import cl.ferreteria.inventario.model.Producto;

import java.util.List;

public record ListadoProductos(int total, List<Producto> productos) {}
