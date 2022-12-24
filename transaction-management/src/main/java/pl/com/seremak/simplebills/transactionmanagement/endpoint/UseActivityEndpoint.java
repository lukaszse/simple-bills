package pl.com.seremak.simplebills.transactionmanagement.endpoint;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.com.seremak.simplebills.commons.dto.http.TokenUser;
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

    @GetMapping(value = "/info", produces = APPLICATION_JSON_VALUE)
    static Mono<ResponseEntity<TokenUser>> getUserInfo(@AuthenticationPrincipal final Principal principal) {
        final TokenUser tokenUser = TokenExtractionHelper.extractTokenUser(principal);
        log.info(USER_INFO_FETCHED_MESSAGE, tokenUser.getPreferredUsername());
        return Mono.just(tokenUser)
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "/logging", produces = APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<UserActivity>> addUserLogging(@AuthenticationPrincipal final Principal principal) {
        final TokenUser tokenUser = TokenExtractionHelper.extractTokenUser(principal);
        log.info(USER_INFO_FETCHED_MESSAGE, tokenUser.getPreferredUsername());
        return userActivityService.addUserLogging(tokenUser.getPreferredUsername())
                .doOnSuccess(userActivity -> log.info(USER_ACTIVITY_ADDED_MESSAGE, userActivity.getActivity(), userActivity.getUsername()))
                .map(ResponseEntity::ok);
    }
}
