package org.stepic.droid.test_utils;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.stepic.droid.concurrency.MainHandler;

import java.util.concurrent.ThreadPoolExecutor;

import kotlin.jvm.functions.Function0;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;

public class ConcurrencyUtilForTest {

    public static void transformToBlockingMock(ThreadPoolExecutor threadPoolExecutor) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object firstArg = invocation.getArguments()[0];
                ((Runnable) firstArg).run();
                return null;
            }
        }).when(threadPoolExecutor)
                .execute(any(Runnable.class));
    }

    public static void transformToBlockingMock(MainHandler mainHandler) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object firstArg = invocation.getArguments()[0];
                ((Function0) firstArg).invoke();
                return null;
            }
        }).when(mainHandler).post(any(Function0.class));
    }
}
