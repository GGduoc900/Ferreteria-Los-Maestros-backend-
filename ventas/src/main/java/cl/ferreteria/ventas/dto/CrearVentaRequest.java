package cl.ferreteria.ventas.dto;

import java.util.List;

/** Acepta la lista como "items" o, por compatibilidad con la versión anterior, "productos". */
public record CrearVentaRequest(List<ItemRequest> items, List<ItemRequest> productos) {}
