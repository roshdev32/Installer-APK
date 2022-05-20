package com.installer.apkinstaller.backup2;

import android.net.Uri;

import com.installer.apkinstaller.model.common.PackageMeta;

import java.util.List;

public interface Backup {

    String storageId();

    /**
     * Get uri of this backup, uris must be namespaced to the storage
     *
     * @return uri of this backup
     */
    Uri uri();

    String pkg();

    String appName();

    Uri iconUri();

    long versionCode();

    String versionName();

    boolean isSplitApk();

    long creationTime();

    String contentHash();

    List<BackupComponent> components();

    default PackageMeta toPackageMeta() {
        return new PackageMeta.Builder(pkg())
                .setLabel(appName())
                .setVersionCode(versionCode())
                .setVersionName(versionName())
                .setIconUri(iconUri())
                .setHasSplits(isSplitApk())
                .build();
    }

}
