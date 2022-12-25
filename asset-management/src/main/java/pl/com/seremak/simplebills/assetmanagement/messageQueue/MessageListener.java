package pl.com.seremak.simplebills.assetmanagement.messageQueue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import pl.com.seremak.simplebills.assetmanagement.service.DepositService;
import pl.com.seremak.simplebills.commons.dto.queue.TransactionEventDto;

import static pl.com.seremak.simplebills.commons.constants.MessageQueue.TRANSACTION_EVENT_ASSETS_MANAGEMENT_QUEUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageListener {

    private final DepositService depositService;

    @RabbitListener(queues = TRANSACTION_EVENT_ASSETS_MANAGEMENT_QUEUE)
    public void receiveAssetTransactionEvent(final Message<TransactionEventDto> assetTransactionEventMessage) {
        final TransactionEventDto assetTransactionEvent = assetTransactionEventMessage.getPayload();
        log.info("Asset transaction event message received: {}", assetTransactionEvent);
        depositService.handleTransactionEvent(assetTransactionEvent);
    }
}
