package pl.com.seremak.simplebills.transactionmanagement.endpoint;

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
import pl.com.seremak.simplebills.commons.dto.http.TokenUser;
import pl.com.seremak.simplebills.commons.dto.http.UserActivityDto;
import pl.com.seremak.simplebills.commons.model.Transaction;
import pl.com.seremak.simplebills.commons.model.UserActivity;
import pl.com.seremak.simplebills.commons.utils.TokenExtractionHelper;
import pl.com.seremak.simplebills.transactionmanagement.service.UserActivityService;
import reactor.core.publisher.Mono;

import java.security.Principal;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UseActivityEndpoint {

    public static final String USER_INFO_FETCHED_MESSAGE = "User info for user={} successfully extracted from token.";
    public static final String USER_ACTIVITY_ADDED_MESSAGE = "User activity {} added for user {}";

    private final UserActivityService userActivityService;

    @Operation(summary = "Get info about user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User info found",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TokenUser.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)})
    @GetMapping(value = "/info", produces = APPLICATION_JSON_VALUE)
    static Mono<ResponseEntity<TokenUser>> getUserInfo(@AuthenticationPrincipal final Principal principal) {
        final TokenUser tokenUser = TokenExtractionHelper.extractTokenUser(principal);
        log.info(USER_INFO_FETCHED_MESSAGE, tokenUser.getPreferredUsername());
        return Mono.just(tokenUser)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Add user activity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User activity added",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserActivity.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)})
    @PostMapping(value = "/activity", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<UserActivity>> addUserActivity(@AuthenticationPrincipal final Principal principal, @RequestBody final UserActivityDto userActivityDto) {
        final TokenUser tokenUser = TokenExtractionHelper.extractTokenUser(principal);
        log.info(USER_INFO_FETCHED_MESSAGE, tokenUser.getPreferredUsername());
        return userActivityService.addUserActivity(tokenUser.getPreferredUsername(), userActivityDto.getActivity())
                .doOnSuccess(userActivity -> log.info(USER_ACTIVITY_ADDED_MESSAGE, userActivity.getActivity(), userActivity.getUsername()))
                .map(ResponseEntity::ok);
    }
}
