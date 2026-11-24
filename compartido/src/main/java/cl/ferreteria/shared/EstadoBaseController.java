package cl.ferreteria.shared;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

public abstract class EstadoBaseController {
    private final Instant inicio = Instant.now();

    @Value("${server.port:8080}")
    protected int puerto;

    protected Map<String, Object> estado(String servicio, String descripcion) {
        var m = new LinkedHashMap<String, Object>();
        m.put("servicio", servicio);
        m.put("descripcion", descripcion);
        m.put("proyecto", "Ferretería Los Maestros");
        m.put("estado", "funcionando");
        m.put("puerto", puerto);
        m.put("uptimeSegundos", Duration.between(inicio, Instant.now()).toSeconds());
        return m;
    }
}
