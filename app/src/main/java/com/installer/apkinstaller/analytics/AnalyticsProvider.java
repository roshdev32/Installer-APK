package com.installer.apkinstaller.analytics;

public interface AnalyticsProvider {

    boolean supportsDataCollection();

    boolean isDataCollectionEnabled();

    void setDataCollectionEnabled(boolean enabled);

}
