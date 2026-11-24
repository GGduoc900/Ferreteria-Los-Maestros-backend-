package cl.ferreteria.pagos.model;

import jakarta.persistence.*;

import java.time.Instant;

/** Tabla pago, creada por V1__crear_tabla_pago.sql. */
@Entity
@Table(name = "pago")
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_venta")
    private Long idVenta;

    @Column(name = "correo_cliente")
    private String correoCliente;

    private String metodo;
    private Integer monto;
    private String referencia;

    @Enumerated(EnumType.STRING)
    private EstadoPago estado;

    private Instant fecha;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdVenta() { return idVenta; }
    public void setIdVenta(Long idVenta) { this.idVenta = idVenta; }
    public String getCorreoCliente() { return correoCliente; }
    public void setCorreoCliente(String correoCliente) { this.correoCliente = correoCliente; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public Integer getMonto() { return monto; }
    public void setMonto(Integer monto) { this.monto = monto; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public EstadoPago getEstado() { return estado; }
    public void setEstado(EstadoPago estado) { this.estado = estado; }
    public Instant getFecha() { return fecha; }
    public void setFecha(Instant fecha) { this.fecha = fecha; }
}
