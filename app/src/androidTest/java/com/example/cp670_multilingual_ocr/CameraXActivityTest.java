package com.example.cp670_multilingual_ocr;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.camera.lifecycle.ProcessCameraProvider;

import com.google.common.util.concurrent.ListenableFuture;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CameraXActivityTest {

    @Rule
    public ActivityScenarioRule<CameraXActivity> activityScenarioRule =
            new ActivityScenarioRule<>(CameraXActivity.class);

    private ListenableFuture<ProcessCameraProvider> provider = null;

    @Before
    public void beforeTestSuite() {
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        provider = ProcessCameraProvider.getInstance(context);
        Assert.assertNotNull(provider);
    }

    @Test
    public void testCameraXActivityLaunch()  {
        // Use Espresso to check if the camera preview is displayed
        Espresso.onView(ViewMatchers.withId(R.id.camera_preview))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

    }

  }