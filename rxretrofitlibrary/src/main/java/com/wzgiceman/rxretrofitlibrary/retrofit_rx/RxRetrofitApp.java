package com.wzgiceman.rxretrofitlibrary.retrofit_rx;

import android.app.Application;

/**
 * 全局app
 * Created by WZG on 2016/12/12.
 */

public class RxRetrofitApp {
    private static Application application;
    private static boolean debug;
    private static String baseUrl;


    public static void init(Application app) {
        setApplication(app);
        setDebug(true);
    }

    public static void init(Application app, boolean debug, String baseUrl) {
        setApplication(app);
        setDebug(debug);
        setBaseUrl(baseUrl);
    }

    public static Application getApplication() {
        return application;
    }

    private static void setApplication(Application application) {
        RxRetrofitApp.application = application;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        RxRetrofitApp.debug = debug;
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static void setBaseUrl(String baseUrl) {
        RxRetrofitApp.baseUrl = baseUrl;
    }
}
