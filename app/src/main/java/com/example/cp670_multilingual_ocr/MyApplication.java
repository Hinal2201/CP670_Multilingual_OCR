package com.example.cp670_multilingual_ocr;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.updateResources(base, LocaleHelper.getLanguage(base)));
    }
}