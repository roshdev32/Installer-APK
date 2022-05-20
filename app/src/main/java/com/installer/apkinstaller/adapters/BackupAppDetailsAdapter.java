package com.installer.apkinstaller.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import installer.apk.xapk.apkinstaller.R;
import com.installer.apkinstaller.adapters.selection.SelectableAdapter;
import com.installer.apkinstaller.adapters.selection.Selection;
import com.installer.apkinstaller.backup2.Backup;
import com.installer.apkinstaller.backup2.BackupApp;
import com.installer.apkinstaller.backup2.BackupAppDetails;
import com.installer.apkinstaller.backup2.BackupStatus;
import com.installer.apkinstaller.model.common.PackageMeta;
import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BackupAppDetailsAdapter extends SelectableAdapter<String, BackupAppDetailsAdapter.BaseViewHolder> {
    private static final int VH_TYPE_HEADER = 0;
    private static final int VH_TYPE_BACKUP = 1;

    private Context mContext;
    private LayoutInflater mInflater;

    private BackupAppDetails mDetails;

    private ActionDelegate mActionDelegate;

    private SimpleDateFormat mBackupTimeSdf = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault());

    private RecyclerView.RecycledViewPool mComponentViewPool;

    public BackupAppDetailsAdapter(Context context, Selection<String> selection, LifecycleOwner lifecycleOwner, ActionDelegate actionDelegate) {
        super(selection, lifecycleOwner);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mActionDelegate = actionDelegate;

        mComponentViewPool = new RecyclerView.RecycledViewPool();
        mComponentViewPool.setMaxRecycledViews(0, 16);

        setHasStableIds(true);
    }

    public void setDetails(@Nullable BackupAppDetails details) {
        mDetails = details;
        notifyDataSetChanged();
    }

    private Backup getBackupForPosition(int position) {
        return mDetails.backups().get(position - 1);
    }

    @Override
    protected String getKeyForPosition(int position) {
        if (position == 0)
            return "BackupAppDetailsAdapter.Header";

        Backup backup = getBackupForPosition(position);

        return backup.uri() + "@" + backup.storageId();
    }

    @Override
    public long getItemId(int position) {
        return getKeyForPosition(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return VH_TYPE_HEADER;

        return VH_TYPE_BACKUP;
    }

    @NonNull
    @Override
    public BackupAppDetailsAdapter.BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VH_TYPE_HEADER:
                return new HeaderViewHolder(mInflater.inflate(R.layout.item_backup_app_details_header, parent, false));
            case VH_TYPE_BACKUP:
                return new BackupViewHolder(mInflater.inflate(R.layout.item_backup_app_details_backup, parent, false));
        }

        throw new IllegalArgumentException("Unknown viewType - " + viewType);
    }

    @Override
    public int getItemCount() {
        if (mDetails == null)
            return 0;

        return 1 + mDetails.backups().size();
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (position == 0)
            holder.bindTo(mDetails.app());
        else
            holder.bindTo(getBackupForPosition(position));
    }

    @Override
    public void onViewRecycled(@NonNull BaseViewHolder holder) {
        super.onViewRecycled(holder);
        holder.recycle();
    }

    protected static abstract class BaseViewHolder<T> extends RecyclerView.ViewHolder {

        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        protected abstract void bindTo(T t);

        protected void recycle() {

        }
    }

    protected class HeaderViewHolder extends BaseViewHolder<BackupApp> {

        private ImageView mAppIcon;
        private TextView mAppTitle;
        private TextView mAppPackage;
        private TextView mAppVersion;

        private Button mBackupButton;
        private Button mDeleteButton;
        private Button mInstallButton;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);

            mAppIcon = itemView.findViewById(R.id.iv_backup_app_details_header_app_icon);
            mAppTitle = itemView.findViewById(R.id.tv_backup_app_details_header_app_title);
            mAppPackage = itemView.findViewById(R.id.tv_backup_app_details_header_app_package);
            mAppVersion = itemView.findViewById(R.id.tv_backup_app_details_header_app_version);

            mBackupButton = itemView.findViewById(R.id.button_backup_app_details_backup);
            mDeleteButton = itemView.findViewById(R.id.button_backup_app_details_delete);
            mInstallButton = itemView.findViewById(R.id.button_backup_app_details_install);

            mBackupButton.setOnClickListener(v -> mActionDelegate.backupApp(mDetails.app()));
            mDeleteButton.setOnClickListener(v -> mActionDelegate.deleteApp(mDetails.app()));
            mInstallButton.setOnClickListener(v -> mActionDelegate.installApp(mDetails.app()));
        }

        @Override
        protected void bindTo(BackupApp app) {

            PackageMeta packageMeta = app.packageMeta();
            Glide.with(mAppIcon)
                    .load(packageMeta.iconUri != null ? packageMeta.iconUri : R.drawable.placeholder_app_icon)
                    .placeholder(R.drawable.placeholder_app_icon)
                    .into(mAppIcon);

            mAppTitle.setText(packageMeta.label != null ? packageMeta.label : packageMeta.packageName);
            if (!app.isInstalled()) {
                mAppTitle.setPaintFlags(mAppTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                mAppTitle.setPaintFlags(mAppTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            mAppVersion.setVisibility(packageMeta.versionName != null ? View.VISIBLE : View.GONE);
            mAppVersion.setText(packageMeta.versionName);
            mAppPackage.setText(packageMeta.packageName);

            boolean appInstalled = app.isInstalled();
            mBackupButton.setVisibility(appInstalled ? View.VISIBLE : View.GONE);
            mDeleteButton.setVisibility(appInstalled ? View.VISIBLE : View.GONE);
            mInstallButton.setVisibility(appInstalled ? View.GONE : View.VISIBLE);
        }

        @Override
        protected void recycle() {

            Glide.with(mAppIcon)
                    .clear(mAppIcon);
        }
    }

    protected class BackupViewHolder extends BaseViewHolder<Backup> {

        private TextView mBackupTitle;
        private TextView mAppVersion;
        private AppCompatImageView mBackupStatus;

        private Button mRestoreButton;
        private Button mDeleteButton;

        private TextView mIncompatibleVersionWarning;

        private BackupComponentsAdapter mComponentsAdapter;

        public BackupViewHolder(@NonNull View itemView) {
            super(itemView);

            mBackupTitle = itemView.findViewById(R.id.tv_backup_title);
            mAppVersion = itemView.findViewById(R.id.tv_app_version);
            mBackupStatus = itemView.findViewById(R.id.iv_backup_status);

            mRestoreButton = itemView.findViewById(R.id.button_backup_restore_backup);
            mDeleteButton = itemView.findViewById(R.id.button_backup_delete_backup);

            mIncompatibleVersionWarning = itemView.findViewById(R.id.tv_backup_incompatible_version_warning);

            mRestoreButton.setOnClickListener(v -> {
                int adapterPosition = getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION)
                    return;

                mActionDelegate.restoreBackup(getBackupForPosition(adapterPosition));
            });

            mDeleteButton.setOnClickListener(v -> {
                int adapterPosition = getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION)
                    return;

                mActionDelegate.deleteBackup(getBackupForPosition(adapterPosition));
            });

            RecyclerView componentsRecycler = itemView.findViewById(R.id.rv_backup_components);
            FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(itemView.getContext(), FlexDirection.ROW, FlexWrap.WRAP);
            layoutManager.setJustifyContent(JustifyContent.FLEX_START);
            componentsRecycler.setLayoutManager(layoutManager);
            componentsRecycler.setRecycledViewPool(mComponentViewPool);
            mComponentsAdapter = new BackupComponentsAdapter(mContext);
            componentsRecycler.setAdapter(mComponentsAdapter);
        }

        @Override
        protected void bindTo(Backup backup) {
            mAppVersion.setText(mContext.getString(R.string.backup_app_details_backup_version, backup.versionName()));
            mBackupTitle.setText(mContext.getString(R.string.backup_app_details_backup_time, mBackupTimeSdf.format(new Date(backup.creationTime()))));

            BackupStatus backupStatus;
            if (mDetails.app().isInstalled()) {
                backupStatus = BackupStatus.fromInstalledAppAndBackupVersions(mDetails.app().packageMeta().versionCode, backup.versionCode());
            } else {
                backupStatus = BackupStatus.APP_NOT_INSTALLED;
            }
            mBackupStatus.setImageResource(backupStatus.getIconRes());
            mRestoreButton.setVisibility(backupStatus.canBeInstalledOverExistingApp() ? View.VISIBLE : View.GONE);
            mIncompatibleVersionWarning.setVisibility(backupStatus.canBeInstalledOverExistingApp() ? View.GONE : View.VISIBLE);

            mComponentsAdapter.setComponents(backup.components());
        }

        @Override
        protected void recycle() {
            mComponentsAdapter.setComponents(null);
        }
    }

    public interface ActionDelegate {

        void backupApp(BackupApp backupApp);

        void deleteApp(BackupApp backupApp);

        void installApp(BackupApp backupApp);

        void restoreBackup(Backup backup);

        void deleteBackup(Backup backup);

    }

}
