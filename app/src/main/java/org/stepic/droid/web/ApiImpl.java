package org.stepic.droid.web;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.CookieHelper;
import org.stepic.droid.configuration.Config;
import org.stepic.droid.di.AppSingleton;
import org.stepic.droid.model.NotificationCategory;
import org.stepic.droid.model.StepikFilter;
import org.stepic.droid.notifications.model.Notification;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.social.ISocialType;
import org.stepic.droid.util.DeviceInfoUtil;
import org.stepic.droid.util.NetworkExtensionsKt;
import org.stepic.droid.web.model.adaptive.RatingRequest;
import org.stepic.droid.web.model.adaptive.RatingResponse;
import org.stepic.droid.web.model.adaptive.RatingRestoreResponse;
import org.stepic.droid.web.model.adaptive.RecommendationReactionsRequest;
import org.stepic.droid.web.model.adaptive.RecommendationsResponse;
import org.stepik.android.model.Tag;
import org.stepik.android.model.adaptive.RatingItem;
import org.stepik.android.model.adaptive.RecommendationReaction;
import org.stepik.android.model.user.Profile;
import org.stepik.android.remote.assignment.model.AssignmentResponse;
import org.stepik.android.remote.attempt.model.AttemptRequest;
import org.stepik.android.remote.attempt.model.AttemptResponse;
import org.stepik.android.remote.auth.model.OAuthResponse;
import org.stepik.android.remote.auth.model.StepikProfileResponse;
import org.stepik.android.remote.auth.service.EmptyAuthService;
import org.stepik.android.remote.certificate.model.CertificateResponse;
import org.stepik.android.remote.course.model.CourseResponse;
import org.stepik.android.remote.course.model.CourseReviewSummaryResponse;
import org.stepik.android.remote.course.model.EnrollmentRequest;
import org.stepik.android.remote.email_address.model.EmailAddressResponse;
import org.stepik.android.remote.last_step.model.LastStepResponse;
import org.stepik.android.remote.progress.model.ProgressResponse;
import org.stepik.android.remote.step.model.StepResponse;
import org.stepik.android.remote.unit.model.UnitResponse;
import org.stepik.android.remote.user.model.UserResponse;
import org.stepik.android.remote.user_activity.model.UserActivityResponse;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URLEncoder;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Retrofit;

@AppSingleton
public class ApiImpl implements Api {
    private final long TIMEOUT_IN_SECONDS = 60L;
    private static final String USER_AGENT_NAME = "User-Agent";

    private final Context context;
    private final SharedPreferenceHelper sharedPreference;
    private final Config config;
    private final Analytic analytic;
    private final UserAgentProvider userAgentProvider;
    private final StepicRestLoggedService loggedService;
    private final RatingService ratingService;

    private final CookieHelper cookieHelper;
    private final Converter.Factory converterFactory;

    @Inject
    public ApiImpl(
            Context context, SharedPreferenceHelper sharedPreference,
            Config config,
            Analytic analytic,
            UserAgentProvider userAgentProvider,
            CookieHelper cookieHelper,
            StepicRestLoggedService stepicRestLoggedService,
            RatingService ratingService,
            Converter.Factory converterFactory
    ) {
        this.context = context;
        this.sharedPreference = sharedPreference;
        this.config = config;
        this.analytic = analytic;
        this.userAgentProvider = userAgentProvider;
        this.cookieHelper = cookieHelper;
        this.loggedService = stepicRestLoggedService;
        this.ratingService = ratingService;
        this.converterFactory = converterFactory;
    }
    public Single<UserCoursesResponse> getUserCourses(int page) {
        return loggedService.getUserCourses(page);
    }

    public Single<CourseResponse> getPopularCourses(int page) {
        EnumSet<StepikFilter> enumSet = sharedPreference.getFilterForFeatured();
        String lang = enumSet.iterator().next().getLanguage();
        return loggedService.getPopularCourses(page, lang);
    }

    @Override
    public Call<StepikProfileResponse> getUserProfile() {
        return loggedService.getUserProfile();
    }

    @Override
    public Call<UserResponse> getUsers(long[] userIds) {
        return loggedService.getUsers(userIds);
    }

    @Override
    public Single<UserResponse> getUsersRx(long[] userIds) {
        return loggedService.getUsersRx(userIds);
    }

    @Override
    public Completable joinCourse(EnrollmentRequest enrollmentRequest) {
        return loggedService.joinCourse(enrollmentRequest);
    }

    @Override
    public Call<UnitResponse> getUnits(List<Long> units) {
        return loggedService.getUnits(units);
    }

    @Override
    public Single<UnitResponse> getUnits(long courseId, long lessonId) {
        return loggedService.getUnits(courseId, lessonId);
    }
    @Override
    public Single<StepResponse> getSteps(long[] steps) {
        return loggedService.getSteps(steps);
    }

    @Override
    public Single<StepResponse> getStepsByLessonId(long lessonId) {
        return loggedService.getStepsByLessonId(lessonId);
    }

    @Override
    public Completable dropCourse(long courseId) {
        if (!config.isUserCanDropCourse()) return null;
        return loggedService.dropCourse(courseId);
    }

    @Override
    public Call<ProgressResponse> getProgresses(String[] progresses) {
        return loggedService.getProgresses(progresses);
    }

    @Override
    public Single<ProgressResponse> getProgressesReactive(String[] progresses) {
        return loggedService.getProgressesReactive(progresses);
    }

    @Override
    public Single<AssignmentResponse> getAssignments(long[] assignmentsIds) {
        return loggedService.getAssignments(assignmentsIds);
    }

    @Override
    public void loginWithSocial(final FragmentActivity activity, ISocialType type) {
        String socialIdentifier = type.getIdentifier();
        String url = config.getBaseUrl() + "/accounts/" + socialIdentifier + "/login?next=/oauth2/authorize/?" + Uri.encode("client_id=" + config.getOAuthClientId(TokenType.social) + "&response_type=code");
        Uri uri = Uri.parse(url);
        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
        activity.startActivity(intent);
    }

    @Override
    public Call<SearchResultResponse> getSearchResultsCourses(int page, String rawQuery) {
        EnumSet<StepikFilter> enumSet = sharedPreference.getFilterForFeatured();
        String lang = enumSet.iterator().next().getLanguage();
        String encodedQuery = URLEncoder.encode(rawQuery);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, rawQuery);
        analytic.reportEvent(FirebaseAnalytics.Event.SEARCH, bundle);

        return loggedService.getSearchResults(page, encodedQuery, lang);
    }

    @Override
    public Single<QueriesResponse> getSearchQueries(String query) {
        return loggedService.getSearchQueries(query);
    }

    @Override
    public Call<CourseResponse> getCourses(int page, @Nullable long[] ids) {
        if (ids == null || ids.length == 0) {
            ids = new long[]{0};
        }
        return loggedService.getCourses(page, ids);
    }

    @Override
    public Single<CourseResponse> getCoursesReactive(int page, @NotNull long[] ids) {
        if (ids.length == 0) {
            ids = new long[]{0};
        }
        return loggedService.getCoursesReactive(page, ids);
    }

    @Override
    public Single<CourseResponse> getCoursesReactive(@NotNull long[] ids) {
        return loggedService.getCoursesReactive(ids);
    }

    @Override
    public Single<AttemptResponse> createNewAttemptReactive(long stepId) {
        return loggedService.createNewAttemptReactive(new AttemptRequest(stepId));
    }

    @Override
    public Single<AttemptResponse> getExistingAttemptsReactive(long stepId) {
        return loggedService.getExistingAttemptsReactive(stepId, getCurrentUserId());
    }

    @Override
    public Single<AttemptResponse> getExistingAttemptsReactive(long[] attemptIds) {
        return loggedService.getExistingAttemptsReactive(attemptIds);
    }

    @Override
    public Call<Void> remindPassword(String email) {
        String encodedEmail = URLEncoder.encode(email);

        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request newRequest = addUserAgentTo(chain);

                List<HttpCookie> cookies = cookieHelper.getCookiesForBaseUrl();
                if (cookies == null)
                    return chain.proceed(newRequest);

                String csrftoken = null;
                String sessionId = null;
                for (HttpCookie item : cookies) {
                    if (item.getName() != null && item.getName().equals(config.getCsrfTokenCookieName())) {
                        csrftoken = item.getValue();
                        continue;
                    }
                    if (item.getName() != null && item.getName().equals(config.getSessionCookieName())) {
                        sessionId = item.getValue();
                    }
                }

                String cookieResult = config.getCsrfTokenCookieName() + "=" + csrftoken + "; " + config.getSessionCookieName() + "=" + sessionId;
                if (csrftoken == null) return chain.proceed(newRequest);
                HttpUrl url = newRequest
                        .url()
                        .newBuilder()
                        .addQueryParameter("csrfmiddlewaretoken", csrftoken)
                        .addQueryParameter("csrfmiddlewaretoken", csrftoken)
                        .build();
                newRequest = newRequest.newBuilder()
                        .addHeader("referer", config.getBaseUrl())
                        .addHeader("X-CSRFToken", csrftoken)
                        .addHeader("Cookie", cookieResult)
                        .url(url)
                        .build();
                return chain.proceed(newRequest);
            }
        };
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
        okHttpBuilder.addNetworkInterceptor(interceptor);
        NetworkExtensionsKt.setTimeoutsInSeconds(okHttpBuilder, TIMEOUT_IN_SECONDS);
        Retrofit notLogged = NetworkFactory.createRetrofit(config.getBaseUrl(), okHttpBuilder.build(), converterFactory);
        EmptyAuthService tempService = notLogged.create(EmptyAuthService.class);
        return tempService.remindPassword(encodedEmail);
    }

    @Override
    public Call<EmailAddressResponse> getEmailAddresses(@NotNull long[] ids) {
        return loggedService.getEmailAddresses(ids);
    }

    @Override
    public Call<DeviceResponse> getDevicesByRegistrationId(String token) {
        return loggedService.getDeviceByRegistrationId(token);
    }

    @Override
    public Call<DeviceResponse> renewDeviceRegistration(long deviceId, String token) {
        String description = DeviceInfoUtil.getShortInfo(context);
        DeviceRequest deviceRequest = new DeviceRequest(deviceId, token, description);
        return loggedService.renewDeviceRegistration(deviceId, deviceRequest);
    }

    @Override
    public Call<DeviceResponse> registerDevice(String token) {
        String description = DeviceInfoUtil.getShortInfo(context);
        DeviceRequest deviceRequest = new DeviceRequest(token, description);
        return loggedService.registerDevice(deviceRequest);
    }

    @Override
    public Call<CourseResponse> getCourse(long id) {
        long[] ids = new long[]{id};
        return loggedService.getCourses(ids);
    }

    @Override
    public Call<Void> setReadStatusForNotification(long notificationId, boolean isRead) {
        Notification notification = new Notification();
        notification.setUnread(!isRead);
        return loggedService.putNotification(notificationId, new NotificationRequest(notification));
    }

    @Override
    public Completable setReadStatusForNotificationReactive(long notificationId, boolean isRead) {
        Notification notification = new Notification();
        notification.setUnread(!isRead);
        return loggedService.putNotificationReactive(notificationId, new NotificationRequest(notification));
    }

    @Override
    public Single<CertificateResponse> getCertificates(long userId, int page) {
        return loggedService.getCertificates(userId, page);
    }

    @Override
    public Call<NotificationResponse> getNotifications(NotificationCategory notificationCategory, int page) {
        String categoryType = getNotificationCategoryString(notificationCategory);
        return loggedService.getNotifications(page, categoryType);
    }

    @Override
    public Call<Void> markAsReadAllType(@NotNull NotificationCategory notificationCategory) {
        String categoryType = getNotificationCategoryString(notificationCategory);
        return loggedService.markAsRead(categoryType);
    }

    @Override
    public Single<NotificationStatusesResponse> getNotificationStatuses() {
        return loggedService.getNotificationStatuses();
    }

    @Override
    public Call<UserActivityResponse> getUserActivities(long userId) {
        return loggedService.getUserActivities(userId);
    }

    @Override
    public Single<UserActivityResponse> getUserActivitiesReactive(long userId) {
        return loggedService.getUserActivitiesReactive(userId);
    }

    @Override
    public Single<LastStepResponse> getLastStepResponse(@NonNull String lastStepId) {
        return loggedService.getLastStepResponse(lastStepId);
    }

    @Override
    public Single<CourseCollectionsResponse> getCourseCollections(String language) {
        return loggedService.getCourseLists(language);
    }

    @Override
    public Single<CourseReviewSummaryResponse> getCourseReviewSummaries(long[] courseIds) {
        return loggedService.getCourseReviews(courseIds);
    }

    @Override
    public Single<TagResponse> getFeaturedTags() {
        return loggedService.getFeaturedTags();
    }

    @Override
    public Single<RecommendationsResponse> getNextRecommendations(long courseId, int count) {
        return loggedService.getNextRecommendations(courseId, count);
    }

    @Override
    public Completable createReaction(RecommendationReaction reaction) {
        return loggedService.createRecommendationReaction(new RecommendationReactionsRequest(reaction));
    }

    @Override
    public Single<List<RatingItem>> getRating(long courseId, int count, int days) {
        return ratingService.getRating(courseId, count, days, getCurrentUserId()).map(new Function<RatingResponse, List<RatingItem>>() {
            @Override
            public List<RatingItem> apply(RatingResponse ratingResponse) {
                return ratingResponse.getUsers();
            }
        });
    }

    @Override
    public Completable putRating(long courseId, long exp) {
        return ratingService.putRating(new RatingRequest(exp, courseId, getAccessToken()));
    }

    @Override
    public Single<RatingRestoreResponse> restoreRating(long courseId) {
        return ratingService.restoreRating(courseId, getAccessToken());
    }

    @Override
    public Single<SearchResultResponse> getSearchResultsOfTag(int page, @NotNull Tag tag) {
        EnumSet<StepikFilter> enumSet = sharedPreference.getFilterForFeatured();
        String lang = enumSet.iterator().next().getLanguage();
        return loggedService.getSearchResultsOfTag(page, tag.getId(), lang);
    }


    @Nullable
    private String getNotificationCategoryString(NotificationCategory notificationCategory) {
        String categoryType;
        if (notificationCategory == NotificationCategory.all) {
            categoryType = null;
        } else {
            categoryType = notificationCategory.name();
        }
        return categoryType;
    }

    private Request addUserAgentTo(Interceptor.Chain chain) {
        return chain
                .request()
                .newBuilder()
                .header(USER_AGENT_NAME, userAgentProvider.provideUserAgent())
                .build();
    }

    private long getCurrentUserId() {
        Profile profile = sharedPreference.getProfile();
        //noinspection StatementWithEmptyBody
        if (profile == null) {
            //practically it is not happens (yandex metrica)
            return 0;
        } else {
            return profile.getId();
        }
    }

    @Nullable
    private String getAccessToken() {
        final OAuthResponse auth = sharedPreference.getAuthResponseFromStore();
        if (auth == null) {
            return null;
        } else {
            return auth.getAccessToken();
        }
    }
}
