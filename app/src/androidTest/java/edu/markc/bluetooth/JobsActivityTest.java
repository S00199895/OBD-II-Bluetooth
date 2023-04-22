package edu.markc.bluetooth;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class JobsActivityTest {
   /* @Rule
    public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(
            MainActivity.class);
    Context ctx = null;

    public JobsActivityTest() {
        rule.getScenario().onActivity(main
                -> {
            ctx = main.getApplicationContext();
        });
    }

*/

    @Rule
    public ActivityScenarioRule<MainActivity> jobsRule = new ActivityScenarioRule<>(
            MainActivity.class);


    @Test
    public void readJobsTest()
    {
        ActivityScenario.launch(JobsActivity.class).onActivity(activity -> {


            CurrentJobs.getInstance().notes.size();

        });

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(  CurrentJobs.getInstance().notes.size() > 0);

    }

}
