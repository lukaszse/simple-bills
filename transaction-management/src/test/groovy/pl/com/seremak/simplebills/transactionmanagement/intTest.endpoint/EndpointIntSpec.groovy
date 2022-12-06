package pl.com.seremak.simplebills.transactionmanagement.intTest.endpoint

import com.fasterxml.jackson.core.type.TypeReference
import groovy.util.logging.Slf4j
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate
import pl.com.seremak.simplebills.commons.model.Transaction
import pl.com.seremak.simplebills.transactionmanagement.intTest.endpoint.utils.JsonImporter
import pl.com.seremak.simplebills.transactionmanagement.messageQueue.MessageListener
import pl.com.seremak.simplebills.transactionmanagement.messageQueue.MessagePublisher
import pl.com.seremak.simplebills.transactionmanagement.repository.TransactionCrudRepository
import pl.com.seremak.simplebills.transactionmanagement.service.SequentialIdService
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import static pl.com.seremak.simplebills.transactionmanagement.intTest.endpoint.utils.EndpointSpecData.getTEST_USER
import static pl.com.seremak.simplebills.transactionmanagement.intTest.endpoint.utils.EndpointSpecData.getTEST_USER_2

@Slf4j
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
class EndpointIntSpec extends Specification {

    @LocalServerPort
    protected int port

    @Autowired
    ServerHttpSecurity http

    @Autowired
    TransactionCrudRepository transactionCrudRepository

    @Autowired
    SequentialIdService sequentialIdService

    @SpringBean
    SecurityWebFilterChain securityWebFilterChain = Mock()

    @SpringBean
    MessageListener messageListener = Mock()

    @SpringBean
    MessagePublisher messagePublisher = Mock()

    @Shared
    def client = new RestTemplate()

    def conditions = new PollingConditions(timeout: 5, initialDelay: 1)

    def setup() {
        // populate database for tests
        transactionCrudRepository.deleteAll().block()
        cleanSequentialIdRepository()
        def transactions =
                JsonImporter.getDataFromFile("json/transactions.json", new TypeReference<List<Transaction>>() {})
        transactionCrudRepository.saveAll(transactions)
    }

    def cleanup() {
        transactionCrudRepository.deleteAll().block()
        cleanSequentialIdRepository()
    }

    def cleanSequentialIdRepository() {
        try {
            sequentialIdService.deleteUser(TEST_USER).block()
        } catch (Exception ex) {
            log.info("Error occurred while cleaning up: {}, {}", ex.getMessage(), ex.getCause())
        }
        try {
            sequentialIdService.deleteUser(TEST_USER_2).block()
        } catch (Exception ex) {
            log.info("Error occurred while cleaning up: {}, {}", ex.getMessage(), ex.getCause())
        }
    }
}
