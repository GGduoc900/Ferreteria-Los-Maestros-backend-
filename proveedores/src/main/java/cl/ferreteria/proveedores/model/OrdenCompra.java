package cl.ferreteria.proveedores.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orden_compra")
public class OrdenCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_proveedor")
    private Long idProveedor;
    private String estado;
    private Instant fecha;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "orden_compra_item", joinColumns = @JoinColumn(name = "orden_compra_id"))
    private List<ItemOrdenCompra> items = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Long idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }

    public List<ItemOrdenCompra> getItems() {
        return items;
    }

    public void setItems(List<ItemOrdenCompra> items) {
        this.items = items;
    }
}
