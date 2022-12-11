package pl.com.seremak.simplebills.transactionmanagement.intTest.testUtilsAndConfig

import groovy.util.logging.Slf4j
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.messaging.Message
import pl.com.seremak.simplebills.commons.dto.queue.TransactionEventDto

import static pl.com.seremak.simplebills.commons.constants.MessageQueue.TRANSACTION_EVENT_ASSETS_MANAGEMENT_QUEUE
import static pl.com.seremak.simplebills.commons.constants.MessageQueue.TRANSACTION_EVENT_PLANING_QUEUE

@Slf4j
class TestRabbitListener {

    Map<String, List<TransactionEventDto>> transactionEventReceivedMessages = createEmptyMessageBox()

    @RabbitListener(queues = TRANSACTION_EVENT_PLANING_QUEUE)
    void receiveTransactionEventMessageToPlanning(final Message<TransactionEventDto> transactionMessage) {
        final TransactionEventDto transactionEvent = transactionMessage.getPayload()
        def  transactionList = transactionEventReceivedMessages.get(TRANSACTION_EVENT_PLANING_QUEUE)
        transactionList.add(transactionEvent)
        log.info("Message received: {}", transactionEvent)
    }

    @RabbitListener(queues = TRANSACTION_EVENT_ASSETS_MANAGEMENT_QUEUE)
    void receiveTransactionEventMessageToAssetManagement(final Message<TransactionEventDto> transactionMessage) {
        final TransactionEventDto transactionEvent = transactionMessage.getPayload()
        def  transactionList = transactionEventReceivedMessages.get(TRANSACTION_EVENT_ASSETS_MANAGEMENT_QUEUE)
        transactionList.add(transactionEvent)
        log.info("Message received: {}", transactionEvent)
    }

    void clearReceivedMessages() {
        this.transactionEventReceivedMessages = createEmptyMessageBox()
    }

    static Map<String, List<TransactionEventDto>> createEmptyMessageBox() {
        Map.of(
                TRANSACTION_EVENT_PLANING_QUEUE, new ArrayList<TransactionEventDto>(),
                TRANSACTION_EVENT_ASSETS_MANAGEMENT_QUEUE, new ArrayList<TransactionEventDto>()
        )
    }
}
