package com.installer.apkinstaller.backup2.impl.components;

import com.installer.apkinstaller.backup2.BackupComponent;

public class SimpleBackupComponent implements BackupComponent {

    private String mType;
    private long mSize;

    public SimpleBackupComponent(String type, long size) {
        mType = type;
        mSize = size;
    }

    @Override
    public String type() {
        return mType;
    }

    @Override
    public long size() {
        return mSize;
    }
}
