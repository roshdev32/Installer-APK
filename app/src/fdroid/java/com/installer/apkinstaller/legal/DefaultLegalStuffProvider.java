package com.installer.apkinstaller.legal;

import android.content.Context;

public class DefaultLegalStuffProvider implements LegalStuffProvider {

    private static DefaultLegalStuffProvider sInstance;

    private Context mContext;

    public static synchronized DefaultLegalStuffProvider getInstance(Context context) {
        return sInstance != null ? sInstance : new DefaultLegalStuffProvider(context);
    }

    private DefaultLegalStuffProvider(Context context) {
        mContext = context.getApplicationContext();

        sInstance = this;
    }

    @Override
    public boolean hasPrivacyPolicy() {
        return true;
    }

    @Override
    public String getPrivacyPolicyUrl() {
        return "https://telegra.ph/Privacy-Policy-05-20-15";
    }

    @Override
    public boolean hasEula() {
        return false;
    }

    @Override
    public String getEulaUrl() { return null; }
}
