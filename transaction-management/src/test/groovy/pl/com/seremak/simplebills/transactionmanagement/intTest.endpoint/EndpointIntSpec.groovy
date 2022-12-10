package pl.com.seremak.simplebills.transactionmanagement.intTest.endpoint

import com.fasterxml.jackson.core.type.TypeReference
import groovy.util.logging.Slf4j
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate
import pl.com.seremak.simplebills.commons.dto.http.TransactionDto
import pl.com.seremak.simplebills.commons.model.Transaction
import pl.com.seremak.simplebills.transactionmanagement.TransactionTestConfig
import pl.com.seremak.simplebills.transactionmanagement.intTest.endpoint.utils.JsonImporter
import pl.com.seremak.simplebills.transactionmanagement.messageQueue.MessageListener
import pl.com.seremak.simplebills.transactionmanagement.messageQueue.MessagePublisher
import pl.com.seremak.simplebills.transactionmanagement.repository.TransactionCrudRepository
import pl.com.seremak.simplebills.transactionmanagement.service.SequentialIdService
import pl.com.seremak.simplebills.transactionmanagement.service.TransactionService
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.stream.Collectors

import static pl.com.seremak.simplebills.transactionmanagement.intTest.endpoint.utils.EndpointSpecData.TEST_USER

@Slf4j
@SpringBootTest(
        classes = TransactionTestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EndpointIntSpec extends Specification {

    @LocalServerPort
    protected int port

    @Autowired
    ServerHttpSecurity http

    @Autowired
    TransactionService transactionService

    @Autowired
    SequentialIdService sequentialIdService

    @Autowired
    TransactionCrudRepository transactionCrudRepository

    @SpringBean
    MessageListener messageListener = Mock()

    @SpringBean
    MessagePublisher messagePublisher = Mock()

    @Shared
    def client = new RestTemplate()

    def conditions = new PollingConditions(timeout: 5, initialDelay: 1)

    def setup() {
        log.info("Populating database before tests")
        // populate database for tests
        transactionCrudRepository.deleteAll().block()
        cleanSequentialIdRepository()
        def transactions =
                JsonImporter.getDataFromFile("json/transactions.json", new TypeReference<List<TransactionDto>>() {})
        log.info("Number of transaction to populate {}", transactions.size())
        def addedTransactions = transactions.stream()
                .map(transactionToSave -> transactionService.createTransaction(TEST_USER, transactionToSave))
                .map(transactionMono -> transactionMono.block())
                .collect(Collectors.toList())
        log.info("Transaction added to database: {}", addedTransactions.toString())
    }

    def cleanup() {
//        transactionCrudRepository.deleteAll().block()
//        cleanSequentialIdRepository()
    }

    def cleanSequentialIdRepository() {
        try {
            sequentialIdService.deleteUser(TEST_USER).block()
        } catch (Exception ex) {
            log.info("Error occurred while cleaning up: {}, {}", ex.getMessage(), ex)
        }
    }
}
