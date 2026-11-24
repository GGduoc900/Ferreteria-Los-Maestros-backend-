package cl.ferreteria.shared.seguridad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/** Firma tokens JWT (HS256). Lo usa usuarios para las sesiones y el resto para llamadas internas. */
@Component
public class GeneradorTokens {

    private final JwtEncoder encoder;
    private final String emisor;

    private String tokenServicio;
    private Instant vencimientoTokenServicio = Instant.EPOCH;

    public GeneradorTokens(JwtEncoder encoder, @Value("${jwt.emisor}") String emisor) {
        this.encoder = encoder;
        this.emisor = emisor;
    }

    public TokenEmitido generar(
            String sujeto, String rol, Map<String, Object> datosExtra, Duration duracion) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(duracion);
        JwtClaimsSet.Builder claims =
                JwtClaimsSet.builder()
                        .issuer(emisor)
                        .subject(sujeto)
                        .issuedAt(ahora)
                        .expiresAt(expira)
                        .claim(JwtConfig.CLAIM_ROL, rol);
        datosExtra.forEach(claims::claim);
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String valor = encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
        return new TokenEmitido(valor, expira);
    }

    /**
     * Token técnico con rol SERVICIO para que un microservicio llame a otro (por ejemplo, ventas →
     * inventario). Se reutiliza mientras le queden más de 30 segundos.
     */
    public synchronized String tokenServicio(String nombreServicio) {
        if (tokenServicio == null || Instant.now().isAfter(vencimientoTokenServicio.minusSeconds(30))) {
            TokenEmitido t =
                    generar("servicio:" + nombreServicio, SeguridadBase.SERVICIO, Map.of(), Duration.ofMinutes(5));
            tokenServicio = t.valor();
            vencimientoTokenServicio = t.expiraEn();
        }
        return tokenServicio;
    }

    public record TokenEmitido(String valor, Instant expiraEn) {}
}
