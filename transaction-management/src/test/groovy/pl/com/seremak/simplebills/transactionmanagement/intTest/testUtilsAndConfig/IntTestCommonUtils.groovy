package pl.com.seremak.simplebills.transactionmanagement.intTest.testUtilsAndConfig

import com.fasterxml.jackson.core.type.TypeReference
import groovy.util.logging.Slf4j
import pl.com.seremak.simplebills.commons.dto.http.TransactionDto
import pl.com.seremak.simplebills.transactionmanagement.service.TransactionService
import pl.com.seremak.simplebills.transactionmanagement.testUtils.JsonImporter

import java.util.stream.Collectors

@Slf4j
class IntTestCommonUtils {

    static def TEST_USER = "testUser"
    static def SALARY = "salary"
    static def BASIC_TOKEN = "Basic dGVzdFVzZXI6cGFzc3dvcmQ="
    static def FOOD = "food"
    static def TRAVEL = "travel"
    static def CAR = "car"
    static def ASSET = "asset"
    static def EXPENSE = "EXPENSE"
    static def INCOME = "INCOME"
    static def TRANSACTION_URL_PATTERN = "http://localhost:%d/transactions%s"
    static def AUTHORIZATION_HEADER = "Authorization"

    static def populateDatabase(TransactionService transactionService) {
        log.info("Populating database before test")
        def transactions =
                JsonImporter.getDataFromFile("json/transactions.json", new TypeReference<List<TransactionDto>>() {})
        log.info("Number of transaction to populate {}", transactions.size())
        def addedTransactions = transactions.stream()
                .map(transactionToSave -> transactionService.createTransaction(TEST_USER, transactionToSave))
                .map(transactionMono -> transactionMono.block())
                .collect(Collectors.toList())
        log.info("Transaction added to database: {}", addedTransactions.toString())
    }
}
