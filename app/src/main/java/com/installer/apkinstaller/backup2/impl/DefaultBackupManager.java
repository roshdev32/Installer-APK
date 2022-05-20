package com.installer.apkinstaller.backup2.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.installer.apkinstaller.backup2.Backup;
import com.installer.apkinstaller.backup2.BackupApp;
import com.installer.apkinstaller.backup2.BackupAppDetails;
import com.installer.apkinstaller.backup2.BackupIndex;
import com.installer.apkinstaller.backup2.BackupManager;
import com.installer.apkinstaller.backup2.BackupStatus;
import com.installer.apkinstaller.backup2.BackupStorage;
import com.installer.apkinstaller.backup2.BackupStorageProvider;
import com.installer.apkinstaller.backup2.backuptask.config.BatchBackupTaskConfig;
import com.installer.apkinstaller.backup2.backuptask.config.SingleBackupTaskConfig;
import com.installer.apkinstaller.backup2.impl.db.DaoBackedBackupIndex;
import com.installer.apkinstaller.backup2.impl.local.LocalBackupStorageProvider;
import com.installer.apkinstaller.installer2.base.model.SaiPiSessionParams;
import com.installer.apkinstaller.installer2.impl.FlexSaiPackageInstaller;
import com.installer.apkinstaller.model.apksource.ApkSource;
import com.installer.apkinstaller.model.common.PackageMeta;
import com.installer.apkinstaller.utils.PreferencesHelper;
import com.installer.apkinstaller.utils.Stopwatch;
import com.installer.apkinstaller.utils.Utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefaultBackupManager implements BackupManager, BackupStorage.Observer, BackupIndex.BackupIconProvider {
    private static final String TAG = "DefaultBackupManager";

    private static DefaultBackupManager sInstance;

    private Context mContext;
    private BackupStorageProvider mStorageProvider;
    private BackupStorage mStorage;
    private BackupIndex mIndex;
    private PreferencesHelper mPrefsHelper;
    private FlexSaiPackageInstaller mInstaller;

    private Map<String, PackageMeta> mInstalledApps;
    private MutableLiveData<List<PackageMeta>> mInstalledAppsLiveData = new MutableLiveData<>(Collections.emptyList());
    private Handler mWorkerHandler;

    private Map<String, BackupApp> mApps;
    private MutableLiveData<List<BackupApp>> mAppsLiveData = new MutableLiveData<>(Collections.emptyList());

    private MutableLiveData<IndexingStatus> mIndexingStatus = new MutableLiveData<>(new IndexingStatus());

    private final Set<AppsObserver> mAppsObservers = new HashSet<>();

    private ExecutorService mMiscExecutor = Executors.newCachedThreadPool();

    public static synchronized DefaultBackupManager getInstance(Context context) {
        return sInstance != null ? sInstance : new DefaultBackupManager(context);
    }

    private DefaultBackupManager(Context context) {
        mContext = context.getApplicationContext();
        mStorageProvider = LocalBackupStorageProvider.getInstance(mContext);
        mStorage = mStorageProvider.getStorage();
        mIndex = DaoBackedBackupIndex.getInstance(mContext);
        mPrefsHelper = PreferencesHelper.getInstance(mContext);
        mInstaller = FlexSaiPackageInstaller.getInstance(mContext);

        HandlerThread workerThread = new HandlerThread("DefaultBackupManager Worker Thread");
        workerThread.start();
        mWorkerHandler = new Handler(workerThread.getLooper());

        mStorage.addObserver(this, mWorkerHandler);

        IntentFilter packagesStuffIntentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        packagesStuffIntentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        packagesStuffIntentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packagesStuffIntentFilter.addDataScheme("package");
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateAppInAppList(Objects.requireNonNull(intent.getData()).getSchemeSpecificPart());
            }
        }, packagesStuffIntentFilter, null, mWorkerHandler);

        mWorkerHandler.post(this::fetchPackages);

        if (!mPrefsHelper.isInitialIndexingDone()) {
            mWorkerHandler.post(this::scanBackups);
        }


        sInstance = this;
    }

    @Override
    public LiveData<List<PackageMeta>> getInstalledPackages() {
        return mInstalledAppsLiveData;
    }

    @Override
    public LiveData<List<BackupApp>> getApps() {
        return mAppsLiveData;
    }

    @Override
    public void enqueueBackup(SingleBackupTaskConfig config) {
        BackupStorage storage = getBackupStorageProvider(config.getBackupStorageId()).getStorage();
        BackupService2.enqueueBackup(mContext, storage.getStorageId(), storage.createBackupTask(config));
    }

    @Override
    public void enqueueBackup(BatchBackupTaskConfig config) {
        BackupStorage storage = getBackupStorageProvider(config.getBackupStorageId()).getStorage();
        BackupService2.enqueueBackup(mContext, storage.getStorageId(), storage.createBatchBackupTask(config));
    }

    @Override
    public void reindex() {
        mPrefsHelper.setInitialIndexingDone(false);
        mWorkerHandler.post(this::scanBackups);
    }

    @Override
    public LiveData<IndexingStatus> getIndexingStatus() {
        return mIndexingStatus;
    }

    @Override
    public LiveData<BackupAppDetails> getAppDetails(String pkg) {
        return new LiveAppDetails(pkg);
    }

    @Override
    public void deleteBackup(Uri backupUri, @Nullable BackupDeletionCallback callback, @Nullable Handler callbackHandler) {
        mMiscExecutor.execute(() -> {
            try {
                mStorage.deleteBackup(backupUri);

                if (callback != null && callbackHandler != null)
                    callbackHandler.post(() -> callback.onBackupDeleted(backupUri));

            } catch (Exception e) {
                Log.w(TAG, "Unable to delete backup", e);

                if (callback != null && callbackHandler != null)
                    callbackHandler.post(() -> callback.onFailedToDeleteBackup(backupUri, e));
            }
        });
    }

    @Override
    public void restoreBackup(Uri backupUri) {
        mMiscExecutor.execute(() -> {
            ApkSource apkSource = mStorage.createApkSource(backupUri);
            mInstaller.enqueueSession(mInstaller.createSessionOnInstaller(mPrefsHelper.getInstaller(), new SaiPiSessionParams(apkSource)));
        });
    }

    @Override
    public List<BackupStorageProvider> getBackupStorageProviders() {
        return Collections.singletonList(mStorageProvider);
    }

    @Override
    public BackupStorageProvider getBackupStorageProvider(String storageId) {
        if (mStorageProvider.getId().equals(storageId))
            return mStorageProvider;

        throw new IllegalArgumentException("Unknown storage provider");
    }

    @Override
    public BackupStorageProvider getDefaultBackupStorageProvider() {
        return mStorageProvider;
    }

    @WorkerThread
    @Nullable
    private PackageMeta getInstalledApp(String pkg) {
        enforceWorkerThread();
        return mInstalledApps.get(pkg);
    }

    @WorkerThread
    @Nullable
    private BackupApp getApp(String pkg) {
        enforceWorkerThread();
        return mApps.get(pkg);
    }

    private void addAppsObserver(AppsObserver observer) {
        synchronized (mAppsObservers) {
            mAppsObservers.add(observer);
        }
    }

    private void removeAppsObserver(AppsObserver observer) {
        synchronized (mAppsObservers) {
            mAppsObservers.remove(observer);
        }
    }

    private void notifyInstalledAppInvalidated(String pkg) {
        synchronized (mAppsObservers) {
            for (AppsObserver observer : mAppsObservers)
                observer.onInstalledAppInvalidated(pkg);
        }
    }

    private void notifyAppInvalidated(String pkg) {
        synchronized (mAppsObservers) {
            for (AppsObserver observer : mAppsObservers)
                observer.onAppInvalidated(pkg);
        }
    }


    @WorkerThread
    private void fetchPackages() {
        enforceWorkerThread();

        long start = System.currentTimeMillis();

        PackageManager pm = mContext.getPackageManager();
        List<PackageInfo> packageInfos = pm.getInstalledPackages(0);

        Map<String, PackageMeta> packages = new HashMap<>();
        for (PackageInfo packageInfo : packageInfos) {
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;

            PackageMeta packageMeta = new PackageMeta.Builder(applicationInfo.packageName)
                    .setLabel(applicationInfo.loadLabel(pm).toString())
                    .setHasSplits(applicationInfo.splitPublicSourceDirs != null && applicationInfo.splitPublicSourceDirs.length > 0)
                    .setIsSystemApp((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                    .setVersionCode(Utils.apiIsAtLeast(Build.VERSION_CODES.P) ? packageInfo.getLongVersionCode() : packageInfo.versionCode)
                    .setVersionName(packageInfo.versionName)
                    .setIcon(applicationInfo.icon)
                    .setInstallTime(packageInfo.firstInstallTime)
                    .setUpdateTime(packageInfo.lastUpdateTime)
                    .build();

            packages.put(packageMeta.packageName, packageMeta);
        }

        Log.d(TAG, String.format("Loaded packages in %d ms", (System.currentTimeMillis() - start)));

        mInstalledApps = packages;
        mInstalledAppsLiveData.postValue(new ArrayList<>(mInstalledApps.values()));

        rebuildAppList();
    }

    @WorkerThread
    private void scanBackups() {
        enforceWorkerThread();

        if (!mStorageProvider.isSetup()) {
            Log.w(TAG, "Storage provider is not configured, cancelling indexing");
            return;
        }

        mIndexingStatus.postValue(new IndexingStatus(0, 1));
        try {
            Stopwatch sw = new Stopwatch();
            Log.i(TAG, "Indexing backup storage...");

            List<Backup> backups = new ArrayList<>();

            List<Uri> backupFileUris = mStorage.listBackupFiles();
            for (int i = 0; i < backupFileUris.size(); i++) {
                Uri backupFileUri = backupFileUris.get(i);
                String fileHash = mStorage.getBackupFileHash(backupFileUri);

                Log.i(TAG, String.format("Indexing %s@%s", backupFileUri, fileHash));

                try {
                    Backup backup = mStorage.getBackupByUri(backupFileUri);
                    backups.add(backup);
                    Log.i(TAG, String.format("Indexed %s@%s", backupFileUri, fileHash));
                } catch (Exception e) {
                    Log.w(TAG, String.format("Unable to get meta for %s@%s, skipping", backupFileUri, fileHash), e);
                }

                mIndexingStatus.postValue(new IndexingStatus(i + 1, backupFileUris.size()));
            }

            try {
                mIndex.rewrite(backups, this);
                Log.i(TAG, "Index rewritten");
            } catch (Exception e) {
                Log.w(TAG, "Unable to rewrite index", e);
                throw e;
            }

            mPrefsHelper.setInitialIndexingDone(true);
            Log.i(TAG, String.format("Backup storage indexed in %d ms.", sw.millisSinceStart()));
        } catch (Exception e) {
            //TODO handle this
            throw new RuntimeException(e);
        }

        mIndexingStatus.postValue(new IndexingStatus());
        rebuildAppList();
    }

    @WorkerThread
    private void rebuildAppList() {
        enforceWorkerThread();

        Stopwatch sw = new Stopwatch();

        Map<String, BackupApp> backupApps = new HashMap<>();

        for (PackageMeta packageMeta : mInstalledApps.values()) {

            Backup backup = mIndex.getLatestBackupForPackage(packageMeta.packageName);
            if (backup != null) {
                backupApps.put(packageMeta.packageName, new BackupAppImpl(packageMeta, true, BackupStatus.fromInstalledAppAndBackupVersions(packageMeta.versionCode, backup.versionCode())));
            } else {
                backupApps.put(packageMeta.packageName, new BackupAppImpl(packageMeta, true, BackupStatus.NO_BACKUP));
            }
        }

        for (String pkg : mIndex.getAllPackages()) {
            if (mInstalledApps.containsKey(pkg))
                continue;

            Backup backup = mIndex.getLatestBackupForPackage(pkg);
            if (backup == null)
                continue;

            backupApps.put(pkg, new BackupAppImpl(backup.toPackageMeta(), false, BackupStatus.APP_NOT_INSTALLED));
        }

        mApps = backupApps;
        mAppsLiveData.postValue(new ArrayList<>(backupApps.values()));

        Log.i(TAG, String.format("Invalidated list in %d ms.", sw.millisSinceStart()));
    }

    @WorkerThread
    private void updateAppInAppList(String pkg) {
        enforceWorkerThread();

        PackageMeta packageMeta = PackageMeta.forPackage(mContext, pkg);
        if (packageMeta != null) {
            mInstalledApps.put(pkg, packageMeta);
        } else {
            mInstalledApps.remove(pkg);
        }
        notifyInstalledAppInvalidated(pkg);

        if (packageMeta != null) {
            Backup backup = mIndex.getLatestBackupForPackage(packageMeta.packageName);
            if (backup != null) {
                mApps.put(packageMeta.packageName, new BackupAppImpl(packageMeta, true, BackupStatus.fromInstalledAppAndBackupVersions(packageMeta.versionCode, backup.versionCode())));
            } else {
                mApps.put(packageMeta.packageName, new BackupAppImpl(packageMeta, true, BackupStatus.NO_BACKUP));
            }

            mAppsLiveData.postValue(new ArrayList<>(mApps.values()));
            notifyAppInvalidated(pkg);
        } else {
            Backup backup = mIndex.getLatestBackupForPackage(pkg);
            if (backup != null) {
                mApps.put(pkg, new BackupAppImpl(backup.toPackageMeta(), false, BackupStatus.APP_NOT_INSTALLED));
            } else {
                mApps.remove(pkg);
            }

            mAppsLiveData.postValue(new ArrayList<>(mApps.values()));
            notifyAppInvalidated(pkg);
        }
    }

    private void enforceWorkerThread() {
        if (Looper.myLooper() != mWorkerHandler.getLooper())
            throw new RuntimeException("This method must be invoked on mWorkerHandler");
    }

    @Override
    public void onBackupAdded(String storageId, Backup backup) {
        Stopwatch sw = new Stopwatch();

        try {
            mIndex.addEntry(backup, this);
            updateAppInAppList(backup.pkg());
        } catch (Exception e) {
            Log.e(TAG, "Unable to add backup to index, scheduling rescan", e);
            reindex();
        }


        Log.i(TAG, String.format("onBackupAdded handled in %d ms.", sw.millisSinceStart()));
    }

    @Override
    public void onBackupRemoved(String storageId, Uri backupUri) {
        Stopwatch sw = new Stopwatch();

        Backup backup = mIndex.deleteEntryByUri(backupUri);
        if (backup == null) {
            Log.w(TAG, String.format("Meta from deleteEntryByUri for uri %s in storage %s is null", backupUri.toString(), storageId));
            return;
        }
        updateAppInAppList(backup.pkg());

        Log.i(TAG, String.format("onBackupRemoved handled in %d ms.", sw.millisSinceStart()));
    }

    @Override
    public void onStorageUpdated(String storageId) {
        mPrefsHelper.setInitialIndexingDone(false);
        scanBackups();
    }

    @Override
    public InputStream getIconInputStream(Backup backup) throws Exception {
        return mStorage.getBackupIcon(backup.iconUri());
    }

    private static class BackupAppImpl implements BackupApp {

        private PackageMeta mPackageMeta;
        private boolean mIsInstalled;
        private BackupStatus mBackupStatus;

        private BackupAppImpl(PackageMeta packageMeta, boolean isInstalled, BackupStatus backupStatus) {
            mPackageMeta = packageMeta;
            mIsInstalled = isInstalled;
            mBackupStatus = backupStatus;
        }

        @Override
        public PackageMeta packageMeta() {
            return mPackageMeta;
        }

        @Override
        public boolean isInstalled() {
            return mIsInstalled;
        }

        @Override
        public BackupStatus backupStatus() {
            return mBackupStatus;
        }
    }

    /**
     * Observers are called on {@link #mWorkerHandler}
     */
    public interface AppsObserver {

        void onInstalledAppInvalidated(String pkg);

        void onAppInvalidated(String pkg);

    }

    private static class BackupAppDetailsImpl implements BackupAppDetails {

        private State mState;
        private BackupApp mApp;
        private List<Backup> mBackups;

        private BackupAppDetailsImpl(State state, BackupApp app, List<Backup> backups) {
            mState = state;
            mApp = app;
            mBackups = backups;
        }

        @Override
        public State state() {
            return mState;
        }

        @Override
        public BackupApp app() {
            return mApp;
        }

        @Override
        public List<Backup> backups() {
            return mBackups;
        }
    }

    private class LiveAppDetails extends LiveData<BackupAppDetails> implements Observer<List<Backup>>, AppsObserver {

        private String mPkg;
        private LiveData<List<Backup>> mMetasLiveData;

        private LiveAppDetails(String pkg) {
            mPkg = pkg;
            setValue(new BackupAppDetailsImpl(BackupAppDetails.State.LOADING, null, null));
        }

        @Override
        protected void onActive() {
            if (mMetasLiveData == null)
                mMetasLiveData = mIndex.getAllBackupsForPackageLiveData(mPkg);

            addAppsObserver(this);
            mMetasLiveData.observeForever(this);
        }

        @Override
        protected void onInactive() {
            mMetasLiveData.removeObserver(this);
            removeAppsObserver(this);

            if (!hasActiveObservers()) {
                mMetasLiveData = null;
            }
        }

        @Override
        public void onChanged(List<Backup> backupFileMetas) {
            invalidate();
        }

        @Override
        public void onInstalledAppInvalidated(String pkg) {

        }

        @Override
        public void onAppInvalidated(String pkg) {
            if (pkg.equals(mPkg))
                invalidate();
        }

        private void invalidate() {
            mWorkerHandler.post(() -> {
                BackupApp app = getApp(mPkg);
                if (app != null) {
                    postValue(new BackupAppDetailsImpl(BackupAppDetails.State.READY, getApp(mPkg), mIndex.getAllBackupsForPackage(mPkg)));
                } else {
                    postValue(new BackupAppDetailsImpl(BackupAppDetails.State.ERROR, null, null));
                }
            });
        }
    }


}
