package com.installer.apkinstaller.installer.shizuku;

import android.content.Context;

import installer.apk.xapk.apkinstaller.R;
import com.installer.apkinstaller.installer.ShellSAIPackageInstaller;
import com.installer.apkinstaller.shell.Shell;
import com.installer.apkinstaller.shell.ShizukuShell;

public class ShizukuSAIPackageInstaller extends ShellSAIPackageInstaller {
    private static ShizukuSAIPackageInstaller sInstance;

    public static ShizukuSAIPackageInstaller getInstance(Context c) {
        synchronized (ShizukuSAIPackageInstaller.class) {
            return sInstance != null ? sInstance : new ShizukuSAIPackageInstaller(c);
        }
    }

    private ShizukuSAIPackageInstaller(Context c) {
        super(c);
        sInstance = this;
    }

    @Override
    protected Shell getShell() {
        return ShizukuShell.getInstance();
    }

    @Override
    protected String getInstallerName() {
        return "Shizuku";
    }

    @Override
    protected String getShellUnavailableMessage() {
        return getContext().getString(R.string.installer_error_shizuku_unavailable);
    }
}
