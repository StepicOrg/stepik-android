package org.stepic.droid.social;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;

import java.util.Arrays;
import java.util.List;

public class SocialManager {

    private static final String GOOGLE_SOCIAL_IDENTIFIER = "google";
    private static final String FACEBOOK_SOCIAL_IDENTIFIER = "facebook";
    private static final String MAILRU_SOCIAL_IDENTIFIER = "itmailru";
    private static final String TWITTER_SOCIAL_IDENTIFIER = "twitter";
    private static final String GITHUB_SOCIAL_IDENTIFIER = "github";
    private static final String VK_SOCIAL_IDENTIFIER = "vk";

    public enum SocialType implements ISocialType {
        google(GOOGLE_SOCIAL_IDENTIFIER, ContextCompat.getDrawable(MainApplication.getAppContext(), R.drawable.ic_login_social_g)),
        vk(VK_SOCIAL_IDENTIFIER, ContextCompat.getDrawable(MainApplication.getAppContext(), R.drawable.ic_login_social_vk)),
        facebook(FACEBOOK_SOCIAL_IDENTIFIER, ContextCompat.getDrawable(MainApplication.getAppContext(), R.drawable.ic_login_social_fb)),
        twitter(TWITTER_SOCIAL_IDENTIFIER, ContextCompat.getDrawable(MainApplication.getAppContext(), R.drawable.ic_login_social_tw)),
        mailru(MAILRU_SOCIAL_IDENTIFIER, ContextCompat.getDrawable(MainApplication.getAppContext(), R.drawable.ic_login_social_mail)),
        github(GITHUB_SOCIAL_IDENTIFIER, ContextCompat.getDrawable(MainApplication.getAppContext(), R.drawable.ic_login_social_github));


        private String identifier;
        private Drawable icon;

        SocialType(String identifier, Drawable drawable) {
            this.identifier = identifier;
            this.icon = drawable;
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
