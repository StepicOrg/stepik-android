package org.stepic.droid.web;

import org.stepik.android.remote.auth.model.StepikProfileResponse;
import org.stepik.android.remote.unit.model.UnitResponse;
import org.stepik.android.remote.user.model.UserResponse;
import org.stepik.android.remote.vote.model.VoteRequest;
import org.stepik.android.remote.vote.model.VoteResponse;

import java.util.List;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface StepicRestLoggedService {
    @GET("api/users")
    Call<UserResponse> getUsers(@Query("ids[]") long[] userIds);

    @GET("api/users")
    Single<UserResponse> getUsersRx(@Query("ids[]") long[] userIds);

    @GET("api/stepics/1")
    Call<StepikProfileResponse> getUserProfile();

    @GET("api/units")
    Call<UnitResponse> getUnits(
            @Query("ids[]") List<Long> units
    );

    @GET("api/units")
    Single<UnitResponse> getUnits(
            @Query("course") final long courseId,
            @Query("lesson") final long lessonId
    );

    @GET("api/search-results?is_popular=true&is_public=true&type=course")
    Call<SearchResultResponse> getSearchResults(
            @Query("page") int page,
            @Query(value = "query", encoded = true) String encodedQuery,
            @Query("language") String lang
    );

    @GET("api/queries")
    Single<QueriesResponse> getSearchQueries(@Query("query") String query);

    @PUT("api/votes/{id}")
    Single<VoteResponse> saveVote(@Path("id") String voteId, @Body VoteRequest voteRequest);

    @GET("api/course-lists?platform=mobile")
    Single<CourseCollectionsResponse> getCourseLists(@Query("language") String language);

    @GET("api/tags?is_featured=true")
    Single<TagResponse> getFeaturedTags();

    @GET("api/search-results?is_popular=true&is_public=true&type=course")
    Single<SearchResultResponse> getSearchResultsOfTag(@Query("page") int page, @Query("tag") int id, @Query("language") String lang);


}
