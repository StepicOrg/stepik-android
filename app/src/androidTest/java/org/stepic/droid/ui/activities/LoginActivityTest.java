package org.stepic.droid.ui.activities;


import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
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
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityTestRule<SocialAuthActivity> mActivityTestRule = new ActivityTestRule<>(SocialAuthActivity.class);

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

//        onView(withId(R.id.drawer))
//                .perform(DrawerActions.open());

        ViewInteraction checkedTextView = onView(
                allOf(withId(R.id.design_menu_item_text),
                        childAtPosition(
                                childAtPosition(withId(R.id.design_navigation_view), 1),
                                0)));
        checkedTextView.check(matches(isDisplayed()));

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
