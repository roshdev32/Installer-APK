package com.installer.apkinstaller.ui.dialogs;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import installer.apk.xapk.apkinstaller.R;
import com.installer.apkinstaller.backup2.impl.DefaultBackupManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RestoreBackupDialogFragment extends DialogFragment {

    private static final String ARG_BACKUP_URI = "backup_uri";
    private static final String ARG_TIMESTAMP = "timestamp";

    private Uri mBackupUri;
    private long mTimestamp;

    private SimpleDateFormat mBackupTimeSdf = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault());

    public static RestoreBackupDialogFragment newInstance(Uri backupUri, long timestamp) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_BACKUP_URI, backupUri);
        args.putLong(ARG_TIMESTAMP, timestamp);

        RestoreBackupDialogFragment dialog = new RestoreBackupDialogFragment();
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = requireArguments();
        mBackupUri = args.getParcelable(ARG_BACKUP_URI);
        mTimestamp = args.getLong(ARG_TIMESTAMP);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.backup_restore_backup_prompt, mBackupTimeSdf.format(new Date(mTimestamp))))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    DefaultBackupManager.getInstance(requireContext()).restoreBackup(mBackupUri);
                    Toast.makeText(requireContext(), R.string.backup_restore_backup_hint, Toast.LENGTH_SHORT).show();
                }).create();
    }
}
