package com.installer.apkinstaller.installerx.resolver.appmeta;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.installer.apkinstaller.installerx.resolver.appmeta.apkm.ApkmAppMetaExtractor;
import com.installer.apkinstaller.installerx.resolver.appmeta.apks.SaiAppMetaExtractor;
import com.installer.apkinstaller.installerx.resolver.appmeta.brute.BruteAppMetaExtractor;
import com.installer.apkinstaller.installerx.resolver.appmeta.xapk.XapkAppMetaExtractor;
import com.installer.apkinstaller.installerx.resolver.meta.ApkSourceFile;
import com.installer.apkinstaller.utils.Utils;

public class DefaultAppMetaExtractor implements AppMetaExtractor {
    private static final String TAG = "DefaultAppMetaExtractor";

    private Context mContext;

    public DefaultAppMetaExtractor(Context context) {
        mContext = context.getApplicationContext();
    }

    @Nullable
    @Override
    public AppMeta extract(ApkSourceFile apkSourceFile, ApkSourceFile.Entry baseApkEntry) {

        AppMeta appMeta = null;

        AppMetaExtractor appMetaExtractor = fromArchiveExtension(Utils.getExtension(apkSourceFile.getName()));
        if (appMetaExtractor != null) {
            Log.i(TAG, String.format("Using %s to extract meta from %s", appMetaExtractor.getClass().getSimpleName(), apkSourceFile.getName()));
            appMeta = appMetaExtractor.extract(apkSourceFile, baseApkEntry);
        }

        if (appMeta == null) {
            Log.i(TAG, String.format("Using BruteAppMetaExtractor to extract meta from %s", apkSourceFile.getName()));
            appMeta = new BruteAppMetaExtractor(mContext).extract(apkSourceFile, baseApkEntry);
        }

        return appMeta;
    }

    @Nullable
    private AppMetaExtractor fromArchiveExtension(@Nullable String archiveExtension) {
        if (archiveExtension == null)
            return null;

        switch (archiveExtension.toLowerCase()) {
            case "xapk":
                return new XapkAppMetaExtractor(mContext);
            case "apks":
                return new SaiAppMetaExtractor(mContext);
            case "apkm":
                return new ApkmAppMetaExtractor(mContext);
            default:
                return null;
        }
    }
}
