package com.installer.apkinstaller.backup2;

import androidx.annotation.DrawableRes;

import installer.apk.xapk.apkinstaller.R;

public enum BackupStatus {
    NO_BACKUP, SAME_VERSION, HIGHER_VERSION, LOWER_VERSION, APP_NOT_INSTALLED;

    public static BackupStatus fromInstalledAppAndBackupVersions(long installedAppVersion, long backupVersion) {
        if (backupVersion == installedAppVersion)
            return BackupStatus.SAME_VERSION;
        else if (backupVersion > installedAppVersion)
            return BackupStatus.HIGHER_VERSION;
        else
            return BackupStatus.LOWER_VERSION;
    }

    @DrawableRes
    public int getIconRes() {
        switch (this) {
            case NO_BACKUP:
                return R.drawable.ic_backup_status_no_backup;
            case SAME_VERSION:
                return R.drawable.ic_backup_status_same_version;
            case HIGHER_VERSION:
                return R.drawable.ic_backup_status_higher_version;
            case LOWER_VERSION:
                return R.drawable.ic_backup_status_lower_version;
            case APP_NOT_INSTALLED:
                return R.drawable.ic_backup_status_not_installed;
        }

        throw new RuntimeException("wtf");
    }

    public boolean canBeInstalledOverExistingApp() {
        switch (this) {
            case SAME_VERSION:
            case HIGHER_VERSION:
            case APP_NOT_INSTALLED:
                return true;
            case LOWER_VERSION:
            case NO_BACKUP:
                return false;
        }

        throw new RuntimeException("wtf");
    }
}
