package pl.com.seremak.simplebills.commons.securityUtils;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UsernameHeaderFilter implements WebFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String BASIC_PREFIX = "Basic ";
    private static final String USERNAME = "username";
    private static final String PREFERRED_USERNAME = "preferred_username";
    private final ReactiveJwtDecoder reactiveJwtDecoder;

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {
        final String token = extractToken(exchange.getRequest());
        if (StringUtils.startsWith(token, BEARER_PREFIX)) {
            final String bearerToken = token.substring(BEARER_PREFIX.length()).trim();
            return reactiveJwtDecoder.decode(bearerToken)
                    .flatMap(jwt -> Mono.just(jwt.getClaims().getOrDefault(PREFERRED_USERNAME, StringUtils.EMPTY)))
                    .flatMap(username ->
                            chain.filter(exchange.mutate().request(addUsernameHeader(exchange, (String) username)).build()));
        }
        if (StringUtils.startsWith(token, BASIC_PREFIX)) {
            final String username = extractUsernameFromBasicToken(token);
            return chain.filter(exchange.mutate().request(addUsernameHeader(exchange, username)).build());
        }
        return chain.filter(exchange);
    }

    private static ServerHttpRequest addUsernameHeader(final ServerWebExchange exchange, final String username) {
        return exchange
                .getRequest()
                .mutate()
                .header(USERNAME, username)
                .build();
    }

    private static String extractToken(final ServerHttpRequest request) {
        return Optional.ofNullable(request.getHeaders())
                .map(presentRequest -> presentRequest.getFirst(HttpHeaders.AUTHORIZATION))
                .orElse(StringUtils.EMPTY);
    }

    private static String extractUsernameFromBasicToken(final String basicToken) {
        if (basicToken != null && basicToken.startsWith(BASIC_PREFIX)) {
            final String base64Credentials = basicToken.substring(BASIC_PREFIX.length()).trim();
            final byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            final String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            // credentials = username:password
            final String[] values = credentials.split(":", 2);
            return values[0];
        }
        return null;
    }
}