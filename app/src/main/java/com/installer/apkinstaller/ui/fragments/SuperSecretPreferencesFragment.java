package com.installer.apkinstaller.ui.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import installer.apk.xapk.apkinstaller.R;
import com.installer.apkinstaller.utils.DbgPreferencesKeys;

public class SuperSecretPreferencesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_super_secret);

        findPreference(DbgPreferencesKeys.TEST_CRASH).setOnPreferenceClickListener(p -> {
            throw new RuntimeException("Test Crash");
        });
    }
}
