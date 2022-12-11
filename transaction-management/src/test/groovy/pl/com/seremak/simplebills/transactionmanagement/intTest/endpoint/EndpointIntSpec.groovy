package pl.com.seremak.simplebills.transactionmanagement.intTest.endpoint

import com.fasterxml.jackson.core.type.TypeReference
import groovy.util.logging.Slf4j
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.web.client.RestTemplate
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import pl.com.seremak.simplebills.commons.dto.http.TransactionDto
import pl.com.seremak.simplebills.transactionmanagement.TransactionSpecConfig
import pl.com.seremak.simplebills.transactionmanagement.intTest.endpoint.testUtils.EndpointSpecData
import pl.com.seremak.simplebills.transactionmanagement.messageQueue.MessageListener
import pl.com.seremak.simplebills.transactionmanagement.messageQueue.MessagePublisher
import pl.com.seremak.simplebills.transactionmanagement.repository.TransactionCrudRepository
import pl.com.seremak.simplebills.transactionmanagement.service.SequentialIdService
import pl.com.seremak.simplebills.transactionmanagement.service.TransactionService
import pl.com.seremak.simplebills.transactionmanagement.testUtils.JsonImporter
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.stream.Collectors

@Slf4j
@Testcontainers
@SpringBootTest(
        classes = TransactionSpecConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EndpointIntSpec extends Specification {

    @LocalServerPort
    int port

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.4.3"))

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        mongoDBContainer.start()
        log.info("MongoDb container connection string: {}", mongoDBContainer.getConnectionString())
        registry.add("spring.data.mongodb.uri", () ->   mongoDBContainer.getConnectionString())
    }

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
        log.info("Populating database before test")
        def transactions =
                JsonImporter.getDataFromFile("json/transactions.json", new TypeReference<List<TransactionDto>>() {})
        log.info("Number of transaction to populate {}", transactions.size())
        def addedTransactions = transactions.stream()
                .map(transactionToSave -> transactionService.createTransaction(EndpointSpecData.TEST_USER, transactionToSave))
                .map(transactionMono -> transactionMono.block())
                .collect(Collectors.toList())
        log.info("Transaction added to database: {}", addedTransactions.toString())
    }
}
