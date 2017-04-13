package org.stepic.droid.core.presenters;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.core.presenters.contracts.RouteStepView;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;
import org.stepic.droid.storage.repositories.Repository;
import org.stepic.droid.test_utils.ConcurrencyUtilForTest;
import org.stepic.droid.test_utils.generators.ArrayHelper;
import org.stepic.droid.test_utils.generators.FakeCourseGenerator;
import org.stepic.droid.test_utils.generators.FakeLessonGenerator;
import org.stepic.droid.test_utils.generators.FakeSectionGenerator;
import org.stepic.droid.test_utils.generators.FakeUnitGenerator;
import org.stepic.droid.test_utils.generators.ListHelper;

import java.util.List;
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
    private Analytic analytic;

    @Mock
    private Repository<Course> courseRepository;

    @Mock
    private Repository<Section> sectionRepository;

    @Mock
    private Repository<Unit> unitRepository;


    @Before
    public void beforeEachTest() {
        MockitoAnnotations.initMocks(this);

        ConcurrencyUtilForTest.transformToBlockingMock(threadPoolExecutor);
        ConcurrencyUtilForTest.transformToBlockingMock(mainHandler);

        routeStepPresenter = new RouteStepPresenter(
                threadPoolExecutor,
                mainHandler,
                analytic,
                courseRepository,
                sectionRepository,
                unitRepository);
    }

    @Test
    public void checkStepForFirst_firstStepInCourse_notShowAny() {
        long stepId = 31;
        long sectionId = 12;
        long unitId = 48;

        long stepIds[] = ArrayHelper.INSTANCE.arrayOf(stepId);
        long unitIds[] = ArrayHelper.INSTANCE.arrayOf(unitId);
        Lesson lesson = FakeLessonGenerator.INSTANCE.generate(stepIds);
        Unit unit = FakeUnitGenerator.INSTANCE.generate(unitId, sectionId);
        Section section = FakeSectionGenerator.INSTANCE.generate(sectionId, unitIds, 1);

        when(sectionRepository.getObject(sectionId)).thenReturn(section); //return section with position 1 (it is enough)

        routeStepPresenter.attachView(routeStepView);
        routeStepPresenter.checkStepForFirst(stepId, lesson, unit);
        routeStepPresenter.detachView(routeStepView);

        verify(courseRepository, never()).getObject(any(Long.class));
        verify(unitRepository, never()).getObject(any(Long.class));

        verify(routeStepView, never()).showLoading();
        verify(routeStepView, never()).openNextLesson(any(Unit.class), any(Lesson.class));
        verify(routeStepView, never()).showCantGoNext();
        verify(routeStepView, never()).openPreviousLesson(any(Unit.class), any(Lesson.class));
        verify(routeStepView, never()).showNextLessonView();
        verify(routeStepView, never()).showPreviousLessonView();
        verify(routeStepView, never()).showCantGoPrevious();
    }


    @Test
    public void checkStepForFirst_firstStepInSection_beforeSectionsIsClosed_notShowAny() {
        long stepId = 31;
        long sectionId = 12;
        long unitId = 48;
        long courseId = 67;

        long stepIds[] = ArrayHelper.INSTANCE.arrayOf(stepId);
        long unitIds[] = ArrayHelper.INSTANCE.arrayOf(unitId);
        long unitIdsSecond[] = ArrayHelper.INSTANCE.arrayOf(unitId - 20);
        long expectedSectionIds[] = ArrayHelper.INSTANCE.arrayOf(sectionId - 1);
        Lesson lesson = FakeLessonGenerator.INSTANCE.generate(stepIds);

        Unit unit = FakeUnitGenerator.INSTANCE.generate(unitId, sectionId, 1); //the 1st unit in section
        Section section = FakeSectionGenerator.INSTANCE.generate(sectionId, unitIds, 2, courseId); //the 2nd section
        Section closedSection = FakeSectionGenerator.INSTANCE.generate(sectionId - 1, unitIdsSecond, 1, courseId, false);
        Course course = FakeCourseGenerator.INSTANCE.generate(courseId, expectedSectionIds);
        List<Section> sections = ListHelper.INSTANCE.listOf(closedSection, section);

        when(sectionRepository.getObject(sectionId)).thenReturn(section);
        when(courseRepository.getObject(courseId)).thenReturn(course);
        when(sectionRepository.getObjects(expectedSectionIds)).thenReturn(sections);

        routeStepPresenter.attachView(routeStepView);
        routeStepPresenter.checkStepForFirst(stepId, lesson, unit);
        routeStepPresenter.detachView(routeStepView);

        verify(courseRepository).getObject(courseId);
        verify(sectionRepository).getObject(sectionId);
        verify(sectionRepository).getObjects(expectedSectionIds); // we should do optimization and do not request section, which is already requested
        verify(unitRepository, never()).getObject(any(Long.class));

        verify(routeStepView, never()).showLoading();
        verify(routeStepView, never()).openNextLesson(any(Unit.class), any(Lesson.class));
        verify(routeStepView, never()).showCantGoNext();
        verify(routeStepView, never()).openPreviousLesson(any(Unit.class), any(Lesson.class));
        verify(routeStepView, never()).showNextLessonView();
        verify(routeStepView, never()).showPreviousLessonView();
        verify(routeStepView, never()).showCantGoPrevious();
    }

    @Test
    public void checkStepForFirst_firstStepInCourse_noInternet_notShowAny() {
        long stepId = 31;
        long sectionId = 12;
        long unitId = 48;

        long stepIds[] = ArrayHelper.INSTANCE.arrayOf(stepId);
        Lesson lesson = FakeLessonGenerator.INSTANCE.generate(stepIds);
        Unit unit = FakeUnitGenerator.INSTANCE.generate(unitId, sectionId);

        when(sectionRepository.getObject(sectionId)).thenReturn(null); //return section null, no internet

        routeStepPresenter.attachView(routeStepView);
        routeStepPresenter.checkStepForFirst(stepId, lesson, unit);
        routeStepPresenter.detachView(routeStepView);

        verify(courseRepository, never()).getObject(any(Long.class));
        verify(unitRepository, never()).getObject(any(Long.class));

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

        long stepId = 31;
        long sectionId = 12;
        long unitId = 48;
        int unitPosition = 2;

        long stepIds[] = ArrayHelper.INSTANCE.arrayOf(stepId, stepId + 1, stepId + 2);
        Lesson lesson = FakeLessonGenerator.INSTANCE.generate(stepIds);
        Unit unit = FakeUnitGenerator.INSTANCE.generate(unitId, sectionId, unitPosition);

        routeStepPresenter.attachView(routeStepView);
        routeStepPresenter.checkStepForFirst(stepId, lesson, unit);
        routeStepPresenter.detachView(routeStepView);

        verify(sectionRepository, never()).getObject(any(Long.class)); // repository should not be triggered, because we can choose by position of unit
        verify(courseRepository, never()).getObject(any(Long.class));
        verify(unitRepository, never()).getObject(any(Long.class));

        verify(routeStepView, never()).showLoading();
        verify(routeStepView, never()).openNextLesson(any(Unit.class), any(Lesson.class));
        verify(routeStepView, never()).showCantGoNext();
        verify(routeStepView, never()).openPreviousLesson(any(Unit.class), any(Lesson.class));
        verify(routeStepView, never()).showNextLessonView();
        verify(routeStepView, never()).showCantGoPrevious();

        verify(routeStepView).showPreviousLessonView();
    }

    @Test
    public void checkStepForLast_lastUnitLastSection_notShowAny() {
        long stepId = 31;
        long sectionId = 12;
        long unitId = 48;
        int unitPosition = 3;
        long courseId = 221;

        long stepIds[] = ArrayHelper.INSTANCE.arrayOf(stepId - 2, stepId - 1, stepId);
        long unitIds[] = ArrayHelper.INSTANCE.arrayOf(unitId - 2, unitId - 1, unitId);
        long sectionIds[] = ArrayHelper.INSTANCE.arrayOf(sectionId - 2, sectionId - 1, sectionId);

        Lesson lesson = FakeLessonGenerator.INSTANCE.generate(stepIds);
        Unit unit = FakeUnitGenerator.INSTANCE.generate(unitId, sectionId, unitPosition);
        Section section = FakeSectionGenerator.INSTANCE.generate(sectionId, unitIds, 3, courseId);
        Course course = FakeCourseGenerator.INSTANCE.generate(courseId, sectionIds);

        when(sectionRepository.getObject(sectionId)).thenReturn(section);
        when(courseRepository.getObject(courseId)).thenReturn(course);

        routeStepPresenter.attachView(routeStepView);
        routeStepPresenter.checkStepForLast(stepId, lesson, unit); // resolve by unitIds in section and sectionIds in course
        routeStepPresenter.detachView(routeStepView);

        verify(sectionRepository).getObject(sectionId);
        verify(courseRepository).getObject(courseId); // we can't know that it is last section, while we do not get course and sectionIds
        verify(unitRepository, never()).getObject(any(Long.class));

        verify(routeStepView, never()).showLoading();
        verify(routeStepView, never()).openNextLesson(any(Unit.class), any(Lesson.class));
        verify(routeStepView, never()).showCantGoNext();
        verify(routeStepView, never()).openPreviousLesson(any(Unit.class), any(Lesson.class));
        verify(routeStepView, never()).showNextLessonView();
        verify(routeStepView, never()).showCantGoPrevious();
        verify(routeStepView, never()).showPreviousLessonView();
    }


    @Test
    public void checkStepForLast_lastUnitNotLastStep_notShowAny() {
        long stepId = 31;
        long sectionId = 12;
        long unitId = 48;
        int unitPosition = 2;

        long stepIds[] = ArrayHelper.INSTANCE.arrayOf(stepId - 2, stepId - 1, stepId, stepId + 1);
        long unitIds[] = ArrayHelper.INSTANCE.arrayOf(unitId - 2, unitId - 1, unitId);
        Lesson lesson = FakeLessonGenerator.INSTANCE.generate(stepIds);
        Unit unit = FakeUnitGenerator.INSTANCE.generate(unitId, sectionId, unitPosition);

        routeStepPresenter.attachView(routeStepView);
        routeStepPresenter.checkStepForLast(stepId, lesson, unit); // resolve by unitIds in section
        routeStepPresenter.detachView(routeStepView);

        verify(sectionRepository, never()).getObject(any(Long.class));
        verify(courseRepository, never()).getObject(any(Long.class));
        verify(unitRepository, never()).getObject(any(Long.class));

        verify(routeStepView, never()).showLoading();
        verify(routeStepView, never()).openNextLesson(any(Unit.class), any(Lesson.class));
        verify(routeStepView, never()).showCantGoNext();
        verify(routeStepView, never()).openPreviousLesson(any(Unit.class), any(Lesson.class));
        verify(routeStepView, never()).showNextLessonView();
        verify(routeStepView, never()).showCantGoPrevious();
        verify(routeStepView, never()).showPreviousLessonView();
    }

    @Test
    public void checkStepForLast_notLastUnit_showNext() {
        long stepId = 31;
        long sectionId = 12;
        long unitId = 48;
        int unitPosition = 2;

        long stepIds[] = ArrayHelper.INSTANCE.arrayOf(stepId - 2, stepId - 1, stepId);
        long unitIds[] = ArrayHelper.INSTANCE.arrayOf(unitId - 2, unitId - 1, unitId, unitId + 1);

        Lesson lesson = FakeLessonGenerator.INSTANCE.generate(stepIds);
        Unit unit = FakeUnitGenerator.INSTANCE.generate(unitId, sectionId, unitPosition);
        Section section = FakeSectionGenerator.INSTANCE.generate(sectionId, unitIds);

        when(sectionRepository.getObject(sectionId)).thenReturn(section);

        routeStepPresenter.attachView(routeStepView);
        routeStepPresenter.checkStepForLast(stepId, lesson, unit); // resolve by unitIds in section
        routeStepPresenter.detachView(routeStepView);

        verify(sectionRepository).getObject(sectionId);
        verify(courseRepository, never()).getObject(any(Long.class));
        verify(unitRepository, never()).getObject(any(Long.class));

        verify(routeStepView, never()).showLoading();
        verify(routeStepView, never()).openNextLesson(any(Unit.class), any(Lesson.class));
        verify(routeStepView, never()).openPreviousLesson(any(Unit.class), any(Lesson.class));
        verify(routeStepView, never()).showCantGoNext();
        verify(routeStepView, never()).showCantGoPrevious();
        verify(routeStepView, never()).showPreviousLessonView();

        verify(routeStepView).showNextLessonView();
    }
}
