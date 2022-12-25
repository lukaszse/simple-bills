package pl.com.seremak.simplebills.planning.endpoint;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.com.seremak.simplebills.commons.model.Balance;
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


    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Balance>> findBalance(@AuthenticationPrincipal final Principal principal) {
        final String username = TokenExtractionHelper.extractUsername(principal);
        return balanceService.findBalance(username)
                .doOnSuccess(balance -> log.info("Balance for username={} found.", balance.getUsername()))
                .map(ResponseEntity::ok);
    }
}
