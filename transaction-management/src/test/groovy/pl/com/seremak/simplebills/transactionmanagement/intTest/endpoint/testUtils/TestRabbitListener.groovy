package pl.com.seremak.simplebills.transactionmanagement.intTest.endpoint.testUtils

import groovy.util.logging.Slf4j
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.messaging.Message
import pl.com.seremak.simplebills.commons.dto.queue.TransactionEventDto

import static pl.com.seremak.simplebills.commons.constants.MessageQueue.TRANSACTION_EVENT_ASSETS_MANAGEMENT_QUEUE
import static pl.com.seremak.simplebills.commons.constants.MessageQueue.TRANSACTION_EVENT_PLANING_QUEUE

@Slf4j
class TestRabbitListener {

    def transactionEventPlanningReceivedMessages = new ArrayList<TransactionEventDto>()
    def transactionEventAssetManagementMessages = new ArrayList<TransactionEventDto>()


    @RabbitListener(queues = TRANSACTION_EVENT_PLANING_QUEUE)
    void receiveTransactionEventMessageToPlanning(final Message<TransactionEventDto> transactionMessage) {
        final TransactionEventDto transaction = transactionMessage.getPayload()
        transactionEventPlanningReceivedMessages.add(transaction)
        log.info("Message received: {}", transaction)
    }

    @RabbitListener(queues = TRANSACTION_EVENT_ASSETS_MANAGEMENT_QUEUE)
    void receiveTransactionEventMessageToAssetManagement(final Message<TransactionEventDto> transactionMessage) {
        final TransactionEventDto transaction = transactionMessage.getPayload()
        transactionEventAssetManagementMessages.add(transaction)
        log.info("Message received: {}", transaction)
    }

    void clearReceivedMessages() {
        transactionEventPlanningReceivedMessages.removeAll()
        transactionEventAssetManagementMessages.removeAll()
    }
}
