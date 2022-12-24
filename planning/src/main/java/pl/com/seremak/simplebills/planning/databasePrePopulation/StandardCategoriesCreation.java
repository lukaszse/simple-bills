package pl.com.seremak.simplebills.planning.databasePrePopulation;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.com.seremak.simplebills.commons.model.Category;
import pl.com.seremak.simplebills.planning.service.CategoryService;
import pl.com.seremak.simplebills.planning.utils.BillPlanConstants;

import java.util.List;
import java.util.Set;

import static pl.com.seremak.simplebills.commons.converter.CategoryConverter.toCategories;
import static pl.com.seremak.simplebills.commons.model.Category.TransactionType.EXPENSE;
import static pl.com.seremak.simplebills.commons.model.Category.TransactionType.INCOME;
import static pl.com.seremak.simplebills.commons.utils.CollectionUtils.mergeLists;

@Slf4j
@Component
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "custom-properties")
public class StandardCategoriesCreation {

    private final CategoryService categoryService;

    @Setter
    private List<String> incomeCategories;

    @Setter
    private List<String> expenseCategories;

    @EventListener
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        createStandardCategoriesForMasterUserIfNotExists();
    }

    private void createStandardCategoriesForMasterUserIfNotExists() {
        log.info("Looking for missing standard categories...");
        categoryService.findStandardCategoriesForUser(BillPlanConstants.MASTER_USER)
                .collectList()
                .map(masterUserCategories -> CategoryService.findAllMissingCategories(BillPlanConstants.MASTER_USER, masterUserCategories, incomeCategories, expenseCategories))
                .flatMapMany(categoryService::createAllCategories)
                .collectList()
                .doOnSuccess(CategoryService::logMissingCategoryAddingSummary)
                .block();
    }
}
