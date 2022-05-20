package com.installer.apkinstaller.backup2;

import android.net.Uri;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.installer.apkinstaller.backup2.backuptask.config.BatchBackupTaskConfig;
import com.installer.apkinstaller.backup2.backuptask.config.SingleBackupTaskConfig;
import com.installer.apkinstaller.model.common.PackageMeta;

import java.util.List;

public interface BackupManager {

    LiveData<List<PackageMeta>> getInstalledPackages();

    LiveData<List<BackupApp>> getApps();

    void enqueueBackup(SingleBackupTaskConfig config);

    void enqueueBackup(BatchBackupTaskConfig config);

    void reindex();

    LiveData<IndexingStatus> getIndexingStatus();

    LiveData<BackupAppDetails> getAppDetails(String pkg);

    void deleteBackup(Uri backupUri, @Nullable BackupDeletionCallback callback, @Nullable Handler callbackHandler);

    default void deleteBackup(Backup backup, @Nullable BackupDeletionCallback callback, @Nullable Handler callbackHandler) {
        deleteBackup(backup.uri(), callback, callbackHandler);
    }

    void restoreBackup(Uri backupUri);

    List<BackupStorageProvider> getBackupStorageProviders();

    BackupStorageProvider getBackupStorageProvider(String storageId);

    BackupStorageProvider getDefaultBackupStorageProvider();

    class IndexingStatus {
        private boolean mInProgress;
        private int mProgress;
        private int mGoal;

        public IndexingStatus(int progress, int goal) {
            mInProgress = true;
            mProgress = progress;
            mGoal = goal;
        }

        public IndexingStatus() {
            mInProgress = false;
        }

        public boolean isInProgress() {
            return mInProgress;
        }

        public int progress() {
            return mProgress;
        }

        public int goal() {
            return mGoal;
        }


    }

    interface BackupDeletionCallback {

        void onBackupDeleted(Uri backupUri);

        void onFailedToDeleteBackup(Uri backupUri, Exception e);
    }

}
