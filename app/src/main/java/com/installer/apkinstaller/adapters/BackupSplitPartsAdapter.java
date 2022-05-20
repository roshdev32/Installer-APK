package com.installer.apkinstaller.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import installer.apk.xapk.apkinstaller.R;
import com.installer.apkinstaller.adapters.selection.SelectableAdapter;
import com.installer.apkinstaller.adapters.selection.Selection;
import com.installer.apkinstaller.model.backup.SplitApkPart;
import com.installer.apkinstaller.utils.Utils;

import java.util.List;

public class BackupSplitPartsAdapter extends SelectableAdapter<String, BackupSplitPartsAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<SplitApkPart> mParts;

    public BackupSplitPartsAdapter(Selection<String> selection, LifecycleOwner lifecycleOwner, Context c) {
        super(selection, lifecycleOwner);
        mContext = c;
        mInflater = LayoutInflater.from(c);
        setHasStableIds(true);
    }

    public void setData(List<SplitApkPart> data) {
        mParts = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_backup_dialog_split_part, parent, false));
    }

    @Override
    protected String getKeyForPosition(int position) {
        return mParts.get(position).toKey();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.bind(mParts.get(position));
    }

    @Override
    public int getItemCount() {
        return mParts == null ? 0 : mParts.size();
    }

    @Override
    public long getItemId(int position) {
        return mParts.get(position).getPath().hashCode();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mName;
        private TextView mSize;
        private TextView mPath;

        private CheckBox mCheck;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);

            mName = itemView.findViewById(R.id.tv_split_part_name);
            mSize = itemView.findViewById(R.id.tv_split_part_size);
            mPath = itemView.findViewById(R.id.tv_split_part_path);

            mCheck = itemView.findViewById(R.id.check_split_apk_part);

            itemView.setOnClickListener((v) -> {
                int adapterPosition = getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION)
                    return;

                SplitApkPart item = mParts.get(adapterPosition);
                boolean selected = switchSelection(item.toKey());
                mCheck.setChecked(selected);
            });
        }

        private void bind(SplitApkPart part) {
            mName.setText(part.getName());
            mSize.setText(Utils.formatSize(mContext, part.getSize()));
            mPath.setText(part.getPath().getName());

            mCheck.setChecked(isSelected(part.toKey()));

        }
    }

}
