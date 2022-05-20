package com.installer.apkinstaller.billing;

public enum DonationStatus {
    UNKNOWN, PENDING, DONATED, NOT_DONATED, NOT_AVAILABLE;

    public boolean unlocksThemes() {
        return this == DONATED;
    }
}
