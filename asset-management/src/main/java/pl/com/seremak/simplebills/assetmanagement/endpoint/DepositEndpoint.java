package pl.com.seremak.simplebills.assetmanagement.endpoint;


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
import pl.com.seremak.simplebills.assetmanagement.service.DepositService;
import pl.com.seremak.simplebills.commons.dto.http.DepositDto;
import pl.com.seremak.simplebills.commons.model.Balance;
import pl.com.seremak.simplebills.commons.model.Deposit;
import pl.com.seremak.simplebills.commons.utils.TokenExtractionHelper;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.com.seremak.simplebills.commons.utils.EndpointUtils.decodeUriParam;
import static pl.com.seremak.simplebills.commons.utils.EndpointUtils.prepareCreatedResponse;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/deposits")
@RequiredArgsConstructor
public class DepositEndpoint {

    public static final String DEPOSITS_URI_PATTERN = "/deposits/%s";

    private final DepositService depositService;

    @Operation(summary = "Create deposit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Deposit created",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Balance.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)})
    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Deposit>> createDeposit(@AuthenticationPrincipal final Principal principal,
                                                       @RequestHeader("Authorization") final String authHeader,
                                                       @Valid @RequestBody final DepositDto depositDto) {
        final String username = TokenExtractionHelper.extractUsername(principal);
        log.info("Deposit creation request received for username={} and depositName={}", username, depositDto.getName());
        return depositService.createDeposit(username, authHeader, depositDto)
                .doOnSuccess(createdDeposit -> log.info("Deposit with name={} and username={} created.", createdDeposit.getName(), createdDeposit.getUsername()))
                .map(deposit -> prepareCreatedResponse(DEPOSITS_URI_PATTERN, deposit.getName(), deposit));
    }

    @Operation(summary = "Get all deposits")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deposits found",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Balance.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)})
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<Deposit>>> findDeposits(@AuthenticationPrincipal final Principal principal) {
        final String username = TokenExtractionHelper.extractUsername(principal);
        return depositService.findAllDeposits(username)
                .doOnSuccess(deposits -> log.info("{} Deposit for username={} found.", deposits.size(), username))
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Get deposit by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deposit found",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Balance.class))}),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)})
    @GetMapping(value = "{depositName}", produces = APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Deposit>> findDeposit(@AuthenticationPrincipal final Principal principal,
                                                     @PathVariable final String depositName) {
        final String username = TokenExtractionHelper.extractUsername(principal);
        return depositService.findDepositByName(username, decodeUriParam(depositName))
                .doOnSuccess(deposit -> log.info("Deposit with name={} and username={} found.", deposit.getName(), deposit.getUsername()))
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Update deposit by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deposit found",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Balance.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)})
    @PatchMapping(value = "{depositName}", produces = APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Deposit>> updateDeposit(@AuthenticationPrincipal final Principal principal,
                                                       @RequestHeader("Authorization") final String authHeader,
                                                       @Valid @RequestBody final DepositDto depositDto,
                                                       @PathVariable final String depositName) {
        final String username = TokenExtractionHelper.extractUsername(principal);
        log.info("Deposit update request received for username={} and depositName={}", username, depositDto.getName());
        return depositService.updateDeposit(username, authHeader, decodeUriParam(depositName), depositDto)
                .doOnSuccess(updatedDeposit ->
                        log.info("Deposit with name={} and username={} updated.", updatedDeposit.getName(), updatedDeposit.getUsername()))
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Delete deposit by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deposit found",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Balance.class))}),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)})
    @DeleteMapping(value = "{depositName}")
    public Mono<ResponseEntity<Void>> deleteDeposit(@AuthenticationPrincipal final Principal principal,
                                                    @RequestHeader("Authorization") final String authHeader,
                                                    @PathVariable final String depositName) {
        final String username = TokenExtractionHelper.extractUsername(principal);
        log.info("Deposit deletion request received for username={} and depositName={}", username, depositName);
        return depositService.deleteDeposit(username, authHeader, decodeUriParam(depositName))
                .doOnSuccess(updatedDeposit ->
                        log.info("Deposit with name={} and username={} updated.", updatedDeposit.getName(), updatedDeposit.getUsername()))
                .map(__ -> ResponseEntity.noContent().build());
    }
}
