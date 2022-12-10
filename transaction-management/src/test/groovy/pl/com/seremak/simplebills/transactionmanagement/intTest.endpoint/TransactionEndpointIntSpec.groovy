package pl.com.seremak.simplebills.transactionmanagement.intTest.endpoint

import org.apache.commons.lang3.StringUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import pl.com.seremak.simplebills.commons.dto.http.TransactionDto
import pl.com.seremak.simplebills.commons.model.Transaction

import java.time.LocalDate

import static pl.com.seremak.simplebills.transactionmanagement.intTest.endpoint.utils.EndpointSpecData.*

class TransactionEndpointIntSpec extends EndpointIntSpec {


    def 'should fetch transaction'() {

        given: 'prepare request to get transaction'
        def request =
                RequestEntity.get(TRANSACTION_URL_PATTERN.formatted(port, "/%d".formatted(transactionNumber)))
                        .accept(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, BASIC_TOKEN)
                        .build()

        when: 'should fetch bill'
        def response = client.exchange(request, Transaction.class)

        then:
        response != null
        response.getStatusCode() == HttpStatus.OK
        response.getBody().getCategory() == category
        response.getBody().getAmount() == BigDecimal.valueOf(amount)

        where:
        transactionNumber | category | amount
        1                 | SALARY   | 5000
        2                 | TRAVEL   | -1000
        3                 | CAR      | -300
        4                 | FOOD     | -200
    }

    def 'should create transaction for user and then fetch created transaction'() {

        given: 'prepare new transaction to create'
        def transactionToCreate = new TransactionDto
                (
                        category: category,
                        type: type,
                        description: description,
                        date: LocalDate.parse(date),
                        amount: amount
                )

        and: 'prepare request for transaction creation'
        def creationRequest =
                RequestEntity.post(TRANSACTION_URL_PATTERN.formatted(port, StringUtils.EMPTY))
                        .accept(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, BASIC_TOKEN)
                        .body(transactionToCreate)


        when: 'make request to crate transaction'
        def transactionCreationResponse = client.exchange(creationRequest, Transaction.class)


        then: 'should return correct creation response and retrieve created transaction'
        transactionCreationResponse.getStatusCode() == HttpStatus.CREATED
        conditions.eventually {

            def createdTransactionNumber = transactionCreationResponse.getBody().getTransactionNumber()
            def fetchResponse = client.exchange(
                    RequestEntity.get(TRANSACTION_URL_PATTERN.formatted(port, "/%s".formatted(createdTransactionNumber)))
                            .accept(MediaType.APPLICATION_JSON)
                            .header(AUTHORIZATION_HEADER, BASIC_TOKEN)
                            .build(),
                    Transaction.class)

            assert fetchResponse != null
            assert fetchResponse.getStatusCode() == HttpStatus.OK
            assert fetchResponse.getBody().getCategory() == category
            assert fetchResponse.getBody().getType().toString() == type
            assert fetchResponse.getBody().getDescription() == description
            assert fetchResponse.getBody().getDate().toString().startsWith(date)
            assert fetchResponse.getBody().getAmount() == amountResponse
        }

        where:
        category | type    | description           | amount | date         | amountResponse
        FOOD     | EXPENSE | "Grocery shopping"    | 232.34 | "2022-10-10" | 0 - amount
        TRAVEL   | EXPENSE | "Holiday in Spain"    | 5323   | "2022-11-10" | 0 - amount
        CAR      | EXPENSE | "Tire service"        | 130    | "2022-12-10" | 0 - amount
        FOOD     | EXPENSE | "Fruits & vegetables" | 75.99  | "2022-12-11" | 0 - amount
        SALARY   | INCOME  | "December salary"     | 5000   | "2022-10-10" | amount
    }
}
