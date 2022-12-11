package pl.com.seremak.simplebills.transactionmanagement.intTest.messages

import com.fasterxml.jackson.core.type.TypeReference
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import pl.com.seremak.simplebills.commons.dto.http.TransactionDto
import pl.com.seremak.simplebills.commons.dto.http.TransactionQueryParams
import pl.com.seremak.simplebills.transactionmanagement.service.TransactionService
import pl.com.seremak.simplebills.transactionmanagement.testUtils.JsonImporter

import java.util.stream.Collectors

import static pl.com.seremak.simplebills.transactionmanagement.intTest.testUtilsAndConfig.CategoryEventListenerSpecData.WAGES
import static pl.com.seremak.simplebills.transactionmanagement.intTest.testUtilsAndConfig.CategoryEventListenerSpecData.prepareCategoryEventDto
import static pl.com.seremak.simplebills.transactionmanagement.intTest.testUtilsAndConfig.TestConstants.TEST_USER

@Slf4j
class CategoryEventListenerSpec extends MessageListenerSpec {

    @Autowired
    TransactionService transactionService

    def "should receive and handle Category message"() {

        given: "populate database"
        populateDatabase()

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
        }

    }

    def populateDatabase() {
        log.info("Populating database before test")
        def transactions =
                JsonImporter.getDataFromFile("json/transactions.json", new TypeReference<List<TransactionDto>>() {})
        log.info("Number of transaction to populate {}", transactions.size())
        def addedTransactions = transactions.stream()
                .map(transactionToSave -> transactionService.createTransaction(TEST_USER, transactionToSave))
                .map(transactionMono -> transactionMono.block())
                .collect(Collectors.toList())
        log.info("Transaction added to database: {}", addedTransactions.toString())
    }
}
