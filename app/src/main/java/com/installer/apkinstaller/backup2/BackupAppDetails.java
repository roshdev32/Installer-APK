package com.installer.apkinstaller.backup2;

import java.util.List;

public interface BackupAppDetails {

    State state();

    BackupApp app();

    List<Backup> backups();

    enum State {
        LOADING, READY, ERROR
    }

}
