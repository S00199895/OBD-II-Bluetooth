package edu.markc.bluetooth;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FuelActivityTest {

/*
    @Rule
    public ActivityScenarioRule<MainActivity> jobsRule = new ActivityScenarioRule<>(
            MainActivity.class);*/

    @Test
    public void FETest()
    {
        ActivityScenario.launch(MainActivity.class).onActivity(activity -> {
            Intent i = new Intent(activity.getApplicationContext(), FuelActivity.class);
            i.putExtra("Gsfcs", activity.Gsfcs);
            activity.startActivity(i);
        });
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Espresso.onView(ViewMatchers.withId(R.id.fuel_barchart)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

    }
}
