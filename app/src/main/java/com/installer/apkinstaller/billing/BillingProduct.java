package com.installer.apkinstaller.billing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface BillingProduct {

    @NonNull
    String getTitle();

    @Nullable
    String getDescription();

    @Nullable
    String getIconUrl();

    @NonNull
    String getPrice();

    @NonNull
    String getId();

    boolean isPurchased();

}
