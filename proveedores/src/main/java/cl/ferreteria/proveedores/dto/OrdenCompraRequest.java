package cl.ferreteria.proveedores.dto;

import java.util.List;

public record OrdenCompraRequest(Long idProveedor, List<ItemOrdenRequest> items) {}
