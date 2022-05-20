package com.installer.apkinstaller.installerx.common;

import androidx.annotation.Nullable;

import com.installer.apkinstaller.installerx.splitmeta.SplitMeta;

public interface SplitPart {

    SplitMeta meta();

    String id();

    /**
     * @return the local path of this part in the apk source
     */
    String localPath();

    String name();

    long size();

    @Nullable
    String description();

    boolean isRequired();

    boolean isRecommended();

}
