package org.stepic.droid.core.presenters;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.core.presenters.contracts.InstructorsView;
import org.stepic.droid.model.Course;
import org.stepik.android.model.User;
import org.stepic.droid.testUtils.ConcurrencyUtilForTest;
import org.stepic.droid.testUtils.ResponseGeneratorKt;
import org.stepic.droid.testUtils.generators.FakeCourseGenerator;
import org.stepic.droid.testUtils.generators.FakeUserGenerator;
import org.stepic.droid.web.Api;
import org.stepic.droid.web.UsersResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class InstructorsPresenterTest {
    private InstructorsPresenter instructorsPresenter;

    @Mock
    ThreadPoolExecutor threadPoolExecutor;

    @Mock
    MainHandler mainHandler;

    @Mock
    Api api;

    @Mock
    InstructorsView instructorsView;


    @Before
    public void beforeEachTest() throws IOException {
        MockitoAnnotations.initMocks(this);

        ConcurrencyUtilForTest.transformToBlockingMock(threadPoolExecutor);
        ConcurrencyUtilForTest.transformToBlockingMock(mainHandler);


        instructorsPresenter = new InstructorsPresenter(
                threadPoolExecutor,
                mainHandler,
                api
        );
    }


    @Test
    public void nullCourse_hideInstructors() {

        instructorsPresenter.attachView(instructorsView);
        instructorsPresenter.fetchInstructors(null);

        //verify
        verify(threadPoolExecutor, never()).execute(any(Runnable.class)); //should be insta on main thread

        verify(instructorsView).onHideInstructors();
        verify(instructorsView, never()).onFailLoadInstructors();
        verify(instructorsView, never()).onLoadingInstructors();
        verify(instructorsView, never()).onInstructorsLoaded(any(List.class));
    }

    @Test
    public void emptyInstructors_hideInstructors() {
        Course course = FakeCourseGenerator.INSTANCE.generate(67);
        course.setInstructors(new long[0]);

        instructorsPresenter.attachView(instructorsView);
        instructorsPresenter.fetchInstructors(course);

        verify(threadPoolExecutor, never()).execute(any(Runnable.class));

        verify(instructorsView).onHideInstructors();
        verifyNoMoreInteractions(instructorsView);
    }

    @Test
    public void oneInstructor_instructorsLoaded() {
        //setup
        Course course = FakeCourseGenerator.INSTANCE.generate(67);
        long userId = 1112234;
        long[] instructors = new long[1];
        instructors[0] = userId;
        course.setInstructors(instructors);

        User instructor = FakeUserGenerator.INSTANCE.generate(userId);
        List<User> instructorList = new ArrayList<>();
        instructorList.add(instructor);

        UsersResponse responseMock = mock(UsersResponse.class);
        when(responseMock.getUsers()).thenReturn(instructorList);
        ResponseGeneratorKt.useMockInsteadCall(when(api.getUsers(any(long[].class))), responseMock);

        //call
        instructorsPresenter.attachView(instructorsView);
        instructorsPresenter.fetchInstructors(course);

        //verify
        verify(threadPoolExecutor, only()).execute(any(Runnable.class));

        InOrder inOrder = inOrder(instructorsView);
        inOrder.verify(instructorsView).onLoadingInstructors();
        inOrder.verify(instructorsView).onInstructorsLoaded(instructorList);
        verifyNoMoreInteractions(instructorsView);
    }

    @Test
    public void noInternet_failInstructors() {
        Course course = FakeCourseGenerator.INSTANCE.generate(67);
        long userId = 1112234;
        long[] instructors = new long[1];
        instructors[0] = userId;
        course.setInstructors(instructors);
        when(api.getUsers(any(long[].class)))
                .thenThrow(RuntimeException.class); //throw exception on getting from api instead of executing for simplify testing


        instructorsPresenter.attachView(instructorsView);
        instructorsPresenter.fetchInstructors(course);

        InOrder inOrder = inOrder(instructorsView);
        inOrder.verify(instructorsView).onLoadingInstructors();
        inOrder.verify(instructorsView).onFailLoadInstructors();
        verifyNoMoreInteractions(instructorsView);
    }

    @Test
    public void multiplyFetchingSameCourse_showCachedData() {
        Course course = FakeCourseGenerator.INSTANCE.generate(67);
        long userId = 1112234;
        long[] instructors = new long[1];
        instructors[0] = userId;
        course.setInstructors(instructors);

        User instructor = FakeUserGenerator.INSTANCE.generate(userId);
        List<User> instructorList = new ArrayList<>();
        instructorList.add(instructor);

        UsersResponse responseMock = mock(UsersResponse.class);
        when(responseMock.getUsers()).thenReturn(instructorList);
        ResponseGeneratorKt.useMockInsteadCall(when(api.getUsers(any(long[].class))), responseMock);

        //call
        int numberOfCalls = 6;
        instructorsPresenter.attachView(instructorsView);
        for (int i = 0; i < numberOfCalls; i++) {
            instructorsPresenter.fetchInstructors(course); //assumption, that code is not async
        }


        //verify
        verify(api, only()).getUsers(any(long[].class));
        verify(threadPoolExecutor, only()).execute(any(Runnable.class));

        verify(instructorsView, times(1)).onLoadingInstructors();
        verify(instructorsView, times(numberOfCalls)).onInstructorsLoaded(instructorList);
    }


}
