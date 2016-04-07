package org.stepic.droid.notifications;


import org.junit.Test;
import static junit.framework.Assert.*;

public class NotificationHelperTest {
    @Test
    public void testIsValidByAction_True() {
        assertTrue(NotificationHelper.isNotificationValidByAction("soft_deadline_approach"));
    }

    @Test
    public void testIsValidByAction_False() {
        assertFalse(NotificationHelper.isNotificationValidByAction("invalid_not_812uqw12"));
    }
}
