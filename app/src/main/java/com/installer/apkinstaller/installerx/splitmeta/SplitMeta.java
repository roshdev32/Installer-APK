package com.installer.apkinstaller.installerx.splitmeta;

import androidx.annotation.Nullable;

import com.installer.apkinstaller.installerx.splitmeta.config.AbiConfigSplitMeta;
import com.installer.apkinstaller.installerx.splitmeta.config.LocaleConfigSplitMeta;
import com.installer.apkinstaller.installerx.splitmeta.config.ScreenDestinyConfigSplitMeta;
import com.installer.apkinstaller.installerx.splitmeta.config.UnknownConfigSplitMeta;
import com.installer.apkinstaller.utils.TextUtils;

import java.util.HashMap;
import java.util.Map;

public abstract class SplitMeta {
    protected static final String ANDROID_XML_NAMESPACE = "http://schemas.android.com/apk/res/android";

    private String mPackageName;
    private long mVersionCode;
    private String mSplitName;

    public SplitMeta(Map<String, String> manifestAttrs) {
        mPackageName = TextUtils.requireNonEmpty(manifestAttrs.get("package"));
        mVersionCode = Long.parseLong(TextUtils.requireNonEmpty(manifestAttrs.get(ANDROID_XML_NAMESPACE + ":versionCode")));

        mSplitName = TextUtils.getNullIfEmpty(manifestAttrs.get("split"));
    }

    public String packageName() {
        return mPackageName;
    }

    public long versionCode() {
        return mVersionCode;
    }

    @Nullable
    public String splitName() {
        return mSplitName;
    }

    public static SplitMeta from(HashMap<String, String> manifestAttrs) {
        if (!manifestAttrs.containsKey("split")) {
            return new BaseSplitMeta(manifestAttrs);
        }

        if (manifestAttrs.containsKey(ANDROID_XML_NAMESPACE + ":isFeatureSplit")) {
            return new FeatureSplitMeta(manifestAttrs);
        }

        if (manifestAttrs.containsKey("configForSplit") || manifestAttrs.get("split").startsWith("config.")) {
            String splitName = TextUtils.requireNonEmpty(manifestAttrs.get("split"));

            if (AbiConfigSplitMeta.isAbiSplit(splitName))
                return new AbiConfigSplitMeta(manifestAttrs);

            if (ScreenDestinyConfigSplitMeta.isScreenDensitySplit(splitName))
                return new ScreenDestinyConfigSplitMeta(manifestAttrs);

            if (LocaleConfigSplitMeta.isLocaleSplit(splitName))
                return new LocaleConfigSplitMeta(manifestAttrs);

            return new UnknownConfigSplitMeta(manifestAttrs);
        }

        return new UnknownSplitMeta(manifestAttrs);
    }

}
