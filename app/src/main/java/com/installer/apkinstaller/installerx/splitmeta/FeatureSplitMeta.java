package com.installer.apkinstaller.installerx.splitmeta;

import com.installer.apkinstaller.utils.TextUtils;

import java.util.Map;

public class FeatureSplitMeta extends SplitMeta {

    private String mModule;

    public FeatureSplitMeta(Map<String, String> manifestAttrs) {
        super(manifestAttrs);
        mModule = TextUtils.requireNonEmpty(manifestAttrs.get("split"));
    }

    public String module() {
        return mModule;
    }

}
