package com.installer.apkinstaller.ui.dialogs;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import installer.apk.xapk.apkinstaller.R;
import com.installer.apkinstaller.adapters.SplitApkSourceMetaAdapter;
import com.installer.apkinstaller.installerx.resolver.urimess.UriHostFactory;
import com.installer.apkinstaller.ui.dialogs.base.BaseBottomSheetDialogFragment;
import com.installer.apkinstaller.utils.AlertsUtils;
import com.installer.apkinstaller.utils.PermissionsUtils;
import com.installer.apkinstaller.utils.PreferencesHelper;
import com.installer.apkinstaller.utils.Utils;
import com.installer.apkinstaller.view.ViewSwitcherLayout;
import com.installer.apkinstaller.viewmodels.InstallerXDialogViewModel;
import com.installer.apkinstaller.viewmodels.factory.InstallerXDialogViewModelFactory;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.installer.apkinstaller.installerx.resolver.urimess.impl.AndroidUriHost;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InstallerXDialogFragment extends BaseBottomSheetDialogFragment implements FilePickerDialogFragment.OnFilesSelectedListener, SimpleAlertDialogFragment.OnDismissListener {
    private static final int REQUEST_CODE_GET_FILES = 337;

    private static final String ARG_APK_SOURCE_URI = "apk_source_uri";
    private static final String ARG_URI_HOST_FACTORY = "uri_host_factory";

    private InstallerXDialogViewModel mViewModel;

    private PreferencesHelper mHelper;

    private int mActionAfterGettingStoragePermissions;
    private static final int PICK_WITH_INTERNAL_FILEPICKER = 0;
    private static final int PICK_WITH_SAF = 1;

    private static final String DIALOG_TAG_Q_SAF_WARNING = "q_saf_warning";

    /**
     * Create an instance of InstallerXDialogFragment with given apk source uri and UriHostFactory class.
     * If {@code apkSourceUri} is null, dialog will let user pick apk source file.
     * If {@code uriHostFactoryClass} is null, {@link AndroidUriHost} will be used.
     *
     * @param apkSourceUri
     * @param uriHostFactoryClass
     * @return
     */
    public static InstallerXDialogFragment newInstance(@Nullable Uri apkSourceUri, @Nullable Class<? extends UriHostFactory> uriHostFactoryClass) {
        Bundle args = new Bundle();
        if (apkSourceUri != null)
            args.putParcelable(ARG_APK_SOURCE_URI, apkSourceUri);

        if (uriHostFactoryClass != null)
            args.putString(ARG_URI_HOST_FACTORY, uriHostFactoryClass.getCanonicalName());

        InstallerXDialogFragment fragment = new InstallerXDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        UriHostFactory uriHostFactory = null;
        if (args != null) {
            String uriHostFactoryClass = args.getString(ARG_URI_HOST_FACTORY);
            if (uriHostFactoryClass != null) {
                try {
                    uriHostFactory = (UriHostFactory) Class.forName(uriHostFactoryClass).getConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        mHelper = PreferencesHelper.getInstance(requireContext());
        mViewModel = new ViewModelProvider(this, new InstallerXDialogViewModelFactory(requireContext(), uriHostFactory)).get(InstallerXDialogViewModel.class);

        if (args == null)
            return;

        Uri apkSourceUri = args.getParcelable(ARG_APK_SOURCE_URI);
        if (apkSourceUri != null)
            mViewModel.setApkSourceUris(Collections.singletonList(apkSourceUri));
    }

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_installerx, container, false);
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onContentViewCreated(view, savedInstanceState);

        setTitle(R.string.installerx_dialog_title);
        getPositiveButton().setText(R.string.installerx_dialog_install);

        ViewSwitcherLayout viewSwitcher = view.findViewById(R.id.container_dialog_installerx);

        RecyclerView recycler = view.findViewById(R.id.rv_dialog_installerx_content);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.getRecycledViewPool().setMaxRecycledViews(SplitApkSourceMetaAdapter.VH_TYPE_SPLIT_PART, 16);

        SplitApkSourceMetaAdapter adapter = new SplitApkSourceMetaAdapter(mViewModel.getPartsSelection(), this, requireContext());
        recycler.setAdapter(adapter);

        getNegativeButton().setOnClickListener(v -> dismiss());
        getPositiveButton().setOnClickListener(v -> {
            mViewModel.enqueueInstallation();
            dismiss();
        });

        view.findViewById(R.id.button_installerx_fp_internal).setOnClickListener(v -> checkPermissionsAndPickFiles());
        view.findViewById(R.id.button_installerx_fp_saf).setOnClickListener(v -> pickFilesWithSaf(false));

        TextView warningTv = view.findViewById(R.id.tv_installerx_warning);
        mViewModel.getState().observe(this, state -> {
            switch (state) {
                case NO_DATA:
                    viewSwitcher.setShownView(R.id.container_installerx_no_data);
                    getPositiveButton().setVisibility(View.GONE);
                    break;
                case LOADING:
                    viewSwitcher.setShownView(R.id.container_installerx_loading);
                    getPositiveButton().setVisibility(View.GONE);
                    break;
                case LOADED:
                    viewSwitcher.setShownView(R.id.rv_dialog_installerx_content);
                    getPositiveButton().setVisibility(View.VISIBLE);
                    break;
                case WARNING:
                    viewSwitcher.setShownView(R.id.container_installerx_warning);
                    warningTv.setText(mViewModel.getWarning().message());
                    getPositiveButton().setVisibility(mViewModel.getWarning().canInstallAnyway() ? View.VISIBLE : View.GONE);
                    break;
                case ERROR:
                    viewSwitcher.setShownView(R.id.container_installerx_error);
                    getPositiveButton().setVisibility(View.VISIBLE);
                    break;
            }
            revealBottomSheet();
        });

        mViewModel.getMeta().observe(this, meta -> {
            adapter.setMeta(meta);
            revealBottomSheet();
        });

        view.requestFocus(); //TV fix
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        if (mViewModel.getState().getValue() == InstallerXDialogViewModel.State.LOADING)
            mViewModel.cancelParsing();
    }

    private void checkPermissionsAndPickFiles() {
        if (!PermissionsUtils.checkAndRequestStoragePermissions(this)) {
            mActionAfterGettingStoragePermissions = PICK_WITH_INTERNAL_FILEPICKER;
            return;
        }


        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.MULTI_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = Environment.getExternalStorageDirectory();
        properties.offset = new File(mHelper.getHomeDirectory());
        properties.extensions = new String[]{"zip", "apks", "xapk", "apk", "apkm"};
        properties.sortBy = mHelper.getFilePickerSortBy();
        properties.sortOrder = mHelper.getFilePickerSortOrder();

        FilePickerDialogFragment.newInstance(null, getString(R.string.installer_pick_apks), properties).show(getChildFragmentManager(), "dialog_files_picker");
    }

    private void pickFilesWithSaf(boolean ignorePermissions) {
        if (Utils.apiIsAtLeast(30) && !ignorePermissions) {
            if (requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                SimpleAlertDialogFragment.newInstance(requireContext(), R.string.warning, R.string.installerx_thank_you_scoped_storage_very_cool).show(getChildFragmentManager(), DIALOG_TAG_Q_SAF_WARNING);
                return;
            }
        }

        Intent getContentIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getContentIntent.setType("*/*");
        getContentIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        getContentIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        getContentIntent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(getContentIntent, getString(R.string.installer_pick_apks)), REQUEST_CODE_GET_FILES);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionsUtils.REQUEST_CODE_STORAGE_PERMISSIONS) {
            boolean permissionsGranted = !(grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED);

            switch (mActionAfterGettingStoragePermissions) {
                case PICK_WITH_INTERNAL_FILEPICKER:
                    if (!permissionsGranted)
                        AlertsUtils.showAlert(this, R.string.error, R.string.permissions_required_storage);
                    else
                        checkPermissionsAndPickFiles();
                    break;
                case PICK_WITH_SAF:
                    pickFilesWithSaf(true);
                    break;
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GET_FILES) {
            if (resultCode != Activity.RESULT_OK || data == null)
                return;

            if (data.getData() != null) {
                mViewModel.setApkSourceUris(Collections.singletonList(data.getData()));
                return;
            }

            if (data.getClipData() != null) {
                ClipData clipData = data.getClipData();
                List<Uri> apkUris = new ArrayList<>(clipData.getItemCount());

                for (int i = 0; i < clipData.getItemCount(); i++)
                    apkUris.add(clipData.getItemAt(i).getUri());

                mViewModel.setApkSourceUris(apkUris);
            }
        }
    }

    @Override
    public void onFilesSelected(String tag, List<File> files) {
        mViewModel.setApkSourceFiles(files);
    }

    @Override
    public void onDialogDismissed(@NonNull String dialogTag) {
        switch (dialogTag) {
            case DIALOG_TAG_Q_SAF_WARNING:
                mActionAfterGettingStoragePermissions = PICK_WITH_SAF;
                if (PermissionsUtils.checkAndRequestStoragePermissions(this)) {
                    pickFilesWithSaf(false);
                }
                break;
        }
    }
}
