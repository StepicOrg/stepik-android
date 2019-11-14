package org.stepic.droid.web;

import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.FragmentActivity;

import org.stepic.droid.configuration.Config;
import org.stepic.droid.di.AppSingleton;
import org.stepic.droid.social.ISocialType;
import org.stepic.droid.util.NetworkExtensionsKt;
import org.stepik.android.remote.auth.service.EmptyAuthService;
import org.stepik.android.remote.base.CookieHelper;
import org.stepik.android.remote.base.NetworkFactory;
import org.stepik.android.remote.base.UserAgentProvider;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URLEncoder;
import java.util.List;

import javax.inject.Inject;

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

    private final Config config;
    private final UserAgentProvider userAgentProvider;

    private final CookieHelper cookieHelper;
    private final Converter.Factory converterFactory;

    @Inject
    public ApiImpl(
            Config config,
            UserAgentProvider userAgentProvider,
            CookieHelper cookieHelper,
            Converter.Factory converterFactory
    ) {
        this.config = config;
        this.userAgentProvider = userAgentProvider;
        this.cookieHelper = cookieHelper;
        this.converterFactory = converterFactory;
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

    private Request addUserAgentTo(Interceptor.Chain chain) {
        return chain
                .request()
                .newBuilder()
                .header(USER_AGENT_NAME, userAgentProvider.provideUserAgent())
                .build();
    }
}
