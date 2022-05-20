package com.installer.apkinstaller.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

import installer.apk.xapk.apkinstaller.R;
import com.installer.apkinstaller.ui.fragments.miui.MiEntryFragment;
import com.installer.apkinstaller.utils.PreferencesKeys;

public class MiActivity extends ThemedActivity implements MiEntryFragment.OnContinueListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mi_fragment_container, new MiEntryFragment())
                    .commitNow();
        }
    }

    @Override
    public void onContinue() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(PreferencesKeys.MIUI_WARNING_SHOWN, true).apply();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
