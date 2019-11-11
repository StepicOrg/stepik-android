package org.stepic.droid.web;

import androidx.fragment.app.FragmentActivity;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.social.ISocialType;
import org.stepic.droid.web.model.adaptive.RatingRestoreResponse;
import org.stepik.android.model.Tag;
import org.stepik.android.model.adaptive.RatingItem;
import org.stepik.android.remote.unit.model.UnitResponse;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import retrofit2.Call;


public interface Api {

    enum TokenType {
        social, loginPassword
    }

    /**
     * Max number of  units defined in AppConstants
     */
    Call<UnitResponse> getUnits(List<Long> units);

    Single<UnitResponse> getUnits(long courseId, long lessonId);

    void loginWithSocial(FragmentActivity activity, ISocialType type);

    Call<SearchResultResponse> getSearchResultsCourses(int page, String rawQuery);

    Single<QueriesResponse> getSearchQueries(String query);

    Call<Void> remindPassword(String email);

    Single<CourseCollectionsResponse> getCourseCollections(String language);

    Single<TagResponse> getFeaturedTags();

    Single<SearchResultResponse> getSearchResultsOfTag(int page, @NotNull Tag tag);

    Single<List<RatingItem>> getRating(long courseId, int count, int days);

    Completable putRating(long courseId, long exp);

    Single<RatingRestoreResponse> restoreRating(long courseId);
}
