package pl.com.seremak.simplebills.transactionmanagement.intTest.endpoint


import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.web.client.RestTemplate
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import pl.com.seremak.simplebills.transactionmanagement.intTest.testUtilsAndConfig.IntSpecConfig
import pl.com.seremak.simplebills.transactionmanagement.intTest.testUtilsAndConfig.TestRabbitListener
import pl.com.seremak.simplebills.transactionmanagement.repository.TransactionCrudRepository
import pl.com.seremak.simplebills.transactionmanagement.service.SequentialIdRepository
import pl.com.seremak.simplebills.transactionmanagement.service.TransactionService
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@Slf4j
@Testcontainers
@SpringBootTest(
        classes = IntSpecConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EndpointIntSpec extends Specification {

    @LocalServerPort
    int port

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.3")

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management")

    @Autowired
    TransactionService transactionService

    @Autowired
    SequentialIdRepository sequentialIdService

    @Autowired
    TransactionCrudRepository transactionCrudRepository

    @Autowired
    TestRabbitListener testRabbitListener

    @Shared
    def client = new RestTemplate()

    def conditions = new PollingConditions(timeout: 5, initialDelay: 1)


    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        mongoDBContainer.start()
        rabbitMQContainer.start()
        log.info("MongoDb container connection string: {}", mongoDBContainer.getConnectionString())
        registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getConnectionString())
        registry.add("spring.rabbitmq.host", () -> rabbitMQContainer.getHost())
        registry.add("spring.rabbitmq.port", () -> rabbitMQContainer.getAmqpPort())
    }

    def setup() {
        testRabbitListener.clearReceivedMessages()
    }

    def cleanupSpec() {
        mongoDBContainer.stop()
        rabbitMQContainer.stop()
    }
}
