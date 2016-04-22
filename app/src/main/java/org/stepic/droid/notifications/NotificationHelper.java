package org.stepic.droid.notifications;

import java.util.HashSet;
import java.util.Set;

public class NotificationHelper {
    public static Set<String> validActions = new HashSet<>();

    static {
        validActions.add("opened");
        validActions.add("closed");
        validActions.add("soft_deadline_approach");
        validActions.add("hard_deadline_approach");
    }

    public static boolean isNotificationValidByAction(String action) {
        return validActions.contains(action);
    }
}
