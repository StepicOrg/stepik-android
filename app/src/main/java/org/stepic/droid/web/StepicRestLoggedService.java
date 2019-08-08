package org.stepic.droid.web;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.web.model.adaptive.RecommendationReactionsRequest;
import org.stepic.droid.web.model.adaptive.RecommendationsResponse;
import org.stepic.droid.web.model.story_templates.StoryTemplatesResponse;
import org.stepik.android.remote.assignment.model.AssignmentResponse;
import org.stepik.android.remote.attempt.model.AttemptRequest;
import org.stepik.android.remote.attempt.model.AttemptResponse;
import org.stepik.android.remote.comment.model.CommentRequest;
import org.stepik.android.remote.comment.model.CommentResponse;
import org.stepik.android.remote.course.model.CourseResponse;
import org.stepik.android.remote.course.model.CourseReviewSummaryResponse;
import org.stepik.android.remote.course.model.EnrollmentRequest;
import org.stepik.android.remote.course_payments.model.CoursePaymentRequest;
import org.stepik.android.remote.course_payments.model.CoursePaymentsResponse;
import org.stepik.android.remote.course_reviews.model.CourseReviewRequest;
import org.stepik.android.remote.course_reviews.model.CourseReviewsResponse;
import org.stepik.android.remote.discussion_proxy.model.DiscussionProxyResponse;
import org.stepik.android.remote.email_address.model.EmailAddressResponse;
import org.stepik.android.remote.last_step.model.LastStepResponse;
import org.stepik.android.remote.lesson.model.LessonResponse;
import org.stepik.android.remote.progress.model.ProgressResponse;
import org.stepik.android.remote.section.model.SectionResponse;
import org.stepik.android.remote.step.model.StepResponse;
import org.stepik.android.remote.submission.model.SubmissionRequest;
import org.stepik.android.remote.submission.model.SubmissionResponse;
import org.stepik.android.remote.unit.model.UnitResponse;
import org.stepik.android.remote.user.model.UserResponse;
import org.stepik.android.remote.user_activity.model.UserActivityResponse;
import org.stepik.android.remote.view_assignment.model.ViewAssignmentRequest;

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
    Call<SectionResponse> getSections(@Query("ids[]") long[] sectionIds);

    @GET("api/sections")
    Single<SectionResponse> getSectionsRx(@Query("ids[]") long[] sectionIds);

    @POST("api/enrollments")
    Completable joinCourse(@Body EnrollmentRequest enrollmentCourse);

    @DELETE("api/enrollments/{id}")
    Completable dropCourse(@Path("id") long courseId);

    @GET("api/users")
    Call<UserResponse> getUsers(@Query("ids[]") long[] userIds);

    @GET("api/users")
    Single<UserResponse> getUsersRx(@Query("ids[]") long[] userIds);

    @GET("api/stepics/1")
    Call<StepicProfileResponse> getUserProfile();

    @GET("api/courses?enrolled=true")
    Single<CourseResponse> getEnrolledCourses(@Query("page") int page);

    @GET("api/user-courses")
    Single<UserCoursesResponse> getUserCourses(@Query("page") int page);

    @GET("api/courses?exclude_ended=true&is_public=true&order=-activity")
    Single<CourseResponse> getPopularCourses(@Query("page") int page, @Query("language") String language);

    @GET("api/units")
    Call<UnitResponse> getUnits(
            @Query("ids[]") List<Long> units
    );

    @GET("api/units")
    Single<UnitResponse> getUnitsRx(
            @Query("ids[]") long[] units
    );

    @GET("api/units")
    Single<UnitResponse> getUnits(
            @Query("course") final long courseId,
            @Query("lesson") final long lessonId
    );

    @GET("api/lessons")
    Call<LessonResponse> getLessons(@Query("ids[]") long[] lessons);

    @GET("api/lessons")
    Single<LessonResponse> getLessonsRx(@Query("ids[]") long[] lessons);

    @GET("api/steps")
    Single<StepResponse> getSteps(
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
    Call<ProgressResponse> getProgresses(@Query("ids[]") String[] progresses);

    @GET("api/progresses")
    Single<ProgressResponse> getProgressesReactive(@Query("ids[]") String[] progresses);

    @GET("api/assignments")
    Single<AssignmentResponse> getAssignments(@Query("ids[]") long[] assignmentsIds);


    @Headers("Content-Type:application/json")
    @POST("api/views")
    Completable postViewedReactive(@Body ViewAssignmentRequest stepAssignment);

    @GET("api/search-results?is_popular=true&is_public=true&type=course")
    Call<SearchResultResponse> getSearchResults(
            @Query("page") int page,
            @Query(value = "query", encoded = true) String encodedQuery,
            @Query("language") String lang
    );

    @GET("api/queries")
    Single<QueriesResponse> getSearchQueries(@Query("query") String query);

    @GET("api/courses")
    Call<CourseResponse> getCourses(@Query("page") int page, @Query("ids[]") long[] courseIds);

    @GET("api/courses")
    Single<CourseResponse> getCoursesReactive(@Query("page") int page, @Query("ids[]") long[] courseIds);

    @GET("api/courses")
    Single<CourseResponse> getCoursesReactive(@Query("ids[]") long[] courseIds);

    @POST("api/attempts")
    Call<AttemptResponse> createNewAttempt(@Body AttemptRequest attemptRequest);

    @POST("api/attempts")
    Single<AttemptResponse> createNewAttemptReactive(@Body AttemptRequest attemptRequest);

    @POST("api/submissions")
    Call<SubmissionResponse> createNewSubmission(
            @Body SubmissionRequest submissionRequest
    );

    @POST("api/submissions")
    Single<SubmissionResponse> createNewSubmissionReactive(
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
    Call<CourseResponse> getCourses(@Query("ids[]") long[] courseIds);

    @PUT("api/notifications/{id}")
    Call<Void> putNotification(@Path("id") long notificationId, @Body NotificationRequest notificationRequest);

    @PUT("api/notifications/{id}")
    Completable putNotificationReactive(@Path("id") long notificationId, @Body NotificationRequest notificationRequest);

    @DELETE("api/devices/{id}")
    Call<Void> removeDevice(@Path("id") long deviceId);

    @GET("api/discussion-proxies")
    Single<DiscussionProxyResponse> getDiscussionProxies(@Query("ids[]") String[] ids);

    @GET("api/comments")
    Call<CommentResponse> getComments(@Query("ids[]") long[] ids);

    @GET("api/comments")
    Single<CommentResponse> getCommentsReactive(@Query("ids[]") long[] ids);

    @POST("api/comments")
    Call<CommentResponse> postComment(@Body CommentRequest comment);

    @PUT("api/votes/{id}")
    Call<VoteResponse> postVote(@Path("id") String voteId, @Body VoteRequest voteRequest);

    @DELETE("api/comments/{id}")
    Call<CommentResponse> deleteComment(@Path("id") long commentId);

    @GET("api/certificates")
    Call<CertificateResponse> getCertificates(@Query("user") long userId);

    @GET("api/certificates")
    Single<CertificateResponse> getCertificatesReactive(@Query("user") long userId);

    @GET("api/units")
    Single<UnitResponse> getUnitsByLessonId(@Query("lesson") long lessonId);

    @GET("api/submissions?order=desc")
    Call<SubmissionResponse> getExistingSubmissionsForStep(@Query("step") long stepId);

    @GET("api/submissions?order=desc")
    Single<SubmissionResponse> getExistingSubmissionsForStepReactive(@Query("step") long stepId);

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
    Single<LastStepResponse> getLastStepResponse(@Path("lastStepId") String lastStepId);

    @GET("api/course-lists?platform=mobile")
    Single<CourseCollectionsResponse> getCourseLists(@Query("language") String language);

    @GET("api/course-review-summaries")
    Single<CourseReviewSummaryResponse> getCourseReviews(@Query("ids[]") long[] reviewSummaryIds);

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

    @POST("api/course-payments")
    Single<CoursePaymentsResponse> createCoursePayment(
            @Body final CoursePaymentRequest coursePaymentRequest
    );

    @GET("api/course-payments?order=-id")
    Single<CoursePaymentsResponse> getCoursePaymentsByCourseId(
            @Query("course") final long course
    );

    @GET("api/course-reviews")
    Single<CourseReviewsResponse> getCourseReviewsByCourseId(
            @Query("course") final long course,
            @Query("page") final int page
    );

    @GET("api/course-reviews")
    Single<CourseReviewsResponse> getCourseReviewByCourseIdAndUserId(
            @Query("course") final long course,
            @Query("user") final long user
    );

    @POST("api/course-reviews")
    Single<CourseReviewsResponse> createCourseReview(
            @Body final CourseReviewRequest request
    );

    @PUT("api/course-reviews/{courseReviewId}")
    Single<CourseReviewsResponse> updateCourseReview(
            @Path("courseReviewId") final long courseReviewId,
            @Body final CourseReviewRequest request
    );

    @DELETE("api/course-reviews/{courseReviewId}")
    Completable removeCourseReview(
            @Path("courseReviewId") final long courseReviewId
    );
}
