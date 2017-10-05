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
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.storage.operations.DatabaseFacade;
import org.stepic.droid.storage.repositories.Repository;
import org.stepic.droid.test_utils.ConcurrencyUtilForTest;
import org.stepic.droid.test_utils.ResponseGeneratorKt;
import org.stepic.droid.test_utils.generators.FakeSectionGenerator;
import org.stepic.droid.test_utils.generators.FakeUnitGenerator;
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

    @Mock
    Repository<Unit> unitRepository;

    @Mock
    Repository<Section> sectionRepository;

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
                unitRepository,
                sectionRepository,
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
        long sectionId = 228L;
        list.add(new LastStep(lastStepId, unitId, stepId));
        LastStepResponse lastStepResponse = new LastStepResponse(null, list);
        ResponseGeneratorKt.useMockInsteadCall(when(api.getLastStepResponse(lastStepId)), lastStepResponse); //ok from web

        //AND now we should check access to the section for user, see https://vyahhi.myjetbrains.com/youtrack/issue/APPS-1494#comment=74-39826
        Unit unit = FakeUnitGenerator.INSTANCE.generate(unitId, sectionId);
        when(unitRepository.getObject(unitId)).thenReturn(unit);
        Section section = FakeSectionGenerator.INSTANCE.generate(sectionId, new long[]{unitId});
        when(sectionRepository.getObject(sectionId)).thenReturn(section);

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
        long unitId = 33;
        long sectionId = 24;
        Step step = new Step();
        step.setId(stepId);
        PersistentLastStep persistentLastStep = new PersistentLastStep(courseId, stepId, unitId);
        when(api.getLastStepResponse(lastStepId)).thenThrow(new RuntimeException("No connection"));
        when(databaseFacade.getLocalLastStepByCourseId(courseId)).thenReturn(persistentLastStep);
        when(stepRepository.getObject(stepId)).thenReturn(step);
        //AND we should check, that user has access to section
        when(sectionRepository.getObject(sectionId)).thenReturn(FakeSectionGenerator.INSTANCE.generate(sectionId));
        when(unitRepository.getObject(unitId)).thenReturn(FakeUnitGenerator.INSTANCE.generate(unitId, sectionId));

        lastStepPresenter.attachView(lastStepView);
        lastStepPresenter.fetchLastStep(lastStepId, courseId);

        verify(lastStepView).onShowLastStep(step);
        verify(lastStepView, never()).onShowPlaceholder();
        //do not use direct call in stepPresenter to this method of DatabaseFacade,
        // use repository instead
        verify(databaseFacade, never()).getStepById(stepId);
    }
}
