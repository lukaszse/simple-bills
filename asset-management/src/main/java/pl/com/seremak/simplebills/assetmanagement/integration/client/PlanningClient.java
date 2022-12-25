package pl.com.seremak.simplebills.assetmanagement.integration.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pl.com.seremak.simplebills.commons.dto.http.CategoryDto;
import pl.com.seremak.simplebills.commons.model.Balance;
import pl.com.seremak.simplebills.commons.model.Category;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static pl.com.seremak.simplebills.assetmanagement.utils.HttpClientUtils.URI_SEPARATOR;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanningClient {

    private final WebClient balanceClient;
    private final WebClient categoryClient;


    public Mono<Balance> getBalance(final String authHeader) {
        return balanceClient.get()
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Balance.class)
                .doOnNext(retrievedBalance -> log.info("Balance for username={} retrieved.", retrievedBalance.getUsername()));
    }

    public Mono<Category> getCategory(final String username,
                                      final String autHeader,
                                      final String categoryName) {
        return categoryClient.get()
                .uri(URI_SEPARATOR.formatted(categoryName))
                .header(HttpHeaders.AUTHORIZATION, autHeader)
                .retrieve()
                .bodyToMono(Category.class)
                .doOnNext(retrievedCategory -> log.info("Category with username={} and name={} retrieved.",
                        retrievedCategory.getUsername(), retrievedCategory.getName()))
                .onErrorResume(error -> isNotFoundStatus(error, username, categoryName), __ -> Mono.empty());
    }

    public Mono<Category> createCategory(final String authHeader, final CategoryDto categoryDto) {
        return categoryClient.post()
                .header(HttpHeaders.AUTHORIZATION, authHeader)
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
