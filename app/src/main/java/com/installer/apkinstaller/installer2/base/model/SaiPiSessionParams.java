package com.installer.apkinstaller.installer2.base.model;

import androidx.annotation.NonNull;

import com.installer.apkinstaller.model.apksource.ApkSource;

public class SaiPiSessionParams {

    private ApkSource mApkSource;

    public SaiPiSessionParams(@NonNull ApkSource apkSource) {
        mApkSource = apkSource;
    }

    @NonNull
    public ApkSource apkSource() {
        return mApkSource;
    }

}
