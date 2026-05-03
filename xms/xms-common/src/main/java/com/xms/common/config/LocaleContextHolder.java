package com.xms.common.config;

public class LocaleContextHolder {
    private static final ThreadLocal<String> localeHolder = new ThreadLocal<>();

    public static String getLocale() {
        return localeHolder.get();
    }

    public static void setLocale(String locale) {
        localeHolder.set(locale);
    }

    public static void clearLocale() {
        localeHolder.remove();
    }
}
