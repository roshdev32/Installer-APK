package com.installer.apkinstaller.installer2.impl.shell;

import android.content.Context;

import installer.apk.xapk.apkinstaller.R;
import com.installer.apkinstaller.shell.Shell;
import com.installer.apkinstaller.shell.SuShell;

public class RootedSaiPackageInstaller extends ShellSaiPackageInstaller {

    private static RootedSaiPackageInstaller sInstance;

    public static RootedSaiPackageInstaller getInstance(Context c) {
        synchronized (RootedSaiPackageInstaller.class) {
            return sInstance != null ? sInstance : new RootedSaiPackageInstaller(c);
        }
    }

    private RootedSaiPackageInstaller(Context c) {
        super(c);
        sInstance = this;
    }

    @Override
    protected Shell getShell() {
        return SuShell.getInstance();
    }

    @Override
    protected String getInstallerName() {
        return "Rooted";
    }

    @Override
    protected String getShellUnavailableMessage() {
        return getContext().getString(R.string.installer_error_root_no_root);
    }

    @Override
    protected String tag() {
        return "RootedSaiPi";
    }
}
