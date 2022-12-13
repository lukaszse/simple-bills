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
import pl.com.seremak.simplebills.commons.dto.queue.ActionType
import pl.com.seremak.simplebills.commons.dto.queue.TransactionEventDto
import pl.com.seremak.simplebills.commons.model.Transaction
import pl.com.seremak.simplebills.transactionmanagement.config.RabbitMQConfig
import pl.com.seremak.simplebills.transactionmanagement.intTest.testUtilsAndConfig.IntSpecConfig
import pl.com.seremak.simplebills.transactionmanagement.messageQueue.MessageListener
import pl.com.seremak.simplebills.transactionmanagement.messageQueue.MessagePublisher
import pl.com.seremak.simplebills.transactionmanagement.repository.TransactionCrudRepository
import pl.com.seremak.simplebills.transactionmanagement.repository.TransactionSearchRepository
import pl.com.seremak.simplebills.transactionmanagement.repository.UserActivityRepository
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

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
                category: newCategory,
                amount: newAmount,
                type: "EXPENSE"
        )

        def existingTransaction = new Transaction(
                user: "testUser",
                transactionNumber: 1,
                type: Transaction.Type.EXPENSE,
                category: "food",
                amount: -1 * existingAmount
        )

        def updatedTransaction = new Transaction(
                user: "testUser",
                transactionNumber: 1,
                type: Transaction.Type.EXPENSE,
                category: newCategory,
                amount: -1 * newAmount
        )

        def transactionEventMessage = new TransactionEventDto(
                username: "testUser",
                transactionNumber: 1,
                categoryName: newCategory,
                amount: existingAmount - newAmount,
                type: ActionType.UPDATE
        )

        and: "mock database response"
        1 * transactionCrudRepository.findByUserAndTransactionNumber("testUser", 1) >> Mono.just(existingTransaction)
        1 * messagePublisher.sendTransactionEventMessage(transactionEventMessage)
        1 * transactionSearchRepository.updateTransaction(updatedTransaction) >> Mono.just(updatedTransaction)

        when:
        def updatedTransactionMono = transactionService.updateTransaction("testUser", 1, transactionDto)

        then:
        StepVerifier
                .create(updatedTransactionMono)
                .expectNextMatches(transaction -> transaction == updatedTransaction)
                .expectComplete()
                .verify()

        where:
        newCategory | newAmount | existingAmount
        "food"      | 300       | 100
        "car"       | 100       | 100
        "travel"    | 100       | 144.23
        "sport"     | 124.99    | 299.99
    }
}
