package cl.ferreteria.ventas.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Tablas venta y venta_item, creadas por V1__crear_tablas_venta.sql. */
@Entity
@Table(name = "venta")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String folio;

    @Column(name = "correo_cliente")
    private String correoCliente;

    @Enumerated(EnumType.STRING)
    private EstadoVenta estado;

    private Instant fecha;
    private Integer neto;
    private Integer iva;
    private Integer total;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "venta_item", joinColumns = @JoinColumn(name = "venta_id"))
    private List<ItemVenta> items = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }
    public String getCorreoCliente() { return correoCliente; }
    public void setCorreoCliente(String correoCliente) { this.correoCliente = correoCliente; }
    public EstadoVenta getEstado() { return estado; }
    public void setEstado(EstadoVenta estado) { this.estado = estado; }
    public Instant getFecha() { return fecha; }
    public void setFecha(Instant fecha) { this.fecha = fecha; }
    public Integer getNeto() { return neto; }
    public void setNeto(Integer neto) { this.neto = neto; }
    public Integer getIva() { return iva; }
    public void setIva(Integer iva) { this.iva = iva; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    public List<ItemVenta> getItems() { return items; }
    public void setItems(List<ItemVenta> items) { this.items = items; }
}
