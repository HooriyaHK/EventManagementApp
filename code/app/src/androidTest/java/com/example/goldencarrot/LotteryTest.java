package com.example.goldencarrot;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.repeatedlyUntil;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.goldencarrot.data.db.EventRepository;
import com.example.goldencarrot.views.EntrantHomeView;
import com.example.goldencarrot.views.OrganizerCreateEvent;
import com.example.goldencarrot.views.OrganizerHomeView;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.junit.runner.RunWith;
@RunWith(AndroidJUnit4.class)
public class LotteryTest {
    private static final String TAG = "OrganizerNotificationTest";
    private static final String EVENT_NAME = "SPIDERMAN PARTY";

    @Rule
    public ActivityScenarioRule<OrganizerHomeView> activityRule =
            new ActivityScenarioRule<>(OrganizerHomeView.class);
    @Before
    public void testEventCreation() throws InterruptedException {
        EventRepository eventRepository = new EventRepository();
        eventRepository.deleteEventByName(EVENT_NAME, new EventRepository.DeleteCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "message");
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "clean up failed, event was not deleted successfully");
            }
        });
        // Launch activity using the scenario
        try (ActivityScenario<OrganizerCreateEvent> scenario = ActivityScenario.launch(OrganizerCreateEvent.class)) {

            // Input event details
            onView(withId(R.id.eventNameEditText)).perform(typeText(EVENT_NAME), closeSoftKeyboard());
            onView(withId(R.id.eventLocationEditText)).perform(clearText());
            onView(withId(R.id.eventLocationEditText)).perform(typeText("New York"), closeSoftKeyboard());
            onView(withId(R.id.eventDetailsEditText)).perform(typeText("This is a sample event."), closeSoftKeyboard());
            onView(withId(R.id.eventDateEditText)).perform(typeText("31-12-2024"), closeSoftKeyboard());

            // Input waitlist limit and toggle geolocation switch
            onView(withId(R.id.waitlistLimitEditText)).perform(typeText("10"), closeSoftKeyboard());

            // Click on the "Create Event" button
            onView(withId(R.id.createEventButton)).perform(click());

            Thread.sleep(4000);

            onView(withText("New Event")).check(matches(isDisplayed()));

            // Verify if the success message or a related UI component is displayed
            //onView(withText("Event created successfully")).check(matches(isDisplayed()));
        }
    }
    @Test
    public void drawFromLotteryTest() throws InterruptedException {
        try (ActivityScenario<OrganizerHomeView> scenario = ActivityScenario.launch(OrganizerHomeView.class)) {
            Thread.sleep(3000);
            // scroll to sample event and view details
            onView(withId(R.id.recycler_view_events)).perform(
                    repeatedlyUntil(swipeUp(), hasDescendant(withText(EVENT_NAME)),
                            10)
            );
            onView(withText(EVENT_NAME)).perform(longClick());
            onView(withText(EVENT_NAME)).check(matches(isDisplayed()));

            onView(withId(R.id.button_SelectLotteryUsers)).perform(click());
            onView(withHint("(e.g type '1' to draw 1 winner!)")).perform(typeText("1"));
            onView(withText("OK")).perform(click());

            // check it it navigates back to organizer home
            onView(withText("Welcome back ")).check(matches(isDisplayed()));
        }
    }

    @After
    public void cleanUpEvents() {
        EventRepository eventRepository = new EventRepository();
        eventRepository.deleteEventByName(EVENT_NAME, new EventRepository.DeleteCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "message");
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "clean up failed, event was not deleted successfully");
            }
        });
    }
}
