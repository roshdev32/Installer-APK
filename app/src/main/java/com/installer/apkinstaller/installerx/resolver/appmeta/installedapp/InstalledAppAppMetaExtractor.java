package com.installer.apkinstaller.installerx.resolver.appmeta.installedapp;

import android.content.Context;

import androidx.annotation.Nullable;

import com.installer.apkinstaller.installerx.resolver.appmeta.AppMeta;
import com.installer.apkinstaller.installerx.resolver.appmeta.AppMetaExtractor;
import com.installer.apkinstaller.installerx.resolver.meta.ApkSourceFile;
import com.installer.apkinstaller.installerx.resolver.meta.impl.InstalledAppApkSourceFile;
import com.installer.apkinstaller.model.common.PackageMeta;
import com.installer.apkinstaller.utils.Utils;

public class InstalledAppAppMetaExtractor implements AppMetaExtractor {
    private static final String TAG = "IAAppMetaExtractor";

    private Context mContext;

    public InstalledAppAppMetaExtractor(Context context) {
        mContext = context.getApplicationContext();
    }

    @Nullable
    @Override
    public AppMeta extract(ApkSourceFile apkSourceFile, ApkSourceFile.Entry baseApkEntry) {
        String extension = Utils.getExtension(apkSourceFile.getName());
        if (!InstalledAppApkSourceFile.FAKE_EXTENSION.equals(extension))
            return null;

        String pkg = Utils.getFileNameWithoutExtension(apkSourceFile.getName());
        PackageMeta packageMeta = PackageMeta.forPackage(mContext, pkg);
        if (packageMeta == null)
            return null;

        AppMeta appMeta = new AppMeta();
        appMeta.packageName = packageMeta.packageName;
        appMeta.appName = packageMeta.label;
        appMeta.versionCode = packageMeta.versionCode;
        appMeta.versionName = packageMeta.versionName;
        appMeta.iconUri = packageMeta.iconUri;

        return appMeta;
    }

}
