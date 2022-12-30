package pl.com.seremak.simplebills.transactionmanagement.intTest.messages


import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import pl.com.seremak.simplebills.commons.dto.http.TransactionQueryParams
import pl.com.seremak.simplebills.transactionmanagement.service.TransactionService

import static pl.com.seremak.simplebills.transactionmanagement.intTest.testUtilsAndConfig.CategoryEventListenerSpecData.WAGES
import static pl.com.seremak.simplebills.transactionmanagement.intTest.testUtilsAndConfig.CategoryEventListenerSpecData.prepareCategoryEventDto
import static pl.com.seremak.simplebills.transactionmanagement.intTest.testUtilsAndConfig.IntTestCommonUtils.TEST_USER
import static pl.com.seremak.simplebills.transactionmanagement.intTest.testUtilsAndConfig.IntTestCommonUtils.populateDatabase

@Slf4j
class CategoryEventListenerSpec extends MessageListenerSpec {

    @Autowired
    TransactionService transactionService

    def "should receive and handle Category message"() {

        given: "populate database"
        populateDatabase(transactionService)

        and: "prepare test data"
        def categoryEvent = prepareCategoryEventDto()
        def transactionQueryParams = new TransactionQueryParams(category: WAGES)

        when: "publish test message"
        testRabbitPublisher.sendCategoryEventMessage(categoryEvent)

        then:
        conditions.eventually {
            def transactions = transactionService.findTransactionsByCategory(TEST_USER, transactionQueryParams)
                    .map { it.t1 }
                    .block()
            assert transactions.size() == 1

            def transaction = transactions.get(0)
            assert transaction.getAmount() == 5000
            assert transaction.getCategory() == WAGES
            assert transaction.getDescription() == "Salary"
        }
    }
}
