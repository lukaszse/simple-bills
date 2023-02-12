package pl.com.seremak.simplebills.commons.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;

public class ValidationUtils {

    public static void validateUsername(final String usernameFromToken, final String username) {
        if(!StringUtils.equals(usernameFromToken, username)) {
            throw new AuthenticationServiceException("Error while extracting token. Reason: %s".formatted("Token not match"));
        }
    }
}
