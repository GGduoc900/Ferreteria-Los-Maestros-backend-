package cl.ferreteria.ventas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Línea de la venta. Guarda nombre y precio del momento (si el precio cambia, la venta no). */
@Embeddable
public class ItemVenta {
    private String codigo;
    private String nombre;
    private Integer cantidad;

    @Column(name = "precio_unitario")
    private Integer precioUnitario;

    private Integer subtotal;

    public ItemVenta() {}

    public ItemVenta(String codigo, String nombre, Integer cantidad, Integer precioUnitario) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = cantidad * precioUnitario;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Integer getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Integer precioUnitario) { this.precioUnitario = precioUnitario; }
    public Integer getSubtotal() { return subtotal; }
    public void setSubtotal(Integer subtotal) { this.subtotal = subtotal; }
}
