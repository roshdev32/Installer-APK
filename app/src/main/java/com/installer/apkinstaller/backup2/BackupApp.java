package com.installer.apkinstaller.backup2;

import com.installer.apkinstaller.model.common.PackageMeta;

public interface BackupApp {

    PackageMeta packageMeta();

    boolean isInstalled();

    BackupStatus backupStatus();
}
