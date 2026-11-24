package cl.ferreteria.notificaciones.repository;

import cl.ferreteria.notificaciones.model.Notificacion;
import cl.ferreteria.notificaciones.model.TipoNotificacion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByTipoOrderByFechaDesc(TipoNotificacion tipo);

    List<Notificacion> findAllByOrderByFechaDesc();
}
