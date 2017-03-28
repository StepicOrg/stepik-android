package org.stepic.droid.notifications;


import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.ThreadPoolExecutor;

import static junit.framework.Assert.assertFalse;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
public class NotificationHelperTest {

    @Test
    public void testIsValidByAction_Null_False(){
        assertFalse(NotificationHelper.INSTANCE.isNotificationValidByAction(null));

        ThreadPoolExecutor threadPoolExecutor = mock(ThreadPoolExecutor.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(threadPoolExecutor)
                .execute(any(Runnable.class));
    }
}
