package org.stepic.droid.ui.activities;


import android.content.Context;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.stepic.droid.R;
import org.stepik.android.view.auth.ui.activity.SocialAuthActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {
    private static final String DEVICE_SPECIFIC_PREFERENCES = "device_specific";
    private static final String LOGIN_PREFERENCES = "login preference";
    private static final String IS_PERSONALIZED_ONBOARDING_WAS_SHOWN = "is_personalized_onboarding_was_shown";
    private static final String WAS_STREAK_DIALOG_SEEN_HOME_SCREEN = "was_streak_dialog_seen_home_screen";

    @Rule
    public ActivityTestRule<SocialAuthActivity> mActivityTestRule =
            new ActivityTestRule<>(SocialAuthActivity.class, true, false);

    @Before
    public void setUp() {
        InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext()
                .getSharedPreferences(DEVICE_SPECIFIC_PREFERENCES, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(IS_PERSONALIZED_ONBOARDING_WAS_SHOWN, true)
                .commit();

        InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext()
                .getSharedPreferences(LOGIN_PREFERENCES, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(WAS_STREAK_DIALOG_SEEN_HOME_SCREEN, true)
                .commit();

        mActivityTestRule.launchActivity(null);
    }

    @Test
    public void loginActivityTest() {

        onView(withId(R.id.signInWithEmail))
                .perform(scrollTo(), click());

        onView(withId(R.id.loginField))
                .perform(scrollTo(), replaceText("test@stepik.org"), closeSoftKeyboard());

        onView(allOf(withId(R.id.loginField), withText("test@stepik.org")))
                .perform(pressImeActionButton());

        onView(withId(R.id.passwordField))
                .perform(scrollTo(), replaceText("qwerty123"), closeSoftKeyboard());

        onView(withId(R.id.loginButton))
                .perform(scrollTo(), click());

        onView(allOf(
                withId(R.id.navigationView),
                hasDescendant(withText(R.string.main_tab_home))
        )).check(matches(isDisplayed()));

    }
}
