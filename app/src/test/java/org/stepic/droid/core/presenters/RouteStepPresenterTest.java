package org.stepic.droid.core.presenters;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.core.routing.contract.RoutingPoster;
import org.stepic.droid.core.presenters.contracts.RouteStepView;
import org.stepik.android.model.structure.Course;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;
import org.stepic.droid.storage.repositories.Repository;
import org.stepic.droid.testUtils.ConcurrencyUtilForTest;
import org.stepic.droid.testUtils.generators.ArrayHelper;
import org.stepic.droid.testUtils.generators.FakeCourseGenerator;
import org.stepic.droid.testUtils.generators.FakeLessonGenerator;
import org.stepic.droid.testUtils.generators.FakeSectionGenerator;
import org.stepic.droid.testUtils.generators.FakeUnitGenerator;
import org.stepic.droid.testUtils.generators.ListHelper;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    @Mock
    private Repository<Lesson> lessonRepository;

    @Mock
    private RoutingPoster routingPoster;

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
                unitRepository,
                lessonRepository,
                routingPoster);
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
        verify(routeStepView, never()).openNextLesson(any(Unit.class), any(Lesson.class), any(Section.class));
        verify(routeStepView, never()).showCantGoNext();
        verify(routeStepView, never()).openPreviousLesson(any(Unit.class), any(Lesson.class), any(Section.class));
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
        verify(routeStepView, never()).openNextLesson(any(Unit.class), any(Lesson.class), any(Section.class));
        verify(routeStepView, never()).showCantGoNext();
        verify(routeStepView, never()).openPreviousLesson(any(Unit.class), any(Lesson.class), any(Section.class));
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
        verify(routeStepView, never()).openNextLesson(any(Unit.class), any(Lesson.class), any(Section.class));
        verify(routeStepView, never()).showCantGoNext();
        verify(routeStepView, never()).openPreviousLesson(any(Unit.class), any(Lesson.class), any(Section.class));
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
        verify(routeStepView, never()).openNextLesson(any(Unit.class), any(Lesson.class), any(Section.class));
        verify(routeStepView, never()).showCantGoNext();
        verify(routeStepView, never()).openPreviousLesson(any(Unit.class), any(Lesson.class), any(Section.class));
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
        verify(routeStepView, never()).openNextLesson(any(Unit.class), any(Lesson.class), any(Section.class));
        verify(routeStepView, never()).showCantGoNext();
        verify(routeStepView, never()).openPreviousLesson(any(Unit.class), any(Lesson.class), any(Section.class));
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
        verify(routeStepView, never()).openNextLesson(any(Unit.class), any(Lesson.class), any(Section.class));
        verify(routeStepView, never()).showCantGoNext();
        verify(routeStepView, never()).openPreviousLesson(any(Unit.class), any(Lesson.class), any(Section.class));
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
        verify(routeStepView, never()).openNextLesson(any(Unit.class), any(Lesson.class), any(Section.class));
        verify(routeStepView, never()).openPreviousLesson(any(Unit.class), any(Lesson.class), any(Section.class));
        verify(routeStepView, never()).showCantGoNext();
        verify(routeStepView, never()).showCantGoPrevious();
        verify(routeStepView, never()).showPreviousLessonView();

        verify(routeStepView).showNextLessonView();
    }

    @Test
    public void clickNextLesson_lastInSection() {
        long stepId = 31;
        long sectionId = 12;
        long unitId = 48;
        long lessonId = 48;
        int unitPosition = 1;
        long courseId = 221;

        // first section
        long stepIds0[] = ArrayHelper.INSTANCE.arrayOf(stepId);
        long unitIds0[] = ArrayHelper.INSTANCE.arrayOf(unitId);

        Lesson lesson0 = FakeLessonGenerator.INSTANCE.generate(stepIds0);
        lesson0.setId(lessonId);
        when(lessonRepository.getObject(lessonId)).thenReturn(lesson0);

        Unit unit0 = FakeUnitGenerator.INSTANCE.generate(unitId, sectionId, unitPosition);
        unit0.setLesson(lesson0.getId());
        when(unitRepository.getObject(unitId)).thenReturn(unit0);

        Section section0 = FakeSectionGenerator.INSTANCE.generate(sectionId, unitIds0, 1, courseId);
        when(sectionRepository.getObject(sectionId)).thenReturn(section0);

        // second section
        long stepIds1[] = ArrayHelper.INSTANCE.arrayOf(stepId + 1);
        long unitIds1[] = ArrayHelper.INSTANCE.arrayOf(unitId + 1);

        Lesson lesson1 = FakeLessonGenerator.INSTANCE.generate(stepIds1);
        lesson1.setId(lessonId + 1);
        when(lessonRepository.getObject(lessonId + 1)).thenReturn(lesson1);

        Unit unit1 = FakeUnitGenerator.INSTANCE.generate(unitId + 1, sectionId + 1, unitPosition);
        unit1.setLesson(lesson1.getId());
        when(unitRepository.getObject(unitId + 1)).thenReturn(unit1);

        Section section1 = FakeSectionGenerator.INSTANCE.generate(sectionId + 1, unitIds1, 2, courseId);
        when(sectionRepository.getObject(sectionId + 1)).thenReturn(section1);

        long sectionIds[] = ArrayHelper.INSTANCE.arrayOf(sectionId, sectionId + 1);
        Course course = FakeCourseGenerator.INSTANCE.generate(courseId, sectionIds);
        course.setEnrollment(1);
        when(courseRepository.getObject(courseId)).thenReturn(course);
        when(sectionRepository.getObjects(ArrayHelper.INSTANCE.arrayOf(sectionId + 1))).thenReturn(Collections.singletonList(section1));

        routeStepPresenter.attachView(routeStepView);
        routeStepPresenter.clickNextLesson(unit0);
        routeStepPresenter.detachView(routeStepView);

        verify(routeStepView, times(1)).showLoading();
        verify(routeStepView, times(1)).openNextLesson(unit1, lesson1, section1);
        verify(routingPoster, times(1)).sectionChanged(section0, section1);
        verify(routeStepView, never()).showCantGoNext();
    }

    @Test
    public void clickPreviousLesson_firstInSection() {
        long stepId = 31;
        long sectionId = 12;
        long unitId = 48;
        long lessonId = 48;
        int unitPosition = 1;
        long courseId = 221;

        // first section
        long stepIds0[] = ArrayHelper.INSTANCE.arrayOf(stepId);
        long unitIds0[] = ArrayHelper.INSTANCE.arrayOf(unitId);

        Lesson lesson0 = FakeLessonGenerator.INSTANCE.generate(stepIds0);
        lesson0.setId(lessonId);
        when(lessonRepository.getObject(lessonId)).thenReturn(lesson0);

        Unit unit0 = FakeUnitGenerator.INSTANCE.generate(unitId, sectionId, unitPosition);
        unit0.setLesson(lesson0.getId());
        when(unitRepository.getObject(unitId)).thenReturn(unit0);

        Section section0 = FakeSectionGenerator.INSTANCE.generate(sectionId, unitIds0, 1, courseId);
        when(sectionRepository.getObject(sectionId)).thenReturn(section0);

        // second section
        long stepIds1[] = ArrayHelper.INSTANCE.arrayOf(stepId + 1);
        long unitIds1[] = ArrayHelper.INSTANCE.arrayOf(unitId + 1);

        Lesson lesson1 = FakeLessonGenerator.INSTANCE.generate(stepIds1);
        lesson1.setId(lessonId + 1);
        when(lessonRepository.getObject(lessonId + 1)).thenReturn(lesson1);

        Unit unit1 = FakeUnitGenerator.INSTANCE.generate(unitId + 1, sectionId + 1, unitPosition);
        unit1.setLesson(lesson1.getId());
        when(unitRepository.getObject(unitId + 1)).thenReturn(unit1);

        Section section1 = FakeSectionGenerator.INSTANCE.generate(sectionId + 1, unitIds1, 2, courseId);
        when(sectionRepository.getObject(sectionId + 1)).thenReturn(section1);

        long sectionIds[] = ArrayHelper.INSTANCE.arrayOf(sectionId, sectionId + 1);
        Course course = FakeCourseGenerator.INSTANCE.generate(courseId, sectionIds);
        course.setEnrollment(1);
        when(courseRepository.getObject(courseId)).thenReturn(course);
        when(sectionRepository.getObjects(ArrayHelper.INSTANCE.arrayOf(sectionId))).thenReturn(Collections.singletonList(section0));

        routeStepPresenter.attachView(routeStepView);
        routeStepPresenter.clickPreviousLesson(unit1);
        routeStepPresenter.detachView(routeStepView);

        verify(routeStepView, times(1)).showLoading();
        verify(routeStepView, times(1)).openPreviousLesson(unit0, lesson0, section0);
        verify(routingPoster, times(1)).sectionChanged(section1, section0);
        verify(routeStepView, never()).showCantGoPrevious();
    }
}
