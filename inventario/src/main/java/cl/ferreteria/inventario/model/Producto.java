package cl.ferreteria.inventario.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Producto del inventario. Tabla creada por la migración V1__crear_tabla_producto.sql. */
@Entity
@Table(name = "producto")
public class Producto {
    @Id private String codigo;
    private String imagen, categoria, subcategoria, nombre, marca, unidad;
    private Integer precioCompra, precioVenta, stock, stockMinimo;

    public Producto() {}

    public Producto(
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
            Integer stockMinimo) {
        this.codigo = codigo;
        this.imagen = imagen;
        this.categoria = categoria;
        this.subcategoria = subcategoria;
        this.nombre = nombre;
        this.marca = marca;
        this.unidad = unidad;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String x) {
        codigo = x;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String x) {
        imagen = x;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String x) {
        categoria = x;
    }

    public String getSubcategoria() {
        return subcategoria;
    }

    public void setSubcategoria(String x) {
        subcategoria = x;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String x) {
        nombre = x;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String x) {
        marca = x;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String x) {
        unidad = x;
    }

    public Integer getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(Integer x) {
        precioCompra = x;
    }

    public Integer getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(Integer x) {
        precioVenta = x;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer x) {
        stock = x;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer x) {
        stockMinimo = x;
    }
}
