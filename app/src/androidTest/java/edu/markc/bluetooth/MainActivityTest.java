package edu.markc.bluetooth;

import android.util.Log;
import android.view.View;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import junit.framework.TestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.espresso.Espresso;
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> mMainActivityRule = new ActivityScenarioRule<>(
            MainActivity.class);



    @Test
    public void ReadingsPresent() {
        Espresso.onView(ViewMatchers.withId(R.id.tVSpeed)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.tVRPM)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.imageView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        mMainActivityRule.getScenario().close();

    }

    @Test
    public void fuelGaugeTest()
    {
        Espresso.onView(ViewMatchers.withId(R.id.btnEmuStopReading)).perform(ViewActions.click());
       assert ( MainActivity.stop == true);
    }


}