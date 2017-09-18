package org.stepic.droid.core.presenters;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.core.FirstStepInCourseHelper;
import org.stepic.droid.core.presenters.contracts.LastStepView;
import org.stepic.droid.model.LastStep;
import org.stepic.droid.model.PersistentLastStep;
import org.stepic.droid.model.Step;
import org.stepic.droid.storage.operations.DatabaseFacade;
import org.stepic.droid.storage.repositories.Repository;
import org.stepic.droid.test_utils.ConcurrencyUtilForTest;
import org.stepic.droid.test_utils.ResponseGeneratorKt;
import org.stepic.droid.web.Api;
import org.stepic.droid.web.LastStepResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LastStepPresenterTest {
    private LastStepPresenter lastStepPresenter;

    @Mock
    ThreadPoolExecutor threadPoolExecutor;

    @Mock
    MainHandler mainHandler;

    @Mock
    Api api;

    @Mock
    DatabaseFacade databaseFacade;

    @Mock
    LastStepView lastStepView;

    @Mock
    FirstStepInCourseHelper firstStepInCourseHelper;

    @Mock
    Repository<Step> stepRepository;

    private final int courseId = 3114;
    private final String lastStepId = "78-3114";

    @Before
    public void beforeEachTest() throws IOException {
        MockitoAnnotations.initMocks(this);

        ConcurrencyUtilForTest.transformToBlockingMock(threadPoolExecutor);
        ConcurrencyUtilForTest.transformToBlockingMock(mainHandler);


        lastStepPresenter = new LastStepPresenter(
                threadPoolExecutor,
                mainHandler,
                databaseFacade,
                api,
                stepRepository,
                firstStepInCourseHelper
        );
    }

    @Test
    public void nullId_placeholderImmediately() {
        lastStepPresenter.attachView(lastStepView);
        lastStepPresenter.fetchLastStep(null, 67);

        verify(threadPoolExecutor, never()).execute(any(Runnable.class));
        verify(lastStepView).onShowPlaceholder();
        verify(lastStepView, never()).onShowLastStep(any(Step.class));
    }

    @Test
    public void lastStepIsExistDbIsEmpty_showStep() {
        List<LastStep> list = new ArrayList<>();
        long stepId = 197842L;
        long unitId = 26462L;
        list.add(new LastStep(lastStepId, unitId, stepId));
        LastStepResponse lastStepResponse = new LastStepResponse(null, list);
        ResponseGeneratorKt.useMockInsteadCall(when(api.getLastStepResponse(lastStepId)), lastStepResponse); //ok from web

        Step step = new Step();
        step.setId(stepId);
        when(stepRepository.getObject(stepId)).thenReturn(step); //ok step from repo (db or api, it is doesn't matter)


        lastStepPresenter.attachView(lastStepView);
        lastStepPresenter.fetchLastStep(lastStepId, courseId);


        verify(lastStepView, never()).onShowPlaceholder();
        verify(lastStepView).onShowLastStep(step);

        //when, we have this dependency, we should use it, instead of direct call to databaseFacade
        verify(stepRepository).getObject(stepId);
        verify(databaseFacade, never()).getStepById(any(long.class));
    }

    @Test
    public void noInternetNotSavedLocally_placeholder() {
        when(api.getLastStepResponse(lastStepId)).thenThrow(new RuntimeException("No connection"));
        when(databaseFacade.getLocalLastStepByCourseId(courseId)).thenReturn(null);

        lastStepPresenter.attachView(lastStepView);
        lastStepPresenter.fetchLastStep(lastStepId, courseId);

        verify(lastStepView).onShowPlaceholder();
        verify(lastStepView, never()).onShowLastStep(any(Step.class));
    }

    @Test
    public void noInternetSavedLocally_showStep() {
        int stepId = 1133;
        Step step = new Step();
        step.setId(stepId);
        PersistentLastStep persistentLastStep = new PersistentLastStep(courseId, stepId, 7);
        when(api.getLastStepResponse(lastStepId)).thenThrow(new RuntimeException("No connection"));
        when(databaseFacade.getLocalLastStepByCourseId(courseId)).thenReturn(persistentLastStep);
        when(stepRepository.getObject(stepId)).thenReturn(step);

        lastStepPresenter.attachView(lastStepView);
        lastStepPresenter.fetchLastStep(lastStepId, courseId);

        verify(lastStepView).onShowLastStep(step);
        verify(lastStepView, never()).onShowPlaceholder();
        //do not use direct call in stepPresenter to this method of DatabaseFacade,
        // use repository instead
        verify(databaseFacade, never()).getStepById(stepId);
    }
}
