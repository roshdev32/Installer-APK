package com.installer.apkinstaller.backup2;

import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import java.io.InputStream;
import java.util.List;

public interface BackupIndex {

    @Nullable
    Backup getBackupMetaForUri(Uri uri);

    @Nullable
    Backup getLatestBackupForPackage(String pkg);

    void addEntry(Backup backup, BackupIconProvider iconProvider) throws Exception;

    @Nullable
    Backup deleteEntryByUri(Uri uri);

    List<String> getAllPackages();

    List<Backup> getAllBackupsForPackage(String pkg);

    LiveData<List<Backup>> getAllBackupsForPackageLiveData(String pkg);

    /**
     * Delete all entries from this index and add entries from {@code newIndex}
     * This operation is atomic, either the index is completely rewritten or an exception occurs and index is not changed at all
     *
     * @param newIndex
     */
    void rewrite(List<Backup> newIndex, BackupIconProvider iconProvider) throws Exception;

    interface BackupIconProvider {

        InputStream getIconInputStream(Backup backup) throws Exception;

    }

}
