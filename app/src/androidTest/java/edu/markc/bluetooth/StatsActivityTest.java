package edu.markc.bluetooth;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)

public class StatsActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(
            MainActivity.class);

    @Test
    public void chartDataTest() {

        ActivityScenario.launch(StatsActivity.class).onActivity(activity -> {

            activity.findViewById(R.id.weekRB).performClick();

            Espresso.onView(ViewMatchers.withId(R.id.lineChartStats));

        });

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Espresso.onView(ViewMatchers.withId(R.id.lineChartStats)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

}
