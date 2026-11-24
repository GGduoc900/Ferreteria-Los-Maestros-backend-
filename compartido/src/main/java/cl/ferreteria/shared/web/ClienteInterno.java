package cl.ferreteria.shared.web;

import cl.ferreteria.shared.seguridad.GeneradorTokens;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Crea clientes HTTP (RestClient) para la comunicación entre microservicios.
 *
 * <ul>
 *   <li>Timeouts cortos (2 s conexión, 5 s lectura) para no bloquear al usuario.
 *   <li>Agrega automáticamente un JWT técnico con rol SERVICIO.
 *   <li>{@link #llamar} traduce los errores remotos: 404 → NoSuchElementException, 400/409 →
 *       IllegalArgument/IllegalState con el mismo mensaje, caída o timeout → 503.
 * </ul>
 */
@Component
public class ClienteInterno {

    private static final Pattern CAMPO_ERROR = Pattern.compile("\"error\"\\s*:\\s*\"([^\"]*)\"");

    private final GeneradorTokens tokens;
    private final String nombreServicio;

    public ClienteInterno(
            GeneradorTokens tokens, @Value("${spring.application.name}") String nombreServicio) {
        this.tokens = tokens;
        this.nombreServicio = nombreServicio;
    }

    public RestClient para(String urlBase) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(2));
        fabrica.setReadTimeout(Duration.ofSeconds(5));
        return RestClient.builder()
                .baseUrl(urlBase)
                .requestFactory(fabrica)
                .requestInterceptor(
                        (peticion, cuerpo, ejecucion) -> {
                            peticion.getHeaders().setBearerAuth(tokens.tokenServicio(nombreServicio));
                            return ejecucion.execute(peticion, cuerpo);
                        })
                .build();
    }

    public <T> T llamar(String servicioDestino, Supplier<T> llamada) {
        try {
            return llamada.get();
        } catch (HttpClientErrorException e) {
            String mensaje = mensajeRemoto(e);
            if (e.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) throw new NoSuchElementException(mensaje);
            if (e.getStatusCode().isSameCodeAs(HttpStatus.CONFLICT)) throw new IllegalStateException(mensaje);
            throw new IllegalArgumentException(mensaje);
        } catch (ResourceAccessException e) {
            throw new ServicioNoDisponibleException(
                    "El servicio de " + servicioDestino + " no está disponible. Intenta más tarde.", e);
        } catch (RestClientResponseException e) {
            throw new ServicioNoDisponibleException(
                    "El servicio de " + servicioDestino + " respondió con error " + e.getStatusCode().value(), e);
        }
    }

    private static String mensajeRemoto(RestClientResponseException e) {
        Matcher m = CAMPO_ERROR.matcher(e.getResponseBodyAsString());
        return m.find() ? m.group(1) : e.getStatusText();
    }
}
