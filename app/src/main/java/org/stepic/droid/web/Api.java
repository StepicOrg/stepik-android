package org.stepic.droid.web;

import androidx.fragment.app.FragmentActivity;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.social.ISocialType;
import org.stepic.droid.web.model.adaptive.RatingRestoreResponse;
import org.stepik.android.model.Tag;
import org.stepik.android.model.adaptive.RatingItem;
import org.stepik.android.remote.assignment.model.AssignmentResponse;
import org.stepik.android.remote.auth.model.StepikProfileResponse;
import org.stepik.android.remote.certificate.model.CertificateResponse;
import org.stepik.android.remote.unit.model.UnitResponse;
import org.stepik.android.remote.user.model.UserResponse;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import retrofit2.Call;


public interface Api {

    enum TokenType {
        social, loginPassword
    }

    Call<StepikProfileResponse> getUserProfile();

    Call<UserResponse> getUsers(long[] userIds);

    Single<UserResponse> getUsersRx(long[] userIds);

    /**
     * Max number of  units defined in AppConstants
     */
    Call<UnitResponse> getUnits(List<Long> units);

    Single<UnitResponse> getUnits(long courseId, long lessonId);

    Single<AssignmentResponse> getAssignments(long[] assignmentsIds);

    void loginWithSocial(FragmentActivity activity, ISocialType type);

    Call<SearchResultResponse> getSearchResultsCourses(int page, String rawQuery);

    Single<QueriesResponse> getSearchQueries(String query);

    Call<Void> remindPassword(String email);

    Single<CertificateResponse> getCertificates(long userId, int page);

    Single<CourseCollectionsResponse> getCourseCollections(String language);

    Single<TagResponse> getFeaturedTags();

    Single<SearchResultResponse> getSearchResultsOfTag(int page, @NotNull Tag tag);

    Single<List<RatingItem>> getRating(long courseId, int count, int days);

    Completable putRating(long courseId, long exp);

    Single<RatingRestoreResponse> restoreRating(long courseId);
}
