package org.stepic.droid.web;

import org.stepik.android.remote.unit.model.UnitResponse;

import java.util.List;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StepicRestLoggedService {

    @GET("api/units")
    Call<UnitResponse> getUnits(
            @Query("ids[]") List<Long> units
    );

    @GET("api/units")
    Single<UnitResponse> getUnits(
            @Query("course") final long courseId,
            @Query("lesson") final long lessonId
    );

    @GET("api/course-lists?platform=mobile")
    Single<CourseCollectionsResponse> getCourseLists(@Query("language") String language);

    @GET("api/tags?is_featured=true")
    Single<TagResponse> getFeaturedTags();

    @GET("api/search-results?is_popular=true&is_public=true&type=course")
    Single<SearchResultResponse> getSearchResultsOfTag(@Query("page") int page, @Query("tag") int id, @Query("language") String lang);


}
