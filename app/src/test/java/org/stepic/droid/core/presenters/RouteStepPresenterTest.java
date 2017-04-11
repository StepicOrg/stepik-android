package org.stepic.droid.core.presenters;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.core.presenters.contracts.RouteStepView;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;
import org.stepic.droid.storage.operations.DatabaseFacade;
import org.stepic.droid.test_utils.ConcurrencyUtilForTest;
import org.stepic.droid.test_utils.generators.ArrayHelper;
import org.stepic.droid.test_utils.generators.FakeLessonGenerator;
import org.stepic.droid.test_utils.generators.FakeSectionGenerator;
import org.stepic.droid.test_utils.generators.FakeUnitGenerator;
import org.stepic.droid.web.Api;

import java.util.concurrent.ThreadPoolExecutor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RouteStepPresenterTest {

    private RouteStepPresenter routeStepPresenter;

    @Mock
    private RouteStepView routeStepView;

    @Mock
    private ThreadPoolExecutor threadPoolExecutor;

    @Mock
    private MainHandler mainHandler;

    @Mock
    private DatabaseFacade databaseFacade;

    @Mock
    private Analytic analytic;

    @Mock
    private Api api;


    @Before
    public void beforeEachTest() {
        MockitoAnnotations.initMocks(this);

        ConcurrencyUtilForTest.transformToBlockingMock(threadPoolExecutor);
        ConcurrencyUtilForTest.transformToBlockingMock(mainHandler);

        routeStepPresenter = new RouteStepPresenter(threadPoolExecutor, mainHandler, databaseFacade, analytic, api);
    }

    @Test
    public void checkStepForFirst_firstStepInCourse_SectionCached_notShowAny() {
        routeStepPresenter.attachView(routeStepView);

        long stepId = 31;
        long sectionId = 12;
        long unitId = 48;

        long stepIds[] = ArrayHelper.INSTANCE.arrayOf(stepId);
        long unitIds[] = ArrayHelper.INSTANCE.arrayOf(unitId);
        Lesson lesson = FakeLessonGenerator.INSTANCE.generate(stepIds);
        Unit unit = FakeUnitGenerator.INSTANCE.generate(unitId, sectionId);
        Section section = FakeSectionGenerator.INSTANCE.generate(sectionId, unitIds, 1);

        when(databaseFacade.getSectionById(sectionId)).thenReturn(section); //return section with position 1 (it is enough)

        routeStepPresenter.checkStepForFirst(stepId, lesson, unit);

        routeStepPresenter.detachView(routeStepView);

        verify(routeStepView, never()).showLoading();
        verify(routeStepView, never()).openNextLesson(any(Unit.class), any(Lesson.class));
        verify(routeStepView, never()).showCantGoNext();
        verify(routeStepView, never()).openPreviousLesson(any(Unit.class), any(Lesson.class));
        verify(routeStepView, never()).showNextLessonView();
        verify(routeStepView, never()).showPreviousLessonView();
        verify(routeStepView, never()).showCantGoPrevious();
    }


    @Test
    public void checkStepForFirst_firstStepInCourse_SectionNotCached_notShowAny() {
        routeStepPresenter.attachView(routeStepView);

        long stepId = 31;
        long sectionId = 12;
        long unitId = 48;

        long stepIds[] = ArrayHelper.INSTANCE.arrayOf(stepId);
        Lesson lesson = FakeLessonGenerator.INSTANCE.generate(stepIds);
        Unit unit = FakeUnitGenerator.INSTANCE.generate(unitId, sectionId);

        when(databaseFacade.getSectionById(sectionId)).thenReturn(null); //return section null, section is not cached

        routeStepPresenter.checkStepForFirst(stepId, lesson, unit);

        routeStepPresenter.detachView(routeStepView);

        verify(routeStepView, never()).showLoading();
        verify(routeStepView, never()).openNextLesson(any(Unit.class), any(Lesson.class));
        verify(routeStepView, never()).showCantGoNext();
        verify(routeStepView, never()).openPreviousLesson(any(Unit.class), any(Lesson.class));
        verify(routeStepView, never()).showNextLessonView();
        verify(routeStepView, never()).showPreviousLessonView();
        verify(routeStepView, never()).showCantGoPrevious();
    }

    @Test
    public void checkStepForFirst_notFirstUnit_showPrevious() {
        routeStepPresenter.attachView(routeStepView);
        long stepId = 31;
        long sectionId = 12;
        long unitId = 48;
        int unitPosition = 2;

        long stepIds[] = ArrayHelper.INSTANCE.arrayOf(stepId, stepId + 1, stepId + 2);
        Lesson lesson = FakeLessonGenerator.INSTANCE.generate(stepIds);
        Unit unit = FakeUnitGenerator.INSTANCE.generate(unitId, sectionId, unitPosition);

        verify(databaseFacade, never()).getSectionById(any(Long.class)); // database should not be triggered, because we can choose by position of unit

        routeStepPresenter.checkStepForFirst(stepId, lesson, unit);

        routeStepPresenter.detachView(routeStepView);

        verify(routeStepView, never()).showLoading();
        verify(routeStepView, never()).openNextLesson(any(Unit.class), any(Lesson.class));
        verify(routeStepView, never()).showCantGoNext();
        verify(routeStepView, never()).openPreviousLesson(any(Unit.class), any(Lesson.class));
        verify(routeStepView, never()).showNextLessonView();
        verify(routeStepView, never()).showCantGoPrevious();

        verify(routeStepView).showPreviousLessonView();
    }
}
