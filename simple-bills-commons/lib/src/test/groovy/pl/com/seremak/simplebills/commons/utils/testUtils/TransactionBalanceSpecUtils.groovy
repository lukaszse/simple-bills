package pl.com.seremak.simplebills.commons.utils.testUtils

import pl.com.seremak.simplebills.commons.dto.queue.ActionType
import pl.com.seremak.simplebills.commons.dto.queue.TransactionEventDto

import java.time.Instant

class TransactionBalanceSpecUtils {

    static def prepareTransactionEventDto(amount, actionType) {
        new TransactionEventDto(
                amount: amount,
                type: actionType,
                username: "user",
                categoryName: "FOOD",
                transactionNumber: 1,
                date: Instant.now()
        )
    }
}
