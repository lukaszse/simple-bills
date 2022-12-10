package pl.com.seremak.simplebills.commons.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import pl.com.seremak.simplebills.commons.dto.http.TokenUser;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;

import static pl.com.seremak.simplebills.commons.utils.ObjectMapperUtils.objectMapper;


public class TokenExtractionHelper {

    public static final String EXTRACTING_TOKEN_ERROR_MSG = "Error while extracting token. Reason: %s";
    public static final String TOKEN_NOT_MATCH = "Token not match";
    public static final String INVALID_TOKEN = "Invalid token provided";


    public static String extractUsername(final Principal principal) {
        return extractTokenUser(principal).getPreferredUsername();
    }

    public static TokenUser extractTokenUser(final Principal principal) {
        if(principal instanceof  JwtAuthenticationToken) {
            return extractUsernameFromJwtAuthenticationToken((JwtAuthenticationToken) principal);
        }
        if(principal instanceof UsernamePasswordAuthenticationToken) {
            final String username = principal.getName();
            return TokenUser.builder()
                    .preferredUsername(username)
                    .build();
        }
        throw new AuthenticationServiceException(EXTRACTING_TOKEN_ERROR_MSG.formatted(INVALID_TOKEN));
    }

    public static String extractUsername(final JwtAuthenticationToken jwtAuthenticationToken) {
        return extractUsernameFromJwtAuthenticationToken(jwtAuthenticationToken).getPreferredUsername();
    }

    private static TokenUser extractUsernameFromJwtAuthenticationToken(final JwtAuthenticationToken jwtAuthenticationToken) {
        try {
            final Map<String, Object> claims = jwtAuthenticationToken.getToken().getClaims();
            return objectMapper().convertValue(claims, new TypeReference<>() {
            });
        } catch (final Exception e) {
            throw new AuthenticationServiceException(EXTRACTING_TOKEN_ERROR_MSG.formatted(e.getMessage()));
        }
    }

    public static void validateUsername(final Principal principal, final String username) {
        final String tokenUsername = extractUsername(principal);
        if (Objects.nonNull(tokenUsername) && tokenUsername.equals(username)) {
            throw new AuthenticationServiceException(EXTRACTING_TOKEN_ERROR_MSG.formatted(TOKEN_NOT_MATCH));
        }
    }
}
