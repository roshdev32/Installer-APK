package com.installer.apkinstaller.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import installer.apk.xapk.apkinstaller.R;
import com.installer.apkinstaller.installer2.base.model.SaiPiSessionState;
import com.installer.apkinstaller.model.common.PackageMeta;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.List;

public class SaiPiSessionsAdapter extends RecyclerView.Adapter<SaiPiSessionsAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater mInflater;

    private List<SaiPiSessionState> mSessions;

    private ActionDelegate mActionDelegate;

    public SaiPiSessionsAdapter(Context c) {
        mContext = c;
        mInflater = LayoutInflater.from(c);
        setHasStableIds(true);
    }

    public void setData(List<SaiPiSessionState> data) {
        mSessions = data;
        notifyDataSetChanged();
    }

    public void setActionsDelegate(ActionDelegate delegate) {
        mActionDelegate = delegate;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_installer_session, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(mSessions.get(position));
    }

    @Override
    public long getItemId(int position) {
        return mSessions.get(position).sessionId().hashCode();
    }

    @Override
    public int getItemCount() {
        return mSessions == null ? 0 : mSessions.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.recycle();
    }

    private void launchApp(String packageName) {
        if (mActionDelegate != null)
            mActionDelegate.launchApp(packageName);
    }

    private void showException(String shortError, String fullError) {
        if (mActionDelegate != null)
            mActionDelegate.showError(shortError, fullError);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ViewGroup mContainer;
        private ShimmerFrameLayout mShimmer;
        private TextView mName;
        private TextView mStatus;
        private ImageView mAppIcon;
        private ImageView mActionIcon;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);

            mContainer = itemView.findViewById(R.id.container_item_installer_session);
            mShimmer = itemView.findViewById(R.id.shimmer_item_installer_session);
            mName = itemView.findViewById(R.id.tv_session_name);
            mStatus = itemView.findViewById(R.id.tv_session_status);
            mAppIcon = itemView.findViewById(R.id.iv_app_icon);
            mActionIcon = itemView.findViewById(R.id.iv_installed_app_action);

            mContainer.setOnClickListener((v) -> {
                int adapterPosition = getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION)
                    return;

                SaiPiSessionState state = mSessions.get(adapterPosition);
                switch (state.status()) {
                    case INSTALLATION_SUCCEED:
                        launchApp(state.packageName());
                        break;
                    case INSTALLATION_FAILED:
                        showException(state.shortError(), state.fullError());
                        break;
                }
            });
        }

        private void bindTo(SaiPiSessionState state) {
            PackageMeta packageMeta = state.packageMeta();
            if (packageMeta != null) {
                mName.setText(packageMeta.label);
            } else if (state.appTempName() != null) {
                mName.setText(state.appTempName());
            } else {
                mName.setText(mContext.getString(R.string.installer_unknown_app));
            }

            if (packageMeta != null) {
                Glide.with(mAppIcon)
                        .load(packageMeta.iconUri != null ? packageMeta.iconUri : R.drawable.placeholder_app_icon)
                        .placeholder(R.drawable.placeholder_app_icon)
                        .into(mAppIcon);
            } else {
                Glide.with(mAppIcon)
                        .load(R.drawable.placeholder_app_icon)
                        .placeholder(R.drawable.placeholder_app_icon)
                        .into(mAppIcon);
            }

            mStatus.setText(state.status().getReadableName(mContext));

            switch (state.status()) {
                case INSTALLATION_SUCCEED:
                    mActionIcon.setImageResource(R.drawable.ic_launch);
                    mActionIcon.setVisibility(state.packageName() != null ? View.VISIBLE : View.GONE);
                    mContainer.setEnabled(state.packageName() != null);

                    mShimmer.hideShimmer();
                    break;
                case INSTALLATION_FAILED:
                    mActionIcon.setImageResource(R.drawable.ic_error);
                    mActionIcon.setVisibility(state.shortError() != null ? View.VISIBLE : View.GONE);
                    mContainer.setEnabled(state.shortError() != null);

                    mShimmer.hideShimmer();
                    break;
                default:
                    mActionIcon.setVisibility(View.GONE);
                    mContainer.setEnabled(false);

                    mShimmer.showShimmer(true);
                    mShimmer.startShimmer(); //for some reason it doesn't start via showShimmer(true)
                    break;
            }
        }

        private void recycle() {
            Glide.with(mAppIcon).clear(mAppIcon);
        }
    }

    public interface ActionDelegate {

        void launchApp(String packageName);

        void showError(String shortError, String fullError);
    }
}
