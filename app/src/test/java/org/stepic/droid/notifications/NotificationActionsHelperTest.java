package org.stepic.droid.notifications;


import org.junit.Test;

import static junit.framework.Assert.assertFalse;
public class NotificationActionsHelperTest {

    @Test
    public void testIsValidByAction_Null_False(){
        assertFalse(NotificationActionsHelper.INSTANCE.isNotificationValidByAction(null));
    }
}
