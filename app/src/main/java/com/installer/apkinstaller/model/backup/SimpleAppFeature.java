package com.installer.apkinstaller.model.backup;

import com.installer.apkinstaller.model.common.AppFeature;

public class SimpleAppFeature implements AppFeature {

    private String mText;

    public SimpleAppFeature(String text) {
        mText = text;
    }

    @Override
    public CharSequence toText() {
        return mText;
    }
}
