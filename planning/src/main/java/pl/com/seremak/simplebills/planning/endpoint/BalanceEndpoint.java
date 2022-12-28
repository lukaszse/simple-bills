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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.com.seremak.simplebills.commons.model.Balance;
import pl.com.seremak.simplebills.commons.model.Transaction;
import pl.com.seremak.simplebills.commons.utils.TokenExtractionHelper;
import pl.com.seremak.simplebills.planning.service.BalanceService;
import reactor.core.publisher.Mono;

import java.security.Principal;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/balance")
@RequiredArgsConstructor
public class BalanceEndpoint {

    private final BalanceService balanceService;


    @Operation(summary = "Get balance")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balance found",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Balance.class))}),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)})
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Balance>> findBalance(@AuthenticationPrincipal final Principal principal) {
        final String username = TokenExtractionHelper.extractUsername(principal);
        return balanceService.findBalance(username)
                .doOnSuccess(balance -> log.info("Balance for username={} found.", balance.getUsername()))
                .map(ResponseEntity::ok);
    }
}
