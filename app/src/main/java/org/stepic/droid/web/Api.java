package org.stepic.droid.web;

import androidx.fragment.app.FragmentActivity;

import org.stepic.droid.social.ISocialType;
import org.stepic.droid.web.model.adaptive.RatingRestoreResponse;
import org.stepik.android.model.adaptive.RatingItem;

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

    void loginWithSocial(FragmentActivity activity, ISocialType type);

    Call<Void> remindPassword(String email);

    Single<List<RatingItem>> getRating(long courseId, int count, int days);

    Completable putRating(long courseId, long exp);

    Single<RatingRestoreResponse> restoreRating(long courseId);
}
