// Mengelola teks dalam berbagai bahasa
// Mendukung bahasa Indonesia dan Inggris
// Memudahkan pergantian bahasa secara dinamis

package com.github.repo.tracker.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceManager {

    private static Locale currentLocale = Locale.getDefault();
    private static ResourceBundle bundle = ResourceBundle.getBundle("messages", currentLocale);

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        bundle = ResourceBundle.getBundle("messages", currentLocale);
    }

    public static String get(String key) {
        return bundle.getString(key);
    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }
} 