package org.stepic.droid.util.resolvers;

import org.junit.Before;
import org.junit.Test;
import org.stepik.android.model.structure.SearchResult;
import org.stepic.droid.testUtils.generators.FakeSearchResultGenerator;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SearchResolverImplTest {

    private SearchResolver searchResolver;

    @Before
    public void beforeEachTest() {
        searchResolver = new SearchResolverImpl();
    }

    @Test
    public void getCourseIdsFromSearchResults_null_resultEmptyArray() {
        long[] actual = searchResolver.getCourseIdsFromSearchResults(null);
        assertTrue(actual.length == 0);
    }

    @Test
    public void getCourseIdsFromSearchResults_empty_resultEmptyArray() {
        List<SearchResult> input = new ArrayList<>();

        long[] actual = searchResolver.getCourseIdsFromSearchResults(input);

        assertTrue("input was not save empty state", input.isEmpty());
        assertTrue("result was not empty on empty input", actual.length == 0);
    }

    @Test
    public void getCourseIdsFromSearchResults_nonZeroCourseIds_success() {
        List<SearchResult> input = new ArrayList<>();
        input.add(FakeSearchResultGenerator.INSTANCE.generate(2000));
        input.add(FakeSearchResultGenerator.INSTANCE.generate(3684));
        input.add(FakeSearchResultGenerator.INSTANCE.generate(41));

        long[] actual = searchResolver.getCourseIdsFromSearchResults(input);
        assertEquals("expected number is wrong", 3, actual.length);
        for (int i = 0; i < input.size(); i++) {
            assertEquals("order is wrong", input.get(i).getCourse(), actual[i]);
        }
    }

    @Test
    public void getCourseIdsFromSearchResults_zeroCourseIds_empty() {
        List<SearchResult> input = new ArrayList<>();
        input.add(FakeSearchResultGenerator.INSTANCE.generate(0));
        input.add(FakeSearchResultGenerator.INSTANCE.generate(0));
        input.add(FakeSearchResultGenerator.INSTANCE.generate(0));

        long[] actual = searchResolver.getCourseIdsFromSearchResults(input);
        assertTrue("result was not empty on search results with zero course ids", actual.length == 0);
    }

    @Test
    public void getCourseIdsFromSearchResults_zeroAndNonCourseIds_onlyNonZero() {
        List<SearchResult> input = new ArrayList<>();
        input.add(FakeSearchResultGenerator.INSTANCE.generate(0));
        input.add(FakeSearchResultGenerator.INSTANCE.generate(1));
        input.add(FakeSearchResultGenerator.INSTANCE.generate(3215));
        input.add(FakeSearchResultGenerator.INSTANCE.generate(0));
        input.add(FakeSearchResultGenerator.INSTANCE.generate(666));

        long[] actual = searchResolver.getCourseIdsFromSearchResults(input);

        assertEquals(3, actual.length);
        assertEquals(1, actual[0]);
        assertEquals(3215, actual[1]);
        assertEquals(666, actual[2]);
    }

}
