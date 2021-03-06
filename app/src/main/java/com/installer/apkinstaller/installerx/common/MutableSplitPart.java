package com.installer.apkinstaller.installerx.common;

import androidx.annotation.Nullable;

import com.installer.apkinstaller.installerx.splitmeta.SplitMeta;

public class MutableSplitPart implements SplitPart {

    private SplitMeta mMeta;
    private String mId;
    private String mLocalPath;
    private String mName;
    private long mSize;
    private String mDescription;
    private boolean mRequired;
    private boolean mRecommended;

    /**
     * @param id id is equal to the entry name in an archive
     */
    public MutableSplitPart(SplitMeta meta, String id, String localPath, String name, long size, @Nullable String description, boolean required, boolean recommended) {
        mMeta = meta;
        mId = id;
        mLocalPath = localPath;
        mName = name;
        mSize = size;
        mDescription = description;
        mRequired = required;
        mRecommended = recommended;
    }

    public void setMeta(SplitMeta meta) {
        mMeta = meta;
    }

    @Override
    public SplitMeta meta() {
        return mMeta;
    }


    public void setId(String id) {
        mId = id;
    }

    @Override
    public String id() {
        return mId;
    }


    public void setLocalPath(String localPath) {
        mLocalPath = localPath;
    }

    /**
     * @return the local path of this part in the apk source
     */
    @Override
    public String localPath() {
        return mLocalPath;
    }

    public void setName(String name) {
        mName = name;
    }

    @Override
    public String name() {
        return mName;
    }

    @Override
    public long size() {
        return mSize;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    @Nullable
    @Override
    public String description() {
        return mDescription;
    }

    public void setRequired(boolean required) {
        mRequired = required;
    }

    @Override
    public boolean isRequired() {
        return mRequired;
    }

    public void setRecommended(boolean recommended) {
        mRecommended = recommended;
    }

    @Override
    public boolean isRecommended() {
        return mRecommended || mRequired;
    }

    public SealedSplitPart seal() {
        return new SealedSplitPart(meta(), id(), localPath(), name(), size(), description(), isRequired(), isRecommended());
    }
}
