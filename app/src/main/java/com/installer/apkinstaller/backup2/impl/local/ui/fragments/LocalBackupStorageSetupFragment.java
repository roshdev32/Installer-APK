package com.installer.apkinstaller.backup2.impl.local.ui.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import installer.apk.xapk.apkinstaller.R;
import com.installer.apkinstaller.backup2.impl.local.ui.viewmodels.LocalBackupStorageSetupViewModel;
import com.installer.apkinstaller.ui.dialogs.UriDirectoryPickerDialogFragment;
import com.installer.apkinstaller.ui.fragments.SaiBaseFragment;

public class LocalBackupStorageSetupFragment extends SaiBaseFragment implements UriDirectoryPickerDialogFragment.OnDirectoryPickedListener {
    private LocalBackupStorageSetupViewModel mViewModel;

    @Override
    protected int layoutId() {
        return R.layout.fragment_local_backup_storage_setup;
    }

    public static LocalBackupStorageSetupFragment newInstance() {
        return new LocalBackupStorageSetupFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(LocalBackupStorageSetupViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button selectDirButton = findViewById(R.id.button_lbs_select_dir);
        selectDirButton.setOnClickListener(v -> selectBackupDir());
        selectDirButton.requestFocus(); //TV fix
    }

    private void selectBackupDir() {
        UriDirectoryPickerDialogFragment.newInstance(requireContext()).show(getChildFragmentManager(), "backup_dir");
    }

    @Override
    public void onDirectoryPicked(@Nullable String tag, Uri dirUri) {
        if (tag == null)
            return;

        switch (tag) {
            case "backup_dir":
                mViewModel.setBackupDir(dirUri);
                break;
        }
    }
}
