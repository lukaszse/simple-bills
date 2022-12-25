package pl.com.seremak.simplebills.planning.endpoint;


import com.mongodb.lang.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.com.seremak.simplebills.commons.dto.http.CategoryDto;
import pl.com.seremak.simplebills.commons.model.Category;
import pl.com.seremak.simplebills.commons.utils.EndpointUtils;
import pl.com.seremak.simplebills.commons.utils.TokenExtractionHelper;
import pl.com.seremak.simplebills.planning.service.CategoryService;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryEndpoint {

    public static final String CATEGORY_URI_PATTERN = "/categories/%s";
    private final CategoryService categoryService;

    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Category>> createCategory(@AuthenticationPrincipal final Principal principal,
                                                         @Valid @RequestBody final CategoryDto categoryDto) {
        final String username = TokenExtractionHelper.extractUsername(principal);
        log.info("Category creation request received for username={} and categoryName={}", username, categoryDto.getName());
        return categoryService.createCustomCategory(username, categoryDto)
                .doOnSuccess(category -> log.info("Category with name={} and username={} successfully created for", category.getName(), category.getUsername()))
                .map(category -> EndpointUtils.prepareCreatedResponse(CATEGORY_URI_PATTERN, category.getName(), category));
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<Category>>> findAllCategories(@AuthenticationPrincipal final Principal principal) {
        final String username = TokenExtractionHelper.extractUsername(principal);
        log.info("Finding categories for user with name={}", username);
        return categoryService.findAllCategories(username)
                .doOnSuccess(categories -> log.info("{} categories for username={} found.", categories.size(), username))
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "{categoryName}", produces = APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Category>> findCategoryByName(@AuthenticationPrincipal final Principal principal,
                                                             @PathVariable final String categoryName) {
        final String username = TokenExtractionHelper.extractUsername(principal);
        log.info("Looking for category with name={} and username={}", categoryName, username);
        return categoryService.findCategory(username, categoryName)
                .doOnSuccess(category -> log.info("Category with name={} for username={} successfully found.", category.getName(), category.getUsername()))
                .map(ResponseEntity::ok);
    }

    @PatchMapping(value = "{categoryName}", produces = APPLICATION_JSON_VALUE)
    private Mono<ResponseEntity<Category>> updateCategory(@AuthenticationPrincipal final Principal principal,
                                                          @Valid @RequestBody final CategoryDto categoryDto,
                                                          @PathVariable final String categoryName) {
        final String username = TokenExtractionHelper.extractUsername(principal);
        log.info("Updating Category with username={} and categoryName={}", username, categoryName);
        return categoryService.updateCategory(username, categoryName, categoryDto)
                .doOnSuccess(updatedCategory -> log.info("Category with username={} and categoryName={} updated.", updatedCategory.getUsername(), updatedCategory.getName()))
                .map(ResponseEntity::ok);
    }

    @DeleteMapping(value = "{category}")
    private Mono<ResponseEntity<Void>> deleteCategoryName(@AuthenticationPrincipal final Principal principal,
                                                          @PathVariable final String category,
                                                          @RequestParam @Nullable final String replacementCategory) {
        final String username = TokenExtractionHelper.extractUsername(principal);
        log.info("Deleting category with name={} and username={}", category, username);
        return categoryService.deleteCategory(username, category, replacementCategory)
                .doOnSuccess(deletedCategory -> log.info("Category with name={} and username={} deleted.", deletedCategory.getName(), deletedCategory.getUsername()))
                .map(Category::getName)
                .map(__ -> ResponseEntity.noContent().build());
    }
}
