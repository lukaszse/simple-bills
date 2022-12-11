package pl.com.seremak.simplebills.commons.utils

import pl.com.seremak.simplebills.commons.dto.queue.ActionType
import pl.com.seremak.simplebills.commons.utils.testUtils.TransactionBalanceSpecUtils
import spock.lang.Specification

class TransactionBalanceUtilsSpec extends Specification {

    def "should uprate balance correctly"() {

        given: "prepare TransactionEventDto"
        def transactionEventDto =
                TransactionBalanceSpecUtils.prepareTransactionEventDto(transactionAmount as BigDecimal, actionType)

        when: "invoke updateBalance method"
        def balanceAfterUpdate =
                TransactionBalanceUtils.updateBalance(balanceBeforeUpdate as BigDecimal, transactionEventDto)

        then: "should calculate balance correctly"
        balanceAfterUpdate == expectedBalanceAfterUpdate

        where:
        transactionAmount | actionType          | balanceBeforeUpdate || expectedBalanceAfterUpdate
        -100              | ActionType.CREATION | 5000                || 4900
        100.22            | ActionType.CREATION | 232.22              || 332.44
        -99.99            | ActionType.UPDATE   | 5000                || 4900.01
        99.99             | ActionType.UPDATE   | 5000                || 5099.99
        -344.03           | ActionType.DELETION | 1000.33             || 1344.36
        150               | ActionType.DELETION | 200                 || 50
    }

    def "should uprate category usage correctly"() {

        given: "prepare TransactionEventDto"
        def transactionEventDto =
                TransactionBalanceSpecUtils.prepareTransactionEventDto(transactionAmount as BigDecimal, actionType)

        when: "invoke updateCategoryUsage method"
        def balanceAfterUpdate =
                TransactionBalanceUtils.updateCategoryUsage(balanceBeforeUpdate as BigDecimal, transactionEventDto)

        then: "should calculate category usage correctly"
        balanceAfterUpdate == expectedBalanceAfterUpdate

        where:
        transactionAmount | actionType          | balanceBeforeUpdate || expectedBalanceAfterUpdate
        -100              | ActionType.CREATION | 5000                || 5100
        100.22            | ActionType.CREATION | 232.22              || 132
        -99.99            | ActionType.UPDATE   | 5000                || 5099.99
        99.99             | ActionType.UPDATE   | 5000                || 4900.01
        -344.03           | ActionType.DELETION | 1000.33             || 656.30
        150               | ActionType.DELETION | 200                 || 350
    }
}
