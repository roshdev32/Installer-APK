package com.installer.apkinstaller.installerx.splitmeta;

import androidx.annotation.Nullable;

import com.installer.apkinstaller.utils.TextUtils;

import java.util.HashMap;

public class BaseSplitMeta extends SplitMeta {

    private String mVersionName;

    public BaseSplitMeta(HashMap<String, String> manifestAttrs) {
        super(manifestAttrs);
        mVersionName = TextUtils.getNullIfEmpty(manifestAttrs.get(ANDROID_XML_NAMESPACE + ":versionName"));
    }

    @Nullable
    public String versionName() {
        return mVersionName;
    }

}
