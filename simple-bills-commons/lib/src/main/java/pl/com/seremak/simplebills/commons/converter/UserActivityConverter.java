package pl.com.seremak.simplebills.commons.converter;

import pl.com.seremak.simplebills.commons.model.UserActivity;

public class UserActivityConverter {

    public static UserActivity toUserActivity(final String username, final String activityName) {
        final UserActivity.Activity activity = UserActivity.Activity.valueOf(activityName.toUpperCase());
        return new UserActivity(username, activity);
    }
}
