package pl.com.seremak.simplebills.transactionmanagement.intTest.endpoint

import com.fasterxml.jackson.core.type.TypeReference
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.web.client.HttpClientErrorException
import pl.com.seremak.simplebills.commons.dto.http.TransactionDto
import pl.com.seremak.simplebills.commons.model.Transaction
import pl.com.seremak.simplebills.transactionmanagement.intTest.endpoint.testUtils.EndpointSpecData
import pl.com.seremak.simplebills.transactionmanagement.intTest.endpoint.testUtils.TestRabbitListener
import pl.com.seremak.simplebills.transactionmanagement.testUtils.JsonImporter
import spock.lang.Stepwise

import java.time.LocalDate
import java.util.stream.Collectors

@Slf4j
@Stepwise
class TransactionEndpointIntSpec extends EndpointIntSpec {


    def 'should fetch transaction'() {

        given: 'populate database with test data'
        populateDatabase()

        and: 'prepare request to get transaction'
        def request =
                RequestEntity.get(EndpointSpecData.TRANSACTION_URL_PATTERN.formatted(port, "/%d".formatted(transactionNumber)))
                        .accept(MediaType.APPLICATION_JSON)
                        .header(EndpointSpecData.AUTHORIZATION_HEADER, EndpointSpecData.BASIC_TOKEN)
                        .build()

        when: 'should fetch bill'
        def response = client.exchange(request, Transaction.class)

        then:
        response != null
        response.getStatusCode() == HttpStatus.OK
        response.getBody().getCategory() == category
        response.getBody().getAmount() == BigDecimal.valueOf(amount)

        where:
        transactionNumber | category                | amount
        1                 | EndpointSpecData.SALARY | 5000
        2                 | EndpointSpecData.TRAVEL | -1000
        3                 | EndpointSpecData.CAR    | -300
        4                 | EndpointSpecData.FOOD   | -200
    }

    def 'should return HTTP status 404 when requested when given transaction not found'() {

        given: 'prepare request to get transaction'
        def request =
                RequestEntity.get(EndpointSpecData.TRANSACTION_URL_PATTERN.formatted(port, "/%d".formatted(99)))
                        .accept(MediaType.APPLICATION_JSON)
                        .header(EndpointSpecData.AUTHORIZATION_HEADER, EndpointSpecData.BASIC_TOKEN)
                        .build()

        when: 'should fetch bill'
        client.exchange(request, Transaction.class)

        then:
        def exception = thrown(HttpClientErrorException)
        exception.getStatusCode() == HttpStatus.NOT_FOUND
        exception.getMessage().startsWith("404 Not Found")
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
                RequestEntity.post(EndpointSpecData.TRANSACTION_URL_PATTERN.formatted(port, StringUtils.EMPTY))
                        .accept(MediaType.APPLICATION_JSON)
                        .header(EndpointSpecData.AUTHORIZATION_HEADER, EndpointSpecData.BASIC_TOKEN)
                        .body(transactionToCreate)

        when: 'make request to crate transaction'
        def transactionCreationResponse = client.exchange(creationRequest, Transaction.class)


        then: 'should return correct creation response with status 201'
        transactionCreationResponse.getStatusCode() == HttpStatus.CREATED

        and: 'should fetch created messages'
        conditions.eventually {

            def createdTransactionNumber = transactionCreationResponse.getBody().getTransactionNumber()
            def fetchResponse = client.exchange(
                    RequestEntity.get(EndpointSpecData.TRANSACTION_URL_PATTERN.formatted(port, "/%s".formatted(createdTransactionNumber)))
                            .accept(MediaType.APPLICATION_JSON)
                            .header(EndpointSpecData.AUTHORIZATION_HEADER, EndpointSpecData.BASIC_TOKEN)
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

        and: 'should sent correct RabbitMQ messages to external services'
        conditions.eventually {
            assert testRabbitListener.transactionEventPlanningReceivedMessages.size() == 1
        }

        where:
        category                | type                     | description           | amount | date         | amountResponse
        EndpointSpecData.FOOD   | EndpointSpecData.EXPENSE | "Grocery shopping"    | 232.34 | "2022-10-10" | 0 - amount
//        EndpointSpecData.TRAVEL | EndpointSpecData.EXPENSE | "Holiday in Spain"    | 5323   | "2022-11-10" | 0 - amount
//        EndpointSpecData.CAR    | EndpointSpecData.EXPENSE | "Tire service"        | 130    | "2022-12-10" | 0 - amount
//        EndpointSpecData.FOOD   | EndpointSpecData.EXPENSE | "Fruits & vegetables" | 75.99  | "2022-12-11" | 0 - amount
//        EndpointSpecData.SALARY | EndpointSpecData.INCOME  | "December salary"     | 5000   | "2022-10-10" | amount
    }

    def 'should return HTTP status 400 when transaction creation body is not valid'() {

        given: 'prepare new transaction to create with wrong payload'
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
                RequestEntity.post(EndpointSpecData.TRANSACTION_URL_PATTERN.formatted(port, StringUtils.EMPTY))
                        .accept(MediaType.APPLICATION_JSON)
                        .header(EndpointSpecData.AUTHORIZATION_HEADER, EndpointSpecData.BASIC_TOKEN)
                        .body(transactionToCreate)


        when: 'make request to crate transaction'
        client.exchange(creationRequest, Transaction.class)


        then: 'should return correct HTTP status 400'
        def exception = thrown(HttpClientErrorException)
        exception.getStatusCode() == HttpStatus.BAD_REQUEST
        exception.getMessage().startsWith("400 Bad Request")

        where:
        category                | type                     | description           | amount | date
        EndpointSpecData.FOOD   | EndpointSpecData.EXPENSE | "Grocery shopping"    | null   | "2022-10-10"
        EndpointSpecData.TRAVEL | EndpointSpecData.EXPENSE | null                  | 5323   | "2022-11-10"
        EndpointSpecData.CAR    | null                     | "Tire service"        | 130    | "2022-12-10"
        null                    | EndpointSpecData.EXPENSE | "Fruits & vegetables" | 75.99  | "2022-12-11"
        EndpointSpecData.FOOD   | EndpointSpecData.EXPENSE | "Fruits & vegetables" | -75.99 | "2022-12-11"
        EndpointSpecData.SALARY | EndpointSpecData.INCOME  | ""                    | 5000   | "2022-10-10"
    }

    def populateDatabase() {
        log.info("Populating database before test")
        def transactions =
                JsonImporter.getDataFromFile("json/transactions.json", new TypeReference<List<TransactionDto>>() {})
        log.info("Number of transaction to populate {}", transactions.size())
        def addedTransactions = transactions.stream()
                .map(transactionToSave -> transactionService.createTransaction(EndpointSpecData.TEST_USER, transactionToSave))
                .map(transactionMono -> transactionMono.block())
                .collect(Collectors.toList())
        log.info("Transaction added to database: {}", addedTransactions.toString())
    }
}
