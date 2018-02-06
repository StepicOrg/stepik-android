package org.stepic.droid.web;

import org.stepic.droid.web.model.adaptive.RatingRequest;
import org.stepic.droid.web.model.adaptive.RatingResponse;
import org.stepic.droid.web.model.adaptive.RatingRestoreResponse;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface RatingService {

    @PUT("rating")
    Completable putRating(
            @Body final RatingRequest ratingRequest
    );

    @GET("rating")
    Observable<RatingResponse> getRating(
            @Query("course") final long courseId,
            @Query("count") final long count,
            @Query("days") final long days,
            @Query("user") final long userId
    );

    @GET("rating-restore")
    Single<RatingRestoreResponse> restoreRating(
            @Query("course") final long courseId,
            @Query("token") final String token
    );

}
