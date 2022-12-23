package pl.com.seremak.simplebills.assetmanagement.integration.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pl.com.seremak.simplebills.commons.dto.http.TransactionDto;
import pl.com.seremak.simplebills.commons.model.Transaction;
import reactor.core.publisher.Mono;

import static pl.com.seremak.simplebills.assetmanagement.utils.HttpClientUtils.URI_SEPARATOR;
import static pl.com.seremak.simplebills.assetmanagement.utils.HttpClientUtils.prepareBearerToken;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionsClient {

    private final WebClient transactionClient;


    public Mono<Transaction> createTransaction(final Jwt token,
                                               final TransactionDto transactionDto) {
        return transactionClient.post()
                .header(HttpHeaders.AUTHORIZATION, prepareBearerToken(token))
                .body(Mono.just(transactionDto), TransactionDto.class)
                .retrieve()
                .bodyToMono(Transaction.class)
                .doOnNext(createdTransaction -> log.info("Transaction created: {}", createdTransaction));
    }

    public Mono<Transaction> updateTransaction(final Jwt token,
                                               final TransactionDto transactionDto) {
        return transactionClient.patch()
                .uri(URI_SEPARATOR.formatted(transactionDto.getTransactionNumber()))
                .header(HttpHeaders.AUTHORIZATION, prepareBearerToken(token))
                .body(Mono.just(transactionDto), TransactionDto.class)
                .retrieve()
                .bodyToMono(Transaction.class)
                .doOnNext(updatedTransaction -> log.info("Transaction updated: {}", updatedTransaction));
    }

    public Mono<Void> deleteTransaction(final Jwt token,
                                        final Integer transactionNumber) {
        return transactionClient.delete()
                .uri(URI_SEPARATOR.formatted(transactionNumber))
                .header(HttpHeaders.AUTHORIZATION, prepareBearerToken(token))
                .retrieve()
                .toBodilessEntity()
                .doOnNext(__ -> log.info("Transaction with transactionNumber={} deleted.", transactionNumber))
                .then();
    }
}
