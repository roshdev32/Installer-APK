package com.installer.apkinstaller.ui.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import installer.apk.xapk.apkinstaller.R;
import com.installer.apkinstaller.utils.PreferencesKeys;

import java.util.Objects;

public class MiuiWarningDialogFragment extends DialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                .setTitle(R.string.installer_miui_warning_title)
                .setMessage(R.string.installer_miui_warning_message)
                .setPositiveButton(R.string.installer_miui_warning_open_dev_settings, (d, w) -> {
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(PreferencesKeys.MIUI_WARNING_SHOWN, true).apply();

                    try {
                        startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                    } catch (Exception e) {
                        SimpleAlertDialogFragment.newInstance(getString(R.string.error), getString(R.string.installer_miui_warning_oof)).show(getParentFragmentManager(), "alert_oof");
                    }

                    dismiss();
                })
                .create();
    }
}
