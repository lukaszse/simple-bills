package pl.com.seremak.simplebills.commons.utils

import pl.com.seremak.simplebills.commons.model.Transaction
import spock.lang.Specification

import java.time.Instant

class ReflectionsUtilsSpec extends Specification {

    def "should get value field of given custom object"() {

        given: "prepare custom object"
        def transaction = new Transaction
                (
                        user: "user",
                        transactionNumber: 1,
                        type: Transaction.Type.EXPENSE,
                        description: "Example description",
                        category: "Example category",
                        amount: 999.99
                )
        and: "get field"
        def field = Transaction.getDeclaredField(fieldName)

        when: "get field value for given field"
        def fieldValue = ReflectionsUtils.getFieldValue(field, transaction)

        then:
        fieldValue == expectedValue

        where:
        fieldName           | expectedValue
        "user"              | "user"
        "transactionNumber" | 1
        "type"              | Transaction.Type.EXPENSE
        "description"       | "Example description"
        "category"          | "Example category"
        "amount"            | 999.99
    }
}
