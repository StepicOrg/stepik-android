package org.stepic.droid.social;

import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import org.stepic.droid.R;
import org.stepic.droid.base.App;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class SocialManager {

    @Inject
    public SocialManager (){}

    private static final String GOOGLE_SOCIAL_IDENTIFIER = "google";
    private static final String FACEBOOK_SOCIAL_IDENTIFIER = "facebook";
    private static final String TWITTER_SOCIAL_IDENTIFIER = "twitter";
    private static final String GITHUB_SOCIAL_IDENTIFIER = "github";
    private static final String VK_SOCIAL_IDENTIFIER = "vk";

    public enum SocialType implements ISocialType {
        google(GOOGLE_SOCIAL_IDENTIFIER, R.drawable.ic_login_social_google),
        vk(VK_SOCIAL_IDENTIFIER, R.drawable.ic_login_social_vk),
        facebook(FACEBOOK_SOCIAL_IDENTIFIER, R.drawable.ic_login_social_fb),
        twitter(TWITTER_SOCIAL_IDENTIFIER, R.drawable.ic_login_social_twitter),
        github(GITHUB_SOCIAL_IDENTIFIER, R.drawable.ic_login_social_github);

        private static Drawable getSocialDrawable(@DrawableRes int drawableRes) {
            return VectorDrawableCompat.create(App.Companion.getAppContext().getResources(), drawableRes, null);
        }

        private String identifier;
        private Drawable icon;

        SocialType(String identifier,@DrawableRes int drawableRes) {
            this.identifier = identifier;
            this.icon = getSocialDrawable(drawableRes);
        }

        public String getIdentifier() {
            return identifier;
        }

        public Drawable getIcon() {
            return icon;
        }

        @Override
        public boolean needUseAccessTokenInsteadOfCode() {
            return identifier.equals(VK_SOCIAL_IDENTIFIER) || identifier.equals(FACEBOOK_SOCIAL_IDENTIFIER);
        }
    }


    public List<? extends ISocialType> getAllSocial() {
        return Arrays.asList(SocialType.values());
    }
}
