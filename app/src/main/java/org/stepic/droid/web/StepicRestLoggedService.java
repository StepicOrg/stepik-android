package org.stepic.droid.web;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.web.model.adaptive.RecommendationReactionsRequest;
import org.stepic.droid.web.model.adaptive.RecommendationsResponse;
import org.stepic.droid.web.model.story_templates.StoryTemplatesResponse;
import org.stepik.android.model.EnrollmentWrapper;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface StepicRestLoggedService {
    @GET("api/sections")
    Call<SectionsMetaResponse> getSections(@Query("ids[]") long[] sectionIds);

    @GET("api/sections")
    Single<SectionsMetaResponse> getSectionsRx(@Query("ids[]") long[] sectionIds);

    @Headers("Content-Type:application/json")
    @POST("api/enrollments")
    Completable joinCourse(@Body EnrollmentWrapper enrollmentCourse);

    @DELETE("api/enrollments/{id}")
    Completable dropCourse(@Path("id") long courseId);

    @GET("api/users")
    Call<UsersResponse> getUsers(@Query("ids[]") long[] userIds);

    @GET("api/users")
    Single<UsersResponse> getUsersRx(@Query("ids[]") long[] userIds);

    @GET("api/stepics/1")
    Call<StepicProfileResponse> getUserProfile();

    @GET("api/courses?enrolled=true")
    Single<CoursesMetaResponse> getEnrolledCourses(@Query("page") int page);

    @GET("api/user-courses")
    Single<UserCoursesResponse> getUserCourses(@Query("page") int page);

    @GET("api/courses?exclude_ended=true&is_public=true&order=-activity")
    Single<CoursesMetaResponse> getPopularCourses(@Query("page") int page, @Query("language") String language);

    @GET("api/units")
    Call<UnitMetaResponse> getUnits(
            @Query("ids[]") List<Long> units
    );

    @GET("api/units")
    Single<UnitMetaResponse> getUnitsRx(
            @Query("ids[]") long[] units
    );

    @GET("api/units")
    Single<UnitMetaResponse> getUnits(
            @Query("course") final long courseId,
            @Query("lesson") final long lessonId
    );

    @GET("api/lessons")
    Call<LessonStepicResponse> getLessons(@Query("ids[]") long[] lessons);

    @GET("api/lessons")
    Single<LessonStepicResponse> getLessonsRx(@Query("ids[]") long[] lessons);

    @GET("api/steps")
    Call<StepResponse> getSteps(
            @Query("ids[]") long[] steps
    );

    @GET("api/steps")
    Single<StepResponse> getStepsReactive(
            @Query("ids[]") long[] steps
    );

    @GET("api/steps")
    Single<StepResponse> getStepsByLessonId(
            @Query("lesson") long lessonId
    );

    @DELETE("api/enrollments/{id}")
    Call<Void> dropCourseLegacy(@Path("id") long courseId);

    @GET("api/progresses")
    Call<ProgressesResponse> getProgresses(@Query("ids[]") String[] progresses);

    @GET("api/progresses")
    Single<ProgressesResponse> getProgressesReactive(@Query("ids[]") String[] progresses);

    @GET("api/assignments")
    Call<AssignmentResponse> getAssignments(@Query("ids[]") long[] assignmentsIds);


    @Headers("Content-Type:application/json")
    @POST("api/views")
    Call<Void> postViewed(@Body ViewAssignmentWrapper stepAssignment);

    @Headers("Content-Type:application/json")
    @POST("api/views")
    Completable postViewedReactive(@Body ViewAssignmentWrapper stepAssignment);

    @GET("api/search-results?is_popular=true&is_public=true&type=course")
    Call<SearchResultResponse> getSearchResults(
            @Query("page") int page,
            @Query(value = "query", encoded = true) String encodedQuery,
            @Query("language") String lang
    );

    @GET("api/queries")
    Single<QueriesResponse> getSearchQueries(@Query("query") String query);

    @GET("api/courses")
    Call<CoursesMetaResponse> getCourses(@Query("page") int page, @Query("ids[]") long[] courseIds);

    @GET("api/courses")
    Single<CoursesMetaResponse> getCoursesReactive(@Query("page") int page, @Query("ids[]") long[] courseIds);

    @GET("api/courses")
    Single<CoursesMetaResponse> getCoursesReactive(@Query("ids[]") long[] courseIds);

    @POST("api/attempts")
    Call<AttemptResponse> createNewAttempt(@Body AttemptRequest attemptRequest);

    @POST("api/attempts")
    Single<AttemptResponse> createNewAttemptReactive(@Body AttemptRequest attemptRequest);

    @POST("api/submissions")
    Call<SubmissionResponse> createNewSubmission(
            @Body SubmissionRequest submissionRequest
    );

    @POST("api/submissions")
    Completable createNewSubmissionReactive(
            @Body SubmissionRequest submissionRequest
    );

    @GET("api/attempts")
    Call<AttemptResponse> getExistingAttempts(@Query("step") long stepId, @Query("user") long userId);

    @GET("api/attempts")
    Single<AttemptResponse> getExistingAttemptsReactive(@Query("step") long stepId, @Query("user") long userId);

    @GET("api/submissions")
    Call<SubmissionResponse> getExistingSubmissions(
            @Query("attempt") long attemptId,
            @Query("order") String order
    );

    @GET("api/submissions")
    Single<SubmissionResponse> getExistingSubmissionsReactive(
            @Query("attempt") final long attemptId,
            @Query("order") final String order
    );

    @GET("api/email-addresses")
    Call<EmailAddressResponse> getEmailAddresses(@Query("ids[]") long[] ids);

    @GET("api/devices")
    Call<DeviceResponse> getDevices(@Query("user") long userId);

    @GET("api/devices")
    Call<DeviceResponse> getDeviceByRegistrationId(@Query("registration_id") String token);

    @POST("api/devices")
    Call<DeviceResponse> registerDevice(@Body DeviceRequest deviceRequest);

    @PUT("api/devices/{id}")
    Call<DeviceResponse> renewDeviceRegistration(@Path("id") long deviceId, @Body DeviceRequest deviceRequest);

    @GET("api/courses")
    Call<CoursesMetaResponse> getCourses(@Query("ids[]") long[] courseIds);

    @PUT("api/notifications/{id}")
    Call<Void> putNotification(@Path("id") long notificationId, @Body NotificationRequest notificationRequest);

    @PUT("api/notifications/{id}")
    Completable putNotificationReactive(@Path("id") long notificationId, @Body NotificationRequest notificationRequest);

    @DELETE("api/devices/{id}")
    Call<Void> removeDevice(@Path("id") long deviceId);

    @GET("api/discussion-proxies/{id}")
    Call<DiscussionProxyResponse> getDiscussionProxy(@Path("id") String discussionProxyId);

    @GET("api/comments")
    Call<CommentsResponse> getComments(@Query("ids[]") long[] ids);

    @POST("api/comments")
    Call<CommentsResponse> postComment(@Body CommentRequest comment);

    @PUT("api/votes/{id}")
    Call<VoteResponse> postVote(@Path("id") String voteId, @Body VoteRequest voteRequest);

    @DELETE("api/comments/{id}")
    Call<CommentsResponse> deleteComment(@Path("id") long commentId);

    @GET("api/certificates")
    Call<CertificateResponse> getCertificates(@Query("user") long userId);

    @GET("api/units")
    Call<UnitMetaResponse> getUnitByLessonId(@Query("lesson") long lessonId);

    @GET("api/submissions?order=desc")
    Call<SubmissionResponse> getExistingSubmissionsForStep(@Query("step") long stepId);

    @GET("api/notifications")
    Call<NotificationResponse> getNotifications(@Query("page") int page, @Nullable @Query("type") String type);

    @FormUrlEncoded
    @POST("api/notifications/mark-as-read")
    Call<Void> markAsRead(@Nullable @Field(value = "type", encoded = true) String notificationType);

    @GET("api/notification-statuses")
    Single<NotificationStatusesResponse> getNotificationStatuses();

    @GET("api/user-activities/{userId}")
    Call<UserActivityResponse> getUserActivities(@Path("userId") long userId);

    @GET("api/user-activities/{userId}")
    Single<UserActivityResponse> getUserActivitiesReactive(@Path("userId") long userId);

    @GET("api/last-steps/{lastStepId}")
    Call<LastStepResponse> getLastStepResponse(@Path("lastStepId") String lastStepId);

    @GET("api/course-lists?platform=mobile")
    Single<CourseCollectionsResponse> getCourseLists(@Query("language") String language);

    @GET("api/course-review-summaries")
    Single<CourseReviewResponse> getCourseReviews(@Query("ids[]") long[] reviewSummaryIds);

    @GET("api/tags?is_featured=true")
    Single<TagResponse> getFeaturedTags();

    @GET("api/search-results?is_popular=true&is_public=true&type=course")
    Single<SearchResultResponse> getSearchResultsOfTag(@Query("page") int page, @Query("tag") int id, @Query("language") String lang);

    @GET("api/recommendations")
    Single<RecommendationsResponse> getNextRecommendations(
            @Query("course") final long courseId,
            @Query("count") final int count
    );

    @POST("api/recommendation-reactions")
    Completable createRecommendationReaction(
            @Body final RecommendationReactionsRequest reactionsRequest
    );

    @GET("api/story-templates")
    Observable<StoryTemplatesResponse> getStoryTemplate(
            @Query("page") final int page,
            @Query("is_published") final boolean isPublished,
            @Query("language") final String language
    );
}
