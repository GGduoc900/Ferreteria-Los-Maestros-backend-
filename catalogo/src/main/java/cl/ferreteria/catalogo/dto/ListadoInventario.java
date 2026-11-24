package cl.ferreteria.catalogo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ListadoInventario(int total, List<ProductoInventario> productos) {}
