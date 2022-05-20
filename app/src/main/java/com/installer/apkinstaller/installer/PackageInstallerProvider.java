package com.installer.apkinstaller.installer;

import android.content.Context;

import com.installer.apkinstaller.installer.rooted.RootedSAIPackageInstaller;
import com.installer.apkinstaller.installer.rootless.RootlessSAIPackageInstaller;
import com.installer.apkinstaller.installer.shizuku.ShizukuSAIPackageInstaller;
import com.installer.apkinstaller.utils.PreferencesHelper;
import com.installer.apkinstaller.utils.PreferencesValues;

public class PackageInstallerProvider {
    public static SAIPackageInstaller getInstaller(Context c) {

        switch (PreferencesHelper.getInstance(c).getInstaller()) {
            case PreferencesValues.INSTALLER_ROOTLESS:
                return RootlessSAIPackageInstaller.getInstance(c);
            case PreferencesValues.INSTALLER_ROOTED:
                return RootedSAIPackageInstaller.getInstance(c);
            case PreferencesValues.INSTALLER_SHIZUKU:
                return ShizukuSAIPackageInstaller.getInstance(c);
        }

        return RootlessSAIPackageInstaller.getInstance(c);
    }
}
