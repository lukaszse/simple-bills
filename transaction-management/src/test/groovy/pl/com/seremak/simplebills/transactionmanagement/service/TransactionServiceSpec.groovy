package pl.com.seremak.simplebills.transactionmanagement.service

import groovy.util.logging.Slf4j
import org.spockframework.spring.SpringBean
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers
import pl.com.seremak.simplebills.commons.dto.http.TransactionDto
import pl.com.seremak.simplebills.commons.model.Transaction
import pl.com.seremak.simplebills.transactionmanagement.config.RabbitMQConfig
import pl.com.seremak.simplebills.transactionmanagement.intTest.testUtilsAndConfig.IntSpecConfig
import pl.com.seremak.simplebills.transactionmanagement.messageQueue.MessageListener
import pl.com.seremak.simplebills.transactionmanagement.messageQueue.MessagePublisher
import pl.com.seremak.simplebills.transactionmanagement.repository.TransactionCrudRepository
import pl.com.seremak.simplebills.transactionmanagement.repository.TransactionSearchRepository
import pl.com.seremak.simplebills.transactionmanagement.repository.UserActivityRepository
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.time.Instant

@Slf4j
@Testcontainers
@SpringBootTest(classes = IntSpecConfig.class)
@ActiveProfiles("test")
class TransactionServiceSpec extends Specification {


    @SpringBean
    RabbitMQConfig rabbitMQConfig = Mock()

    @SpringBean
    RabbitMessagingTemplate rabbitMessagingTemplate = Mock()

    @SpringBean
    ReactiveMongoTemplate reactiveMongoTemplate = Mock()

    @SpringBean
    TransactionCrudRepository transactionCrudRepository = Mock()

    @SpringBean
    TransactionSearchRepository transactionSearchRepository = Mock()

    @SpringBean
    UserActivityRepository userActivityRepository = Mock()

    @SpringBean
    MessageListener messageListener = Mock()

    @SpringBean
    MessagePublisher messagePublisher = Mock()

    @Autowired
    TransactionService transactionService


    def "should update transaction"() {

        given: "prepare test data"
        def transactionDto = new TransactionDto(
                category: category,
                amount: amount,
                type: "EXPENSE"
        )

        def existingTransaction = new Transaction(
                user: "testUser",
                transactionNumber: 1,
                type: Transaction.Type.EXPENSE,
                category: "food",
                amount: -100
        )

        def updatedTransaction = new Transaction(
                user: "testUser",
                transactionNumber: 1,
                type: Transaction.Type.EXPENSE,
                category: category,
                amount: -1*amount
        )

        when:
        transactionService.updateTransaction("testUser", 1, transactionDto).block()

        then:
        noExceptionThrown()
        1 * transactionCrudRepository.findByUserAndTransactionNumber("testUser", 1) >> Mono.just(existingTransaction)
        1 * transactionSearchRepository.updateTransaction(updatedTransaction) >> Mono.just(updatedTransaction)
        1 * messagePublisher.sendTransactionEventMessage(_)

        where:
        date         | category | amount
        "2022-10-10" | "food"   | 200
    }
}
