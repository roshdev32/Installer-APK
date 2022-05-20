package com.installer.apkinstaller.backup2.impl.local;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import com.installer.apkinstaller.model.common.PackageMeta;
import com.installer.apkinstaller.utils.BackupNameFormat;
import com.installer.apkinstaller.utils.DbgPreferencesHelper;
import com.installer.apkinstaller.utils.Utils;
import com.installer.apkinstaller.utils.saf.FileUtils;
import com.installer.apkinstaller.utils.saf.SafUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class LocalBackupUtils {
    private static final String TAG = "BackupUtils";

    /**
     * @param c
     * @param backupDirUri
     * @param packageMeta
     * @param apksFile     if true, created file will have .apks extension, otherwise - .apk
     * @return
     */
    @SuppressLint("DefaultLocale")
    @Nullable
    public static Uri createBackupFile(Context c, Uri backupDirUri, PackageMeta packageMeta, boolean apksFile) {

        String extension = apksFile ? "apks" : "apk";

        if (ContentResolver.SCHEME_FILE.equals(backupDirUri.getScheme())) {
            return createBackupUriViaFileIO(c, new File(Objects.requireNonNull(backupDirUri.getPath())), packageMeta, extension);
        } else if (ContentResolver.SCHEME_CONTENT.equals(backupDirUri.getScheme())) {
            return createBackupUriViaSaf(c, backupDirUri, packageMeta, extension);
        }

        return null;
    }

    @SuppressLint("DefaultLocale")
    @Nullable
    private static Uri createBackupUriViaFileIO(Context c, File backupsDir, PackageMeta packageMeta, String extension) {
        if (!backupsDir.exists() && !backupsDir.mkdir()) {
            Log.e(TAG, "Unable to mkdir:" + backupsDir.toString());
            return null;
        }

        String backupFileName = getFileNameForPackageMeta(c, packageMeta);

        File backupFile = new File(backupsDir, Utils.escapeFileName(String.format("%s.%s", backupFileName, extension)));
        int suffix = 0;
        while (backupFile.exists()) {
            suffix++;
            backupFile = new File(backupsDir, Utils.escapeFileName(String.format("%s(%d).%s", backupFileName, suffix, extension)));
        }

        try {
            if (!backupFile.createNewFile())
                return null;
        } catch (IOException e) {
            Log.e(TAG, "Unable to create backup file", e);
            return null;
        }

        return Uri.fromFile(backupFile);
    }

    @SuppressLint("DefaultLocale")
    @Nullable
    private static Uri createBackupUriViaSaf(Context c, Uri backupDirUri, PackageMeta packageMeta, String extension) {
        DocumentFile backupDirFile = DocumentFile.fromTreeUri(c, backupDirUri);
        if (backupDirFile == null)
            return null;

        String backupFileName = getFileNameForPackageMeta(c, packageMeta);

        String actualBackupFileName = String.format("%s.%s", backupFileName, extension);
        int suffix = 0;
        while (true) {
            DocumentFile backupFileCandidate = DocumentFile.fromSingleUri(c, SafUtils.buildChildDocumentUri(backupDirUri, actualBackupFileName));
            if (backupFileCandidate == null || !backupFileCandidate.exists())
                break;

            actualBackupFileName = String.format("%s(%d).%s", backupFileName, ++suffix, extension);
        }

        DocumentFile backupFile = backupDirFile.createFile("saf/sucks", FileUtils.buildValidFatFilename(actualBackupFileName));
        if (backupFile == null)
            return null;

        return backupFile.getUri();
    }

    private static String getFileNameForPackageMeta(Context c, PackageMeta packageMeta) {
        String backupFileName = BackupNameFormat.format(LocalBackupStorageProvider.getInstance(c).getBackupNameFormat(), packageMeta);
        if (DbgPreferencesHelper.getInstance(c).shouldReplaceDots())
            backupFileName = backupFileName.replace('.', '_');

        if (backupFileName.length() > 160)
            backupFileName = backupFileName.substring(0, 160);

        return Utils.escapeFileName(backupFileName);
    }
}
