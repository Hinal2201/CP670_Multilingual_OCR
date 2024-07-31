package com.example.cp670_multilingual_ocr;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OCRTest {

    @Rule
    public ActivityScenarioRule<OCR> activityScenarioRule =
            new ActivityScenarioRule<>(OCR.class);

    @Before
    public void setup() {
        // Initialize any necessary dependencies here
    }

    @Test
    public void testOCRActivityLaunch() {
        // Check if the OCR activity is displayed
        Espresso.onView(ViewMatchers.withId(R.id.ocrNoteTitleEditText))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testSetSaveConfirmed() {
        // Get activity instance
        activityScenarioRule.getScenario().onActivity(activity -> {
            // Set save_confirmed to true
            activity.setSaveConfirmed(true);
            // Check if save_confirmed is true
            Assert.assertTrue(activity.getSaveConfirmed());

            // Set save_confirmed to false
            activity.setSaveConfirmed(false);

            // Check if save_confirmed is false
            Assert.assertFalse(activity.getSaveConfirmed());
        });


    }

}