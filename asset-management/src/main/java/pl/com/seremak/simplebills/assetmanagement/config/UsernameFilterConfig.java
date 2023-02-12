package pl.com.seremak.simplebills.assetmanagement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import pl.com.seremak.simplebills.commons.securityUtils.UsernameHeaderFilter;

@Configuration
@RequiredArgsConstructor
public class UsernameFilterConfig {

    private final ReactiveJwtDecoder reactiveJwtDecoder;


    @Bean
    UsernameHeaderFilter usernameHeaderFilter() {
        return new UsernameHeaderFilter(reactiveJwtDecoder);
    }
}
