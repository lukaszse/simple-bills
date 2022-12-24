package pl.com.seremak.simplebills.planning.service;

import com.mongodb.lang.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.com.seremak.simplebills.commons.dto.http.CategoryDto;
import pl.com.seremak.simplebills.commons.exceptions.ConflictException;
import pl.com.seremak.simplebills.commons.model.Category;
import pl.com.seremak.simplebills.commons.utils.CollectionUtils;
import pl.com.seremak.simplebills.commons.utils.VersionedEntityUtils;
import pl.com.seremak.simplebills.planning.messageQueue.MessagePublisher;
import pl.com.seremak.simplebills.planning.repository.CategoryRepository;
import pl.com.seremak.simplebills.planning.repository.CategorySearchRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static pl.com.seremak.simplebills.commons.converter.CategoryConverter.*;
import static pl.com.seremak.simplebills.commons.model.Category.TransactionType.EXPENSE;
import static pl.com.seremak.simplebills.commons.model.Category.TransactionType.INCOME;
import static pl.com.seremak.simplebills.commons.utils.CollectionUtils.getSoleElementOrThrowException;
import static pl.com.seremak.simplebills.commons.utils.CollectionUtils.mergeLists;
import static pl.com.seremak.simplebills.planning.utils.BillPlanConstants.MASTER_USER;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    public static final String CATEGORY_ALREADY_EXISTS_ERROR_MSG = "Category with name %s for user with name %s already exists";
    public static final String UNDEFINED = "undefined";
    private final CategoryRepository categoryRepository;
    private final CategoryUsageLimitService categoryUsageLimitService;
    private final CategorySearchRepository categorySearchRepository;
    private final MessagePublisher messagePublisher;


    public Mono<Category> createCustomCategory(final String username, final CategoryDto categoryDto) {
        final Category category = toCategory(username, categoryDto, Category.Type.CUSTOM);
        return createCategory(category);
    }

    public Flux<Category> createAllCategories(final Set<Category> categories) {
        final Set<Category> versionedCategories = categories.stream()
                .map(VersionedEntityUtils::setMetadata)
                .collect(Collectors.toSet());
        return categoryRepository.saveAll(versionedCategories);
    }

    public Mono<List<Category>> findAllCategories(final String username) {
        return categoryRepository.findCategoriesByUsername(username)
                .collectList();
    }

    public Mono<Category> findCategory(final String username, final String categoryName) {
        return categoryRepository.findCategoriesByUsernameAndName(username, categoryName)
                .collectList()
                .map(CollectionUtils::getSoleElementOrThrowException);
    }

    public Mono<Category> updateCategory(final String username, final String categoryName, final CategoryDto categoryDto) {
        final Category categoryToUpdate = toCategory(username, categoryName, categoryDto);
        return categorySearchRepository.updateCategory(categoryToUpdate)
                .doOnSuccess(this::updateCategoryUsageLimit);
    }

    public Mono<Category> deleteCategory(final String username,
                                         final String categoryName,
                                         @Nullable final String incomingReplacementCategory) {
        return categoryRepository.deleteCategoryByUsernameAndName(username, categoryName)
                .publishOn(Schedulers.boundedElastic())
                .doOnSuccess(category -> reassignTransactionOfDeletedCategory(category, incomingReplacementCategory)
                        .then(categoryUsageLimitService.deleteCategoryUsageLimit(username, categoryName))
                        .subscribe());
    }

    public Mono<List<Category>> createStandardCategoriesForUserIfNotExists(final String username) {
        log.info("Looking for missing standard categories...");
        return findStandardCategoriesForUser(username)
                .collectList()
                .flatMap(userStandardCategories -> findStandardCategoriesForUser(MASTER_USER)
                        .collectList()
                        .map(masterUserStandardCategories ->
                                findAllMissingCategories(username, userStandardCategories, masterUserStandardCategories)))
                .flatMapMany(this::createAllCategories)
                .collectList()
                .doOnSuccess(CategoryService::logMissingCategoryAddingSummary);
    }

    public Flux<Category> findStandardCategoriesForUser(final String username) {
        return categoryRepository.findCategoriesByUsernameAndType(username, Category.Type.STANDARD);
    }

    public static Set<Category> findAllMissingCategories(final String username,
                                                         final List<Category> userStandardCategories,
                                                         final List<String> incomeStandardCategoryNames,
                                                         final List<String> expenseStandardCategoryNames) {
        final List<Category> incomeStandardCategories = toCategories(username, incomeStandardCategoryNames, INCOME);
        final List<Category> expenseStandardCategories = toCategories(username, expenseStandardCategoryNames, EXPENSE);
        final List<Category> allTypeStandardCategories = mergeLists(incomeStandardCategories, expenseStandardCategories);
        return findAllMissingCategories(username, userStandardCategories, allTypeStandardCategories);
    }

    private Mono<Category> createCategory(final Category category) {
        return categoryRepository.findCategoriesByUsernameAndName(category.getUsername(), category.getName())
                .collectList()
                .mapNotNull(existingCategories -> existingCategories.isEmpty() ? category : null)
                .map(VersionedEntityUtils::setMetadata)
                .map(categoryRepository::save)
                .flatMap(mono -> mono)
                .doOnSuccess(this::createNewCategoryUsageLimit)
                .switchIfEmpty(Mono.error(new ConflictException(CATEGORY_ALREADY_EXISTS_ERROR_MSG.formatted(category.getUsername(), category.getName()))));
    }


    private Mono<String> reassignTransactionOfDeletedCategory(final Category deletedCategory,
                                                              @Nullable final String replacementCategoryName) {
        return findOrCreateReplacementCategory(deletedCategory, replacementCategoryName)
                .doOnNext(existingReplacementCategoryName ->
                        messagePublisher.sendCategoryEventMessage(toCategoryDeletionEventDto(deletedCategory, existingReplacementCategoryName)));
    }

    private void createNewCategoryUsageLimit(final Category category) {
        if (nonNull(category)) {
            categoryUsageLimitService.createNewCategoryUsageLimit(category.getUsername(), category.getName())
                    .subscribe();
        }
    }

    private void updateCategoryUsageLimit(final Category category) {
        if (nonNull(category)) {
            categoryUsageLimitService.updateCategoryUsageLimit(category.getUsername(), category.getName(), category.getLimit())
                    .subscribe();
        }
    }

    private Mono<String> findOrCreateReplacementCategory(final Category deletedCategory,
                                                         final String replacementCategoryName) {
        final String finalReplacementCategoryName = defaultIfNull(replacementCategoryName, UNDEFINED);
        final Category.Type categoryType = isNull(replacementCategoryName) ? Category.Type.UNDEFINED : deletedCategory.getType();
        return categoryRepository.findCategoriesByUsernameAndName(deletedCategory.getUsername(), finalReplacementCategoryName)
                .collectList()
                .mapNotNull(existingCategoryList -> getSoleElementOrThrowException(existingCategoryList, false))
                .map(Category::getName)
                .doOnNext(existingCategoryName -> log.info("Category with name={} found in database.", existingCategoryName))
                .switchIfEmpty(Mono.just(toCategoryDto(deletedCategory.getUsername(), finalReplacementCategoryName, deletedCategory.getTransactionType()))
                        .map(categoryDto -> toCategory(deletedCategory.getUsername(), categoryDto, categoryType))
                        .flatMap(this::createCategory)
                        .map(Category::getName)
                        .doOnNext(createdCategoryName -> log.info("New category with name={} created.", createdCategoryName)));
    }

    private static Set<Category> findAllMissingCategories(final String username,
                                                          final List<Category> userStandardCategories,
                                                          final List<Category> masterUserStandardCategories) {
        final Set<String> existingStandardCategoryNamesForUser = extractExistingStandardCategoryNamesForUser(userStandardCategories);
        return masterUserStandardCategories.stream()
                .filter(masterUserStandardCategory -> !existingStandardCategoryNamesForUser.contains(masterUserStandardCategory.getName()))
                .map(masterUserCategoryToCopy -> toCategory(username, masterUserCategoryToCopy.getName(), masterUserCategoryToCopy.getTransactionType()))
                .collect(Collectors.toSet());
    }


    private static Set<String> extractExistingStandardCategoryNamesForUser(final List<Category> userStandardCategories) {
        return userStandardCategories.stream()
                .map(Category::getName)
                .collect(Collectors.toSet());
    }

    public static void logMissingCategoryAddingSummary(final List<Category> addedCategories) {
        final String addedCategoryNames = addedCategories.stream()
                .map(Category::getName)
                .collect(Collectors.joining(", "));
        if (addedCategories.isEmpty()) {
            log.info("No missing categories found");

        } else {
            log.info("{} missing categories added: {}", addedCategories.size(), addedCategoryNames);
        }
    }
}
