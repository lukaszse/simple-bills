package pl.com.seremak.simplebills.assetmanagement.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import pl.com.seremak.simplebills.assetmanagement.integration.client.PlanningClient;
import pl.com.seremak.simplebills.assetmanagement.integration.client.TransactionManagementClient;
import pl.com.seremak.simplebills.assetmanagement.repository.DepositRepository;
import pl.com.seremak.simplebills.assetmanagement.repository.DepositSearchRepository;
import pl.com.seremak.simplebills.commons.dto.http.CategoryDto;
import pl.com.seremak.simplebills.commons.dto.http.DepositDto;
import pl.com.seremak.simplebills.commons.dto.queue.TransactionEventDto;
import pl.com.seremak.simplebills.commons.exceptions.WrongPayloadException;
import pl.com.seremak.simplebills.commons.model.Balance;
import pl.com.seremak.simplebills.commons.model.Category;
import pl.com.seremak.simplebills.commons.model.Deposit;
import pl.com.seremak.simplebills.commons.utils.VersionedEntityUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.List;

import static pl.com.seremak.simplebills.commons.converter.DepositConverter.toDeposit;
import static pl.com.seremak.simplebills.commons.converter.TransactionConverter.toTransactionDto;
import static pl.com.seremak.simplebills.commons.model.Transaction.Type.EXPENSE;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositService {

    private final DepositRepository depositRepository;
    private final DepositSearchRepository depositSearchRepository;
    private final PlanningClient planningClient;
    private final TransactionManagementClient transactionManagementClient;


    public void handleTransactionEvent(final TransactionEventDto transactionEventDto) {
        validateAssetTransactionEventDto(transactionEventDto);
        final Mono<Deposit> transactionAction = switch (transactionEventDto.getType()) {
            case UPDATE -> updateDeposit(transactionEventDto);
            case DELETION -> deleteDeposit(transactionEventDto);
            case CREATION -> Mono.empty();
        };
        transactionAction.subscribe();
    }

    public Mono<List<Deposit>> findAllDeposits(final String username) {
        return depositRepository.findAllByUsername(username)
                .collectList()
                .doOnNext(deposits -> log.info("{} deposits for userName={} found.", deposits.size(), username));
    }

    public Mono<Deposit> findDepositByName(final String username, final String depositName) {
        return depositRepository.findByUsernameAndName(username, depositName)
                .doOnNext(deposit ->
                        log.info("Deposit with name={} and username={}  found", deposit.getUsername(), deposit.getName()));
    }

    public Mono<Deposit> createDeposit(final String username, final String authHeader, final DepositDto depositDto) {
        return planningClient.getBalance(authHeader)
                .filter(balance -> validateBalance(balance, depositDto.getValue()))
                .then(planningClient.getCategory(username, authHeader, Category.Type.ASSET.toString().toLowerCase())
                        .switchIfEmpty(planningClient.createCategory(authHeader, prepareAssetCategory())))
                .then(transactionManagementClient.createTransaction(authHeader, toTransactionDto(depositDto)))
                .map(depositTransaction -> toDeposit(username, depositTransaction.getTransactionNumber(), depositDto))
                .map(VersionedEntityUtils::setMetadata)
                .flatMap(depositRepository::save)
                .doOnNext(createdDeposit ->
                        log.info("Deposit with name={} and username={} created.", createdDeposit.getName(), createdDeposit.getUsername()));
    }

    public Mono<Deposit> updateDeposit(final String username,
                                       final String authHeader,
                                       final String depositName,
                                       final DepositDto depositDto) {
        final Deposit deposit = toDeposit(username, depositName, depositDto);
        return depositSearchRepository.updateDepositByName(deposit)
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(updatedDeposit -> transactionManagementClient.updateTransaction(authHeader, toTransactionDto(updatedDeposit)).subscribe())
                .doOnNext(updatedDeposit ->
                        log.info("Deposit with name={} and username={} updated.", updatedDeposit.getName(), updatedDeposit.getUsername()));
    }

    public Mono<Deposit> updateDeposit(final TransactionEventDto transactionEventDto) {
        final Deposit deposit = toDeposit(transactionEventDto);
        return depositSearchRepository.updateDepositByTransactionNumber(deposit)
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(updatedDeposit ->
                        log.info("Deposit with name={} and username={} updated.", updatedDeposit.getName(), updatedDeposit.getUsername()));
    }

    public Mono<Deposit> deleteDeposit(final String username, final String authHeader, final String depositName) {
        return depositRepository.deleteByUsernameAndName(username, depositName)
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(deletedDeposit -> transactionManagementClient.deleteTransaction(authHeader, deletedDeposit.getTransactionNumber()).subscribe())
                .doOnNext(deletedDeposit ->
                        log.info("Deposit with name={} and username={} deleted.", deletedDeposit.getName(), deletedDeposit.getUsername()));
    }

    public Mono<Deposit> deleteDeposit(final TransactionEventDto transactionEventDto) {

        return depositRepository.deleteByUsernameAndTransactionNumber(
                        transactionEventDto.getUsername(),
                        transactionEventDto.getTransactionNumber())
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(deletedDeposit ->
                        log.info("Deposit with name={} and username={} deleted.", deletedDeposit.getName(), deletedDeposit.getUsername()));
    }

    private static boolean validateBalance(final Balance balance, final BigDecimal expense) {
        if (balance.getBalance().compareTo(expense) > 0) {
            log.info("User have enough money to perform transaction");
            return true;
        } else {
            log.info("User don't enough money to perform transaction");
            return false;
        }
    }

    private static CategoryDto prepareAssetCategory() {
        return CategoryDto.builder()
                .type(Category.Type.ASSET)
                .transactionType(EXPENSE.toString())
                .name(Category.Type.ASSET.toString().toLowerCase())
                .build();
    }

    private static void validateAssetTransactionEventDto(final TransactionEventDto transactionEventDto) {
        if (!StringUtils.equalsIgnoreCase(Category.Type.ASSET.toString(), transactionEventDto.getCategoryName())) {
            throw new WrongPayloadException("Wrong message payload. Asset transaction should have `ASSET` category");
        }
    }
}
