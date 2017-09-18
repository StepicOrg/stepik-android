package org.stepic.droid.core.presenters;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.core.FirstStepInCourseHelper;
import org.stepic.droid.core.presenters.contracts.LastStepView;
import org.stepic.droid.model.LastStep;
import org.stepic.droid.model.Meta;
import org.stepic.droid.model.Step;
import org.stepic.droid.storage.operations.DatabaseFacade;
import org.stepic.droid.storage.repositories.Repository;
import org.stepic.droid.test_utils.ConcurrencyUtilForTest;
import org.stepic.droid.test_utils.ResponseGeneratorKt;
import org.stepic.droid.web.Api;
import org.stepic.droid.web.LastStepResponse;
import org.stepic.droid.web.StepResponse;

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
    public void nullId_showPlaceholderInsta() {
        lastStepPresenter.attachView(lastStepView);
        lastStepPresenter.fetchLastStep(null, 67);

        verify(threadPoolExecutor, never()).execute(any(Runnable.class));
        verify(lastStepView).onShowPlaceholder();
        verify(lastStepView, never()).onShowLastStep(any(Step.class));
    }

    @Test
    public void lastStepIsExistDbIsEmpty_showStep() {
        String lastStepId = "78-3114";
        List<LastStep> list = new ArrayList<>();
        long stepId = 197842L;
        long unitId = 26462L;
        list.add(new LastStep("78-3114", unitId, stepId));
        LastStepResponse lastStepResponse = new LastStepResponse(null, list);
        ResponseGeneratorKt.useMockInsteadCall(when(api.getLastStepResponse(lastStepId)), lastStepResponse); //ok from web
        long[] steps = new long[1];
        steps[0] = stepId;
        Step step = new Step();
        step.setId(stepId);
        ArrayList<Step> stepList = new ArrayList<>();
        stepList.add(step);
        StepResponse stepResponse = new StepResponse(new Meta(1, false, false), stepList);
        ResponseGeneratorKt.useMockInsteadCall(when(api.getSteps(steps)), stepResponse); //ok from web
        when(databaseFacade.getStepById(stepId)).thenReturn(null); //null in database

        lastStepPresenter.attachView(lastStepView);
        lastStepPresenter.fetchLastStep(lastStepId, 3114);

        verify(lastStepView, never()).onShowPlaceholder();
        verify(lastStepView).onShowLastStep(step);
        verify(api).getSteps(any(long[].class));
        verify(databaseFacade).getStepById(stepId); //we check that presenter try to go into database before getting from api
    }
}
