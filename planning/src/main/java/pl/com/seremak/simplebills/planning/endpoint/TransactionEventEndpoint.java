package pl.com.seremak.simplebills.planning.endpoint;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.com.seremak.simplebills.commons.dto.queue.TransactionEventDto;
import pl.com.seremak.simplebills.commons.model.Balance;
import pl.com.seremak.simplebills.commons.model.Category;
import pl.com.seremak.simplebills.commons.utils.TokenExtractionHelper;
import pl.com.seremak.simplebills.planning.service.TransactionPostingService;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.security.Principal;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/transactionsEvents")
@RequiredArgsConstructor
public class TransactionEventEndpoint {

    private final TransactionPostingService transactionPostingService;

    @Operation(summary = "Post transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction posted",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Category.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)})
    @PostMapping(produces = TEXT_PLAIN_VALUE, consumes = APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Balance>> postTransaction(@AuthenticationPrincipal final Principal principal,
                                                         @Valid @RequestBody final TransactionEventDto transactionEventDto) {
        final String username = TokenExtractionHelper.extractUsername(principal);
        TokenExtractionHelper.validateUsername(principal, transactionEventDto.getUsername());
        log.info("Transaction request for username={} and categoryName={} received.", username, transactionEventDto.getCategoryName());
        return transactionPostingService.postTransaction(transactionEventDto)
                .map(ResponseEntity::ok);
    }
}
