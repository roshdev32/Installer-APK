package com.installer.apkinstaller.installerx.resolver.appmeta;

import androidx.annotation.Nullable;

import com.installer.apkinstaller.installerx.resolver.meta.ApkSourceFile;

/**
 * A class that helps to extract some metadata about the app in an ApkSourceFile
 */
public interface AppMetaExtractor {

    @Nullable
    AppMeta extract(ApkSourceFile apkSourceFile, ApkSourceFile.Entry baseApkEntry);

}
