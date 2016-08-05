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
        google(GOOGLE_SOCIAL_IDENTIFIER, ContextCompat.getDrawable(MainApplication.getAppContext(), R.drawable.ic_new_google_favicon_logo)),
        facebook(FACEBOOK_SOCIAL_IDENTIFIER, ContextCompat.getDrawable(MainApplication.getAppContext(), R.drawable.ic_facebook)),
        mailru(MAILRU_SOCIAL_IDENTIFIER, ContextCompat.getDrawable(MainApplication.getAppContext(), R.drawable.ic_it)),
        twitter(TWITTER_SOCIAL_IDENTIFIER, ContextCompat.getDrawable(MainApplication.getAppContext(), R.drawable.ic_twitter)),
        vk(VK_SOCIAL_IDENTIFIER, ContextCompat.getDrawable(MainApplication.getAppContext(), R.drawable.ic_vk)),
        github(GITHUB_SOCIAL_IDENTIFIER, ContextCompat.getDrawable(MainApplication.getAppContext(), R.drawable.ic_github));


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
    }


    public List<? extends ISocialType> getAllSocial() {
        return Arrays.asList(SocialType.values());
    }
}
