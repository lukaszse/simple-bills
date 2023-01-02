package pl.com.seremak.simplebills.transactionmanagement.endpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.com.seremak.simplebills.commons.dto.http.TransactionDto;
import pl.com.seremak.simplebills.commons.dto.http.TransactionQueryParams;
import pl.com.seremak.simplebills.commons.model.Transaction;
import pl.com.seremak.simplebills.commons.utils.TokenExtractionHelper;
import pl.com.seremak.simplebills.commons.validator.ValidateDatePeriod;
import pl.com.seremak.simplebills.transactionmanagement.service.TransactionService;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.com.seremak.simplebills.commons.utils.EndpointUtils.prepareCreatedResponse;


@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Validated
public class TransactionEndpoint {

    private static final String X_TOTAL_COUNT_HEADER = "x-total-count";
    private static final String TRANSACTION_URI_PATTERN = "/transactions/%s";
    private final TransactionService transactionService;


    @Operation(summary = "Create transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction created",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Transaction.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)})
    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Transaction>> createTransaction(@AuthenticationPrincipal final Principal principal,
                                                               @Valid @RequestBody final TransactionDto transactionDto) {
        final String username = TokenExtractionHelper.extractUsername(principal);
        log.info("Received transaction creation request from user={}", username);
        return transactionService.createTransaction(username, transactionDto)
                .doOnSuccess(createTransaction -> log.info("Transaction for user={} with number={} successfully created",
                        createTransaction.getUser(), createTransaction.getTransactionNumber()))
                .map(transaction -> prepareCreatedResponse(TRANSACTION_URI_PATTERN, String.valueOf(transaction.getTransactionNumber()), transaction));
    }

    @Operation(summary = "Get transaction by transactionNumber")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction found",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Transaction.class))}),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)})
    @GetMapping(value = "/{transactionNumber}", produces = APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Transaction>> findTransactionByTransactionNumber(@AuthenticationPrincipal final Principal principal,
                                                                                @PathVariable final Integer transactionNumber) {
        final String username = TokenExtractionHelper.extractUsername(principal);
        log.info("Find transaction with number={} for user={}", transactionNumber, username);
        return transactionService.findTransactionByTransactionNumber(username, transactionNumber)
                .doOnSuccess(transaction -> log.info("Transaction with number={} for user={} successfully found.", transaction.getTransactionNumber(), transaction.getUser()))
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Get transactions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions found",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Transaction.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)})
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<Transaction>>> findAllTransactionsByCategory(@AuthenticationPrincipal final Principal principal,
                                                                                 @ValidateDatePeriod final TransactionQueryParams params) {
        final String username = TokenExtractionHelper.extractUsername(principal);
        log.info("Find transactions with category={} for user={}", Optional.ofNullable(params.getCategory()).orElse("All categories"), username);
        return transactionService.findTransactionsByCategory(username, params)
                .doOnSuccess(__ -> log.info("List of transactions successfully fetched."))
                .map(tuple -> ResponseEntity.ok().headers(prepareXTotalCountHeader(tuple.getT2())).body(tuple.getT1()));
    }

    @Operation(summary = "Delete transaction by transactionNumber")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transaction deleted"),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)})
    @DeleteMapping(value = "/{transactionNumber}")
    public Mono<ResponseEntity<Void>> deleteTransaction(@AuthenticationPrincipal final Principal principal,
                                                        @PathVariable final Integer transactionNumber) {
        final String username = TokenExtractionHelper.extractUsername(principal);
        log.info("Transaction delete request for user={} transactionNumber={}", username, transactionNumber);
        return transactionService.deleteTransactionByTransactionNumber(username, transactionNumber)
                .doOnSuccess(__ -> log.info("Transaction for user={} with transactionNumber={} successfully deleted.", username, transactionNumber))
                .map(__ -> ResponseEntity.noContent().build());
    }

    @Operation(summary = "Update transaction by transactionNumber")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction updated",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Transaction.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)})
    @PatchMapping(value = "/{transactionNumber}", produces = APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Transaction>> updateTransaction(@AuthenticationPrincipal final Principal principal,
                                                               @Valid @RequestBody final TransactionDto transactionDto,
                                                               @NotNull @PathVariable final Integer transactionNumber) {
        final String username = TokenExtractionHelper.extractUsername(principal);
        log.info("Transaction update request for user={} and transactionNumber={}", username, transactionNumber);
        return transactionService.updateTransaction(username, transactionNumber, transactionDto)
                .doOnSuccess(updatedTransaction -> log.info("Transaction with user={} and transactionNumber={} successfully update.",
                        updatedTransaction.getUser(), updatedTransaction.getTransactionNumber()))
                .map(ResponseEntity::ok);
    }

    private static HttpHeaders prepareXTotalCountHeader(final Long count) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(X_TOTAL_COUNT_HEADER, String.valueOf(count));
        headers.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, X_TOTAL_COUNT_HEADER);
        return headers;
    }
}
