package com.installer.apkinstaller.backup2.impl.db;

import android.net.Uri;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.installer.apkinstaller.backup2.Backup;
import com.installer.apkinstaller.backup2.BackupComponent;

import java.io.File;
import java.util.List;

public class BackupWithComponents implements Backup {

    @Embedded
    public BackupEntity backup;
    @Relation(
            parentColumn = "uri",
            entityColumn = "backup_uri"
    )
    public List<BackupComponentEntity> components;

    @Override
    public String storageId() {
        return backup.storageId;
    }

    @Override
    public Uri uri() {
        return backup.getUri();
    }

    @Override
    public String pkg() {
        return backup.pkg;
    }

    @Override
    public String appName() {
        return backup.label;
    }

    @Override
    public Uri iconUri() {
        return Uri.fromFile(new File(backup.iconFile));
    }

    @Override
    public long versionCode() {
        return backup.versionCode;
    }

    @Override
    public String versionName() {
        return backup.versionName;
    }

    @Override
    public boolean isSplitApk() {
        return backup.isSplitApk();
    }

    @Override
    public long creationTime() {
        return backup.exportTimestamp;
    }

    @Override
    public String contentHash() {
        return backup.contentHash;
    }

    @Override
    public List<BackupComponent> components() {
        return (List<BackupComponent>) ((List<? extends BackupComponent>) components);
    }
}
