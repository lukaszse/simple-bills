package pl.com.seremak.simplebills.transactionmanagement.intTest.endpoint

import org.apache.commons.lang3.StringUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import pl.com.seremak.simplebills.commons.model.Transaction

import static pl.com.seremak.simplebills.transactionmanagement.testUtils.EndpointSpecData.*

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

    def 'should create bill for user and then fetch created bill'() {

        given: 'prepare request for bill creation'
        def creationRequest =
                RequestEntity.post(TRANSACTION_URL_PATTERN.formatted(port, StringUtils.EMPTY))
                        .accept(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, BASIC_TOKEN)
                        .body(prepareBillForEndpointTest(100, FOOD, Instant.now()))


        when: 'make request to crate bill'
        def transactionNumber = client.exchange(creationRequest, String.class)


        then: 'should return correct creation response and retrieve created bill'
        conditions.eventually {

            def fetchResponse = client.exchange(
                    RequestEntity.get(SERVICE_URL_BILL_CRUD_PATTERN.formatted(port, "/%s".formatted(transactionNumber.getBody())))
                            .accept(MediaType.APPLICATION_JSON)
                            .header(AUTHORIZATION_HEADER_NAME, BASIC_TOKEN)
                            .build(),
                    Transaction.class)

            assert fetchResponse != null
            assert fetchResponse.getStatusCode() == HttpStatus.OK
            assert fetchResponse.getBody().getCategory() == FOOD
        }
    }
}
