package cl.ferreteria.proveedores.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ItemOrdenCompra {
    private String codigo;
    private Integer cantidad;
    @Column(name = "costo_unitario")
    private Integer costoUnitario;

    public ItemOrdenCompra() {}

    public ItemOrdenCompra(String codigo, Integer cantidad, Integer costoUnitario) {
        this.codigo = codigo;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(Integer costoUnitario) {
        this.costoUnitario = costoUnitario;
    }
}
