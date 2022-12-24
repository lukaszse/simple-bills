package pl.com.seremak.simplebills.transactionmanagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.com.seremak.simplebills.commons.converter.UserActivityConverter;
import pl.com.seremak.simplebills.commons.model.UserActivity;
import pl.com.seremak.simplebills.commons.utils.VersionedEntityUtils;
import pl.com.seremak.simplebills.transactionmanagement.messageQueue.MessagePublisher;
import pl.com.seremak.simplebills.transactionmanagement.repository.UserActivityRepository;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivityService {


    public static final String MESSAGE_TO_CATEGORY_SERVICE_IS_BEING_SENT_MSG = """
            The user is logging in for the first time. A message to category service to create standard categories set is being sent""";
    public static final String USER_EXISTS_MSG = """
            User already exists. No message to create standard categories is sent""";
    private final MessagePublisher messagePublisher;
    private final UserActivityRepository userActivityRepository;

    public Mono<UserActivity> addUserActivity(final String username, final String activityName) {
        final UserActivity newUserActivity = UserActivityConverter.toUserActivity(username, activityName);
        return createStandardCategoriesForNewUser(newUserActivity)
                .then(Mono.just(newUserActivity))
                .map(VersionedEntityUtils::setMetadata)
                .flatMap(userActivityRepository::save);
    }

    private Mono<UserActivity> createStandardCategoriesForNewUser(final UserActivity userActivity) {
        if (!Objects.equals(userActivity.getActivity(), UserActivity.Activity.LOGGING_IN)) {
            return Mono.just(userActivity);
        }
        return checkIfUserAlreadyExists(userActivity)
                .doOnNext(userNotExists -> sendMessageToCategoryService(userNotExists, userActivity.getUsername()))
                .doOnSuccess(UserActivityService::logInfoAboutMessageToCategoryService)
                .then(Mono.just(userActivity));
    }

    private Mono<Boolean> checkIfUserAlreadyExists(UserActivity userActivity) {
        return userActivityRepository.countByUsernameAndActivity(userActivity.getUsername(), UserActivity.Activity.LOGGING_IN)
                .map(count -> count == 0);
    }

    private void sendMessageToCategoryService(final boolean userNotExists, final String username) {
        if (userNotExists) {
            messagePublisher.sendUserCreationMessage(username);
        }
    }

    private static void logInfoAboutMessageToCategoryService(final boolean userNotExists) {
        if (userNotExists) {
            log.info(MESSAGE_TO_CATEGORY_SERVICE_IS_BEING_SENT_MSG);
        } else {
            log.info(USER_EXISTS_MSG);
        }
    }
}
