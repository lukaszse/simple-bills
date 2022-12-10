package pl.com.seremak.simplebills.transactionmanagement

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@TestConfiguration
@ComponentScan(
        basePackages = 'pl.com.seremak.simplebills.transactionmanagement'
)
class TransactionTestConfig {

        @Bean
        SecurityWebFilterChain securityWebFilterChain(
                ServerHttpSecurity http) {
                return http.authorizeExchange()
                        .anyExchange().permitAll()
                        .and().httpBasic()
                        .and().csrf().disable()
                        .build();
        }

        @Bean
        MapReactiveUserDetailsService userDetailsService() {
                def user = User
                        .withUsername("testUser")
                        .password(passwordEncoder().encode("password"))
                        .roles("USER")
                        .build()
                return new MapReactiveUserDetailsService(user)
        }

        private static PasswordEncoder passwordEncoder() {
                return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }
}
