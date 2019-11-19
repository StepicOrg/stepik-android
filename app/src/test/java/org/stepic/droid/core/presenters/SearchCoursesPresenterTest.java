package org.stepic.droid.core.presenters;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.core.presenters.contracts.CoursesView;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.storage.operations.DatabaseFacade;
import org.stepic.droid.testUtils.ConcurrencyUtilForTest;
import org.stepic.droid.testUtils.generators.FakeCourseGenerator;
import org.stepic.droid.testUtils.generators.FakeMetaGenerator;
import org.stepic.droid.testUtils.generators.FakeSearchResultGenerator;
import org.stepic.droid.util.resolvers.SearchResolver;
import org.stepic.droid.util.resolvers.SearchResolverImpl;
import org.stepik.android.domain.base.DataSourceType;
import org.stepik.android.domain.course.repository.CourseRepository;
import org.stepik.android.domain.search.repository.SearchRepository;
import org.stepik.android.model.Course;
import org.stepik.android.model.Meta;
import org.stepik.android.model.SearchResult;
import org.stepik.android.remote.course.model.CourseResponse;
import org.stepik.android.remote.search.model.SearchResultResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SearchCoursesPresenterTest {

    private SearchCoursesPresenter searchCoursesPresenter;

    @Mock
    SharedPreferenceHelper sharedPreferenceHelper;

    @Mock
    CourseRepository courseRepository;

    @Mock
    SearchRepository searchRepository;

    @Mock
    ThreadPoolExecutor threadPoolExecutor;

    @Mock
    MainHandler mainHandler;

    private SearchResolver searchResolver;

    @Mock
    Analytic analytic;

    @Mock
    CoursesView coursesView;

    @Mock
    DatabaseFacade databaseFacade;


    @Before
    public void beforeEachTest() {
        MockitoAnnotations.initMocks(this);

        ConcurrencyUtilForTest.transformToBlockingMock(threadPoolExecutor);
        ConcurrencyUtilForTest.transformToBlockingMock(mainHandler);

        searchResolver = spy(new SearchResolverImpl());

        searchCoursesPresenter = new SearchCoursesPresenter(
                sharedPreferenceHelper,
                courseRepository,
                searchRepository,
                threadPoolExecutor,
                mainHandler,
                searchResolver,
                databaseFacade,
                analytic
        );
    }

    @Test
    public void downloadData_oneCourse_success() throws IOException {

        searchCoursesPresenter.attachView(coursesView);
        String searchQuery = "One course in answer pls";
        Meta onePageMeta = FakeMetaGenerator.INSTANCE.generate();


        //mock calling api for getting search results
        SearchResultResponse responseMock = mock(SearchResultResponse.class);
        List<SearchResult> searchResults = new ArrayList<>();
        int expectedCourseId = 67;
        SearchResult expectedSingleSearchResult = FakeSearchResultGenerator.INSTANCE.generate(expectedCourseId);
        searchResults.add(expectedSingleSearchResult);
        when(responseMock.getSearchResultList()).thenReturn(searchResults);
        when(responseMock.getMeta()).thenReturn(onePageMeta);
        // ResponseGeneratorKt.useMockInsteadCall(when(searchRemoteDataSource.getSearchResultsCourses(1, searchQuery).blockingGet()), responseMock);


        //mock calling api for getting course
        long[] courseIds = new long[1];
        courseIds[0] = expectedSingleSearchResult.getCourse();
        CourseResponse coursesStepicResponse = mock(CourseResponse.class);
        when(coursesStepicResponse.getMeta()).thenReturn(onePageMeta);
        List<Course> expectedCourses = new ArrayList<>();
        Course expectedCourse = FakeCourseGenerator.INSTANCE.generate(expectedCourseId);
        expectedCourses.add(expectedCourse);
        when(coursesStepicResponse.getCourses()).thenReturn(expectedCourses);
        // ResponseGeneratorKt.useMockInsteadCall(when(courseRepository.getCourses(courseIds, DataSourceType.REMOTE).blockingGet(), coursesStepicResponse);

        //call method of tested object
        searchCoursesPresenter.downloadData(searchQuery);

        //verify calling of dependencies
        verify(searchRepository).getSearchResultsCourses(1, searchQuery, "");
        verify(courseRepository).getCourses(courseIds, DataSourceType.REMOTE);

        verify(threadPoolExecutor).execute(any(Runnable.class));
        verify(searchResolver).getCourseIdsFromSearchResults(any(List.class));

        //verify calls of view methods
        InOrder inOrder = inOrder(coursesView);
        inOrder.verify(coursesView).showLoading();
        inOrder.verify(coursesView).showCourses(expectedCourses);

        //verify never called view's methods
        verify(coursesView, never()).showConnectionProblem();
        verify(coursesView, never()).showEmptyCourses();
    }
}
