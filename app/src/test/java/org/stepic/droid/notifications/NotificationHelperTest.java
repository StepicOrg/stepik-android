package org.stepic.droid.notifications;


import org.junit.Test;
import static junit.framework.Assert.*;

public class NotificationHelperTest {

    @Test
    public void testIsValidByAction_Null_False(){
        assertFalse(NotificationHelper.INSTANCE.isNotificationValidByAction(null));
    }
}
