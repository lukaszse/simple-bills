package pl.com.seremak.simplebills.assetmanagement.integration.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pl.com.seremak.simplebills.commons.dto.http.CategoryDto;
import pl.com.seremak.simplebills.commons.model.Balance;
import pl.com.seremak.simplebills.commons.model.Category;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static pl.com.seremak.simplebills.assetmanagement.utils.HttpClientUtils.URI_SEPARATOR;
import static pl.com.seremak.simplebills.assetmanagement.utils.HttpClientUtils.prepareBearerToken;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillsPlanClient {

    private final WebClient balanceClient;
    private final WebClient categoryClient;


    public Mono<Balance> getBalance(final Jwt token) {
        return balanceClient.get()
                .header(HttpHeaders.AUTHORIZATION, prepareBearerToken(token))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Balance.class)
                .doOnNext(retrievedBalance -> log.info("Balance for username={} retrieved.", retrievedBalance.getUsername()));
    }

    public Mono<Category> getCategory(final Jwt token,
                                      final String username,
                                      final String categoryName) {
        return categoryClient.get()
                .uri(URI_SEPARATOR.formatted(categoryName))
                .header(HttpHeaders.AUTHORIZATION, prepareBearerToken(token))
                .retrieve()
                .bodyToMono(Category.class)
                .doOnNext(retrievedCategory -> log.info("Category with username={} and name={} retrieved.",
                        retrievedCategory.getUsername(), retrievedCategory.getName()))
                .onErrorResume(error -> isNotFoundStatus(error, username, categoryName), __ -> Mono.empty());
    }

    public Mono<Category> createCategory(final Jwt token, final CategoryDto categoryDto) {
        return categoryClient.post()
                .header(HttpHeaders.AUTHORIZATION, prepareBearerToken(token))
                .body(Mono.just(categoryDto), CategoryDto.class)
                .retrieve()
                .bodyToMono(Category.class)
                .doOnNext(createdCategory -> log.info("Category with username={} and name={} created.",
                        createdCategory.getUsername(), createdCategory.getName()));
    }

    private static boolean isNotFoundStatus(final Throwable exception,
                                            final String username,
                                            final String categoryName) {
        if (!(exception instanceof WebClientResponseException)) {
            return false;
        }
        final HttpStatus statusCode = ((WebClientResponseException) exception).getStatusCode();
        if (Objects.equals(statusCode, HttpStatus.NOT_FOUND)) {
            log.info("Category with name={} for username={} not found.", categoryName, username);
            return true;
        }
        return false;
    }
}
