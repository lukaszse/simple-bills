package pl.com.seremak.simplebills.transactionmanagement.intTest.testUtilsAndConfig

import groovy.util.logging.Slf4j
import org.springframework.amqp.rabbit.core.RabbitTemplate
import pl.com.seremak.simplebills.commons.dto.queue.CategoryEventDto

import static pl.com.seremak.simplebills.commons.constants.MessageQueue.CATEGORY_EVENT_TRANSACTION_MANAGEMENT_QUEUE
import static pl.com.seremak.simplebills.commons.constants.MessageQueue.SIMPLE_BILLS_EXCHANGE

@Slf4j
class TestRabbitPublisher {

    private final RabbitTemplate rabbitTemplate

    TestRabbitPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate
    }

    def sendCategoryEventMessage(final CategoryEventDto categoryEventDto) {
        rabbitTemplate.convertAndSend(SIMPLE_BILLS_EXCHANGE, CATEGORY_EVENT_TRANSACTION_MANAGEMENT_QUEUE, categoryEventDto)
        log.info("Message sent: queue={}, message={}", CATEGORY_EVENT_TRANSACTION_MANAGEMENT_QUEUE, categoryEventDto)
    }
}
