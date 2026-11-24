package cl.ferreteria.notificaciones.model;

import jakarta.persistence.*;

import java.time.Instant;

/** Tabla notificacion, creada por V1__crear_tabla_notificacion.sql. */
@Entity
@Table(name = "notificacion")
public class Notificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoNotificacion tipo;

    private String nombre;
    private String correo;
    private String telefono;
    private String para;
    private String asunto;

    @Column(length = 2000)
    private String mensaje;

    private String estado;
    private Instant fecha;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TipoNotificacion getTipo() { return tipo; }
    public void setTipo(TipoNotificacion tipo) { this.tipo = tipo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getPara() { return para; }
    public void setPara(String para) { this.para = para; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Instant getFecha() { return fecha; }
    public void setFecha(Instant fecha) { this.fecha = fecha; }
}
