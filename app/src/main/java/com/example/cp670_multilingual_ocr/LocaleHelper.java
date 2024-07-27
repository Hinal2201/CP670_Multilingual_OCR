package com.example.cp670_multilingual_ocr;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import java.util.Locale;

public class LocaleHelper {

    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";

    public static void setLocale(Context context, String languageCode) {
        persist(context, languageCode);
        updateResources(context, languageCode);
    }

    public static String getLanguage(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(SELECTED_LANGUAGE, Locale.getDefault().getLanguage());
    }

    private static void persist(Context context, String language) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(SELECTED_LANGUAGE, language).apply();
    }

    public static Context updateResources(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return context;
    }
}