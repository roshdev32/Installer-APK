package com.installer.apkinstaller.installer2.base;

import com.installer.apkinstaller.installer2.base.model.SaiPiSessionState;

public interface SaiPiSessionObserver {

    void onSessionStateChanged(SaiPiSessionState state);

}
