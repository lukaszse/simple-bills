package pl.com.seremak.simplebills.planning.endpoint;


import com.mongodb.lang.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.com.seremak.simplebills.commons.model.CategoryUsageLimit;
import pl.com.seremak.simplebills.commons.utils.TokenExtractionHelper;
import pl.com.seremak.simplebills.planning.service.CategoryUsageLimitService;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/category-usage-limit")
@RequiredArgsConstructor
public class CategoryUsageLimitEndpoint {

    private final CategoryUsageLimitService categoryUsageLimitService;

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<CategoryUsageLimit>>> findAllCategoryUsageLimits(
            @AuthenticationPrincipal final Principal principal,
            @Nullable @RequestParam final String yearMonth,
            @RequestParam(value = "total", required = false, defaultValue = "false") final boolean total) {

        final String username = TokenExtractionHelper.extractUsername(principal);
        return categoryUsageLimitService.findAllCategoryUsageLimits(username, yearMonth, total)
                .doOnSuccess(categoryUsageLimits -> log.info("A list of {} usage of limits for all categories for username={} found.", categoryUsageLimits.size(), username))
                .map(ResponseEntity::ok);
    }
}
