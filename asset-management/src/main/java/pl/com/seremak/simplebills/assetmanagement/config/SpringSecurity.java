package pl.com.seremak.simplebills.assetmanagement.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.web.server.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SpringSecurity {

    private static final String[] openApiPaths = {"/api-docs", "/api-docs/*", "/*/swagger-ui/*", "/swagger-ui"};

    @Value("${custom-properties.allowed-origin}")
    private String allowedOrigin;

    private final Environment environment;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            final ServerHttpSecurity http) {
        final ServerHttpSecurity serverHttpSecurity = http
                .authorizeExchange()
                .pathMatchers(openApiPaths).permitAll()
                .anyExchange().authenticated()
                .and()
                .oauth2ResourceServer().bearerTokenConverter(bearerTokenConverter())
                .jwt()
                .and().and()
                .cors()
                .and();
        return extractActiveProfileName(environment).equals("local") ?
                serverHttpSecurity
                        .csrf()
                        .disable()
                        .build() :
                serverHttpSecurity
                        .csrf()
                        .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
                        .and()
                        .build();
    }

    @Bean
    public CorsConfigurationSource corsConfiguration() {
        final CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.applyPermitDefaultValues();
        corsConfig.addAllowedMethod(HttpMethod.POST);
        corsConfig.addAllowedMethod(HttpMethod.PUT);
        corsConfig.addAllowedMethod(HttpMethod.PATCH);
        corsConfig.addAllowedMethod(HttpMethod.DELETE);
        corsConfig.setAllowedOrigins(List.of(allowedOrigin));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }

    private static ServerAuthenticationConverter bearerTokenConverter() {
        final ServerBearerTokenAuthenticationConverter authenticationConverter = new ServerBearerTokenAuthenticationConverter();
        authenticationConverter.setAllowUriQueryParameter(true);
        return authenticationConverter;
    }

    private static String extractActiveProfileName(final Environment environment) {
        final String activeProfileName = Arrays.stream(environment.getActiveProfiles())
                .findFirst()
                .orElse(StringUtils.EMPTY);
        log.info("Active profile: {}", activeProfileName);
        return activeProfileName;
    }
}
