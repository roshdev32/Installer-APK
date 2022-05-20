package com.installer.apkinstaller.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import installer.apk.xapk.apkinstaller.R;
import com.installer.apkinstaller.adapters.BackupSplitPartsAdapter;
import com.installer.apkinstaller.model.common.PackageMeta;
import com.installer.apkinstaller.ui.dialogs.base.BaseBottomSheetDialogFragment;
import com.installer.apkinstaller.view.ViewSwitcherLayout;
import com.installer.apkinstaller.viewmodels.BackupDialogViewModel;

import java.util.Objects;

public class BackupDialogFragment extends BaseBottomSheetDialogFragment {
    private static final String ARG_PACKAGE = "package";

    private PackageMeta mPackage;
    private BackupDialogViewModel mViewModel;

    public static BackupDialogFragment newInstance(PackageMeta packageMeta) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_PACKAGE, packageMeta);

        BackupDialogFragment fragment = new BackupDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(BackupDialogViewModel.class);

        Bundle args = getArguments();
        if (args == null)
            return;
        mPackage = Objects.requireNonNull(args.getParcelable(ARG_PACKAGE));

        if (savedInstanceState == null)
            mViewModel.setPackage(mPackage);
    }

    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_backup, container, false);
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setTitle(R.string.backup_dialog_title);

        Button enqueueButton = getPositiveButton();
        enqueueButton.setText(R.string.backup_enqueue);
        enqueueButton.setOnClickListener((v) -> {
            mViewModel.enqueueBackup();
            dismiss();
        });

        Button cancelButton = getNegativeButton();
        cancelButton.setOnClickListener((v) -> dismiss());

        RecyclerView partsRecycler = view.findViewById(R.id.rv_backup_dialog);
        partsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        BackupSplitPartsAdapter adapter = new BackupSplitPartsAdapter(mViewModel.getSelection(), this, requireContext());
        partsRecycler.setAdapter(adapter);

        ViewSwitcherLayout viewSwitcher = view.findViewById(R.id.container_backup_dialog);

        mViewModel.getLoadingState().observe(this, state -> {
            switch (state) {
                case EMPTY:
                case LOADING:
                    viewSwitcher.setShownView(R.id.container_backup_dialog_loading);
                    enqueueButton.setVisibility(View.GONE);
                    break;
                case LOADED:
                    viewSwitcher.setShownView(R.id.container_backup_dialog_config);
                    enqueueButton.setVisibility(View.VISIBLE);
                    break;
                case FAILED:
                    viewSwitcher.setShownView(R.id.container_backup_dialog_error);
                    enqueueButton.setVisibility(View.GONE);
                    break;
            }
        });
        mViewModel.getSplitParts().observe(this, parts -> {
            adapter.setData(parts);
            revealBottomSheet();
        });
        mViewModel.getSelection().asLiveData().observe(this, selection -> enqueueButton.setEnabled(selection.hasSelection()));


        ViewGroup apkExportContainer = view.findViewById(R.id.container_backup_dialog_apk_export);
        Switch apkExportSwitch = view.findViewById(R.id.switch_backup_dialog_apk_export);

        apkExportContainer.setOnClickListener(v -> {
            mViewModel.setApkExportEnabled(!mViewModel.getIsApkExportEnabled().getValue());
        });

        mViewModel.getIsApkExportOptionAvailable().observe(this, available -> {
            apkExportContainer.setVisibility(available ? View.VISIBLE : View.GONE);
        });
        mViewModel.getIsApkExportEnabled().observe(this, apkExportSwitch::setChecked);
    }
}
