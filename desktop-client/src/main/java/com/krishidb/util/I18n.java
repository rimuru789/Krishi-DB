package com.krishidb.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class I18n {

    public interface LocaleChangeListener {
        void onLocaleChange();
    }

    public static final Locale LOCALE_EN = Locale.ENGLISH;
    public static final Locale LOCALE_MR = Locale.of("mr", "IN");
    public static final Locale LOCALE_HI = Locale.of("hi", "IN");

    private static final String BUNDLE_BASE_NAME = "i18n.messages";

    private static Locale currentLocale = LOCALE_EN;
    private static ResourceBundle currentBundle;
    private static ResourceBundle fallbackBundle;

    private static final List<LocaleChangeListener> listeners = new ArrayList<>();

    static {
        try {
            fallbackBundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, LOCALE_EN);
        } catch (Exception e) {
            System.err.println("Warning: Fallback English bundle could not be loaded: " + e.getMessage());
        }
        setLocale(LOCALE_EN);
    }

    public static synchronized void setLanguage(String langCode) {
        if (langCode == null || langCode.trim().isEmpty()) {
            langCode = "en";
        }
        switch (langCode.toLowerCase()) {
            case "mr":
                setLocale(LOCALE_MR);
                break;
            case "hi":
                setLocale(LOCALE_HI);
                break;
            case "en":
            default:
                setLocale(LOCALE_EN);
                break;
        }
    }

    public static synchronized void setLocale(Locale locale) {
        if (locale == null) {
            locale = LOCALE_EN;
        }
        currentLocale = locale;
        try {
            currentBundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, currentLocale);
        } catch (Exception e) {
            System.err.println("Could not load bundle for " + currentLocale + ", falling back to English: " + e.getMessage());
            currentBundle = fallbackBundle;
        }

        notifyListeners();
    }

    public static synchronized Locale getCurrentLocale() {
        return currentLocale;
    }

    public static synchronized String getLanguageCode() {
        if (currentLocale.getLanguage().equalsIgnoreCase("mr")) {
            return "mr";
        } else if (currentLocale.getLanguage().equalsIgnoreCase("hi")) {
            return "hi";
        }
        return "en";
    }

    public static synchronized String get(String key) {
        if (key == null) return "";
        try {
            if (currentBundle != null && currentBundle.containsKey(key)) {
                return currentBundle.getString(key);
            }
        } catch (Exception ignored) {}

        try {
            if (fallbackBundle != null && fallbackBundle.containsKey(key)) {
                return fallbackBundle.getString(key);
            }
        } catch (Exception ignored) {}

        return key;
    }

    public static synchronized String get(String key, Object... args) {
        String pattern = get(key);
        if (args == null || args.length == 0) {
            return pattern;
        }
        try {
            return MessageFormat.format(pattern, args);
        } catch (Exception e) {
            return pattern;
        }
    }

    public static synchronized void addListener(LocaleChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static synchronized void removeListener(LocaleChangeListener listener) {
        listeners.remove(listener);
    }

    private static synchronized void notifyListeners() {
        for (LocaleChangeListener listener : new ArrayList<>(listeners)) {
            try {
                listener.onLocaleChange();
            } catch (Exception e) {
                System.err.println("Error updating localized component: " + e.getMessage());
            }
        }
    }
}
