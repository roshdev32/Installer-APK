package com.installer.apkinstaller.viewmodels;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.installer.apkinstaller.backup2.Backup;
import com.installer.apkinstaller.backup2.BackupAppDetails;
import com.installer.apkinstaller.backup2.BackupManager;
import com.installer.apkinstaller.backup2.impl.DefaultBackupManager;

public class BackupManageAppViewModel extends ViewModel {

    private Context mContext;
    private String mPackage;

    private BackupManager mBackupManager;

    private LiveData<BackupAppDetails> mDetailsLiveData;

    public BackupManageAppViewModel(Context appContext, String pkg) {
        mContext = appContext;
        mPackage = pkg;

        mBackupManager = DefaultBackupManager.getInstance(mContext);

        mDetailsLiveData = mBackupManager.getAppDetails(pkg);
    }

    public LiveData<BackupAppDetails> getDetails() {
        return mDetailsLiveData;
    }

    public String getPackage() {
        return mPackage;
    }

    @Nullable
    public Backup getLatestBackup() {
        BackupAppDetails details = mDetailsLiveData.getValue();
        if (details == null)
            return null;

        if (details.backups().size() > 0)
            return details.backups().get(0);

        return null;
    }
}
