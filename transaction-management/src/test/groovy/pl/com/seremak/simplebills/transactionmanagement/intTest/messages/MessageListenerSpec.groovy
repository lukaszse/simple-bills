package pl.com.seremak.simplebills.transactionmanagement.intTest.messages

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import pl.com.seremak.simplebills.transactionmanagement.intTest.testUtilsAndConfig.IntSpecConfig
import pl.com.seremak.simplebills.transactionmanagement.intTest.testUtilsAndConfig.TestRabbitPublisher
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@Slf4j
@Testcontainers
@SpringBootTest(classes = IntSpecConfig.class)
@ActiveProfiles("test")
class MessageListenerSpec extends Specification {


    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.3")

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management")

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        mongoDBContainer.start()
        rabbitMQContainer.start()
        log.info("MongoDb container connection string: {}", mongoDBContainer.getConnectionString())
        registry.add("spring.data.mongodb.uri", () ->   mongoDBContainer.getConnectionString())
        registry.add("spring.rabbitmq.host", () -> rabbitMQContainer.getHost())
        registry.add("spring.rabbitmq.port", () -> rabbitMQContainer.getAmqpPort())
    }

    @Autowired
    TestRabbitPublisher testRabbitPublisher

    def conditions = new PollingConditions(timeout: 5, initialDelay: 1)


    def cleanupSpec() {
        mongoDBContainer.stop()
        rabbitMQContainer.stop()
    }
}
