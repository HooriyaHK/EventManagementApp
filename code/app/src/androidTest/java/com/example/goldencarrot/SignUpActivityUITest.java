package com.example.goldencarrot;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;


import com.example.goldencarrot.views.BrowseEventsActivity;
import com.example.goldencarrot.views.EntrantHomeView;
import com.example.goldencarrot.views.SignUpActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Important, while running this test please press allow while using the app.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
// PRESS ALLOW WHILE USING THE APP WHEN RUNNING THESE TESTS
public class SignUpActivityUITest {
    /**
     * PRESS ALLOW WHILE USING THE APP WHEN RUNNING THESE TESTS
     */

    @Rule
    public ActivityScenarioRule<SignUpActivity> activityRule = new ActivityScenarioRule<>(SignUpActivity.class);

    /**
     * Test for invalid email format triggering validation error dialog.
     */
    @Test
    public void testInvalidEmailFormat() {
        onView(withId(R.id.sign_up_email_input)).perform(replaceText("invalid-email"));
        onView(withId(R.id.sign_up_phone_number)).perform(replaceText("1234567890"));
        onView(withId(R.id.sign_up_name)).perform(replaceText("John Doe"));
        onView(withId(R.id.sign_up_create_account_button)).perform(click());

        onView(withText("Validation Error")).check(matches(isDisplayed()));
        onView(withText("Invalid email format")).check(matches(isDisplayed()));
        onView(withText("OK")).perform(click());
    }

    /**
     * Test for phone number that does not have exactly 10 digits
     */
    @Test
    public void testInvalidPhoneNumber() {
        onView(withId(R.id.sign_up_email_input)).perform(replaceText("test@example.com"));
        onView(withId(R.id.sign_up_phone_number)).perform(replaceText("12345")); // Invalid phone
        onView(withId(R.id.sign_up_name)).perform(replaceText("John Doe"));
        onView(withId(R.id.sign_up_create_account_button)).perform(click());

        onView(withText("Validation Error")).check(matches(isDisplayed()));
        onView(withText("Phone number must contain exactly 10 digits")).check(matches(isDisplayed()));
        onView(withText("OK")).perform(click());
    }

    /**
     * Test for missing name input.
     */
    @Test
    public void testEmptyName() {
        onView(withId(R.id.sign_up_email_input)).perform(replaceText("test@example.com"));
        onView(withId(R.id.sign_up_phone_number)).perform(replaceText("1234567890"));
        onView(withId(R.id.sign_up_name)).perform(replaceText("")); // Empty name
        onView(withId(R.id.sign_up_create_account_button)).perform(click());

        onView(withText("Validation Error")).check(matches(isDisplayed()));
        onView(withText("Name cannot be empty")).check(matches(isDisplayed()));
        onView(withText("OK")).perform(click());
    }

    /**
     * Test for successful input validation and send intent to Entrant home activity
     */
    @Test
    public void testValidInput() {
        Intents.init();

        onView(withId(R.id.sign_up_email_input)).perform(replaceText("valid@example.com"));
        onView(withId(R.id.sign_up_phone_number)).perform(replaceText("1234567890"));
        onView(withId(R.id.sign_up_name)).perform(replaceText("John Doe"));
        onView(withId(R.id.sign_up_create_account_button)).perform(click());

        intended(hasComponent(EntrantHomeView.class.getName()));

        Intents.release();

    }
}
