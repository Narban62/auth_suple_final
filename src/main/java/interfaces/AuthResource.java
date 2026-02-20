package interfaces;

import java.time.Instant;
import java.util.Set;

import application.UsuarioService;
import application.representation.UsuarioRepresentation;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/auth")
public class AuthResource {

    @Inject
    private UsuarioService usuarioService;

    @GET
    @Path("/token")
    public TokenResponse token(
            @QueryParam("username") String username,
            @QueryParam("password") String password,
            @QueryParam("rol") String rol) {

        // Validación de parámetros requeridos
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return new TokenResponse(null, 0, null);
        }

        // Buscar el usuario en la base de datos
        UsuarioRepresentation usuario = usuarioService.findByUsuario(username);

        // Validar que el usuario existe y la contraseña es correcta
        if (usuario == null) {
            return new TokenResponse(null, 0, null);
        }

        if (!usuario.getPassword().equals(password)) {
            return new TokenResponse(null, 0, null);
        }

    

        // Generar el token JWT
        String issuer = "vuelo-auth";
        long ttl = 3600;

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttl);

        String jwt = Jwt.issuer(issuer)
                .subject(username)
                .groups(Set.of(rol)) // rol ingresado en la página o del usuario
                .issuedAt(now)
                .expiresAt(exp)
                .sign();
        return new TokenResponse(jwt, exp.getEpochSecond(), rol) ;

    }

    public static class TokenResponse {
        public String accessToken;
        public long expiresAt;
        public String rol;

        public TokenResponse() {
        }

        public TokenResponse(String accessToken, long expiresAt, String rol) {
            this.accessToken = accessToken;
            this.expiresAt = expiresAt;
            this.rol = rol;
        }
    }

}