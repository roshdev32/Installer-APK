package com.installer.apkinstaller.viewmodels;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.installer.apkinstaller.backup2.BackupManager;
import com.installer.apkinstaller.backup2.backuptask.config.BatchBackupTaskConfig;
import com.installer.apkinstaller.backup2.backuptask.config.SingleBackupTaskConfig;
import com.installer.apkinstaller.backup2.impl.DefaultBackupManager;
import com.installer.apkinstaller.model.common.PackageMeta;

import java.util.ArrayList;
import java.util.List;

public class BatchBackupDialogViewModel extends ViewModel {
    private static final String TAG = "BatchBackupVM";

    private Context mContext;

    private BackupManager mBackupManager;

    private MutableLiveData<Boolean> mIsPreparing = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsBackupEnqueued = new MutableLiveData<>();

    private ArrayList<String> mSelectedPackages;

    public BatchBackupDialogViewModel(@NonNull Context applicationContext, @Nullable ArrayList<String> selectedPackages) {
        mContext = applicationContext;
        mBackupManager = DefaultBackupManager.getInstance(mContext);

        mSelectedPackages = selectedPackages;

        mIsPreparing.setValue(false);
        mIsBackupEnqueued.setValue(false);
    }

    public LiveData<Boolean> getIsPreparing() {
        return mIsPreparing;
    }

    public LiveData<Boolean> getIsBackupEnqueued() {
        return mIsBackupEnqueued;
    }

    public void enqueueBackup() {
        if (mIsPreparing.getValue())
            return;

        mIsPreparing.setValue(true);
        new Thread(() -> {
            if (mSelectedPackages != null) {
                backupSelectedApps();
            } else {
                backupLiterallyAllSplits();
            }

            mIsBackupEnqueued.postValue(true);
            mIsPreparing.postValue(false);
        }).start();
    }

    private void backupLiterallyAllSplits() {
        List<PackageMeta> packages = DefaultBackupManager.getInstance(mContext).getInstalledPackages().getValue();
        if (packages == null)
            return;

        String storageId = mBackupManager.getDefaultBackupStorageProvider().getId();
        List<SingleBackupTaskConfig> configs = new ArrayList<>();
        for (PackageMeta packageMeta : packages) {
            if (!packageMeta.hasSplits)
                continue;

            configs.add(new SingleBackupTaskConfig.Builder(storageId, packageMeta).build());
        }
        mBackupManager.enqueueBackup(new BatchBackupTaskConfig(storageId, configs));
    }

    private void backupSelectedApps() {

        String storageId = mBackupManager.getDefaultBackupStorageProvider().getId();
        List<SingleBackupTaskConfig> configs = new ArrayList<>();
        for (String pkg : mSelectedPackages) {
            PackageMeta packageMeta = PackageMeta.forPackage(mContext, pkg);
            if (packageMeta == null) {
                Log.d(TAG, "PackageMeta is null for " + pkg);
                continue;
            }

            configs.add(new SingleBackupTaskConfig.Builder(storageId, packageMeta).build());
        }

        if (configs.size() == 1) {
            mBackupManager.enqueueBackup(configs.get(0));
        } else {
            mBackupManager.enqueueBackup(new BatchBackupTaskConfig(storageId, configs));
        }
    }

    public int getApkCount() {
        return mSelectedPackages != null ? mSelectedPackages.size() : -1;
    }


}
