package pl.com.seremak.simplebills.assetmanagement.integration.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pl.com.seremak.simplebills.commons.dto.http.TransactionDto;
import pl.com.seremak.simplebills.commons.model.Transaction;
import reactor.core.publisher.Mono;

import static pl.com.seremak.simplebills.assetmanagement.utils.HttpClientUtils.URI_SEPARATOR;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionManagementClient {

    private final WebClient transactionClient;


    public Mono<Transaction> createTransaction(final String authHeader,
                                               final TransactionDto transactionDto) {
        return transactionClient.post()
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .body(Mono.just(transactionDto), TransactionDto.class)
                .retrieve()
                .bodyToMono(Transaction.class)
                .doOnNext(createdTransaction -> log.info("Transaction created: {}", createdTransaction));
    }

    public Mono<Transaction> updateTransaction(final String authHeader,
                                               final TransactionDto transactionDto) {
        return transactionClient.patch()
                .uri(URI_SEPARATOR.formatted(transactionDto.getTransactionNumber()))
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .body(Mono.just(transactionDto), TransactionDto.class)
                .retrieve()
                .bodyToMono(Transaction.class)
                .doOnNext(updatedTransaction -> log.info("Transaction updated: {}", updatedTransaction));
    }

    public Mono<Void> deleteTransaction(final String authHeader,
                                        final Integer transactionNumber) {
        return transactionClient.delete()
                .uri(URI_SEPARATOR.formatted(transactionNumber))
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .toBodilessEntity()
                .doOnNext(__ -> log.info("Transaction with transactionNumber={} deleted.", transactionNumber))
                .then();
    }
}
