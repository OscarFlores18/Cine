package com.app.Gateway.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableWebSecurity
public class GatewaySecurityConfig {

    // Mismo secret que usa Auth para firmar. Viene del application.yml
    // global del config server, compartido por todos los microservicios.
    @Value("${jwt.secret}")
    private String jwtSecretBase64;

    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecretBase64);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    // Lee el claim "roles" (una lista tipo ["ROLE_ADMIN"]) que armamos en
    // Auth/JwtService, y lo convierte a las authorities que entiende
    // Spring Security, para poder usar hasRole()/hasAuthority() a futuro.
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix(""); // el claim ya trae el prefijo ROLE_

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtDecoder jwtDecoder,
                                            JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // el preflight de CORS (OPTIONS) tiene que pasar SIEMPRE,
                // sin JWT, o el navegador nunca ve la respuesta con los
                // headers de Access-Control-*
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // login/registro: acá es donde se CONSIGUE el token,
                // así que no puede requerir uno. Auth valida por su cuenta
                // el registro (es SU propio recurso, no el de otro micro).
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()

                // ACÁ vive TODA la política de permisos del sistema, en un solo
                // lugar. Los microservicios de dominio no tienen SecurityConfig
                // propio: confían en que si una request les llegó, el Gateway
                // ya la autorizó (y el GatewayInterceptor les asegura que no
                // les peguen directo salteándose este filtro).
                //
                // Catalogo (productos y salas): leer = cualquier usuario logueado,
                // modificar = solo ADMIN.
                .requestMatchers(HttpMethod.GET, "/productos/**", "/salas/**").authenticated()
                .requestMatchers("/productos/**", "/salas/**").hasRole("ADMIN")

                // Ventas: cualquier usuario logueado puede comprar y ver las suyas;
                // el listado completo es solo para ADMIN y EMPLEADO.
                .requestMatchers(HttpMethod.POST, "/ventas").authenticated()
                .requestMatchers(HttpMethod.GET, "/ventas/mias").authenticated()
                .requestMatchers("/ventas/**").hasAnyRole("ADMIN", "EMPLEADO")

                // Gmail: servicio de notificaciones, solo ADMIN
                .requestMatchers("/gmail/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                .decoder(jwtDecoder)
                .jwtAuthenticationConverter(jwtAuthenticationConverter)
            ))
            // Despues de validar el JWT, agrega X-User-Username y X-User-Roles
            // a la request que se rutea a los microservicios (paso del practico).
            .addFilterAfter(new UserHeadersFilter(), BearerTokenAuthenticationFilter.class);

        return http.build();
    }
}
