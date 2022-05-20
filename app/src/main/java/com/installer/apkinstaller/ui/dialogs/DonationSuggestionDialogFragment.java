package com.installer.apkinstaller.ui.dialogs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import installer.apk.xapk.apkinstaller.R;
import com.installer.apkinstaller.billing.DefaultBillingManager;
import com.installer.apkinstaller.billing.DonationStatus;
import com.installer.apkinstaller.ui.activities.DonateActivity;
import com.installer.apkinstaller.ui.dialogs.base.BaseBottomSheetDialogFragment;

public class DonationSuggestionDialogFragment extends BaseBottomSheetDialogFragment {

    private static final String KEY_SHOWN = "donation_suggestion_dialog_shown";

    public static void showIfNeeded(Context context, FragmentManager fragmentManager) {
        if (DefaultBillingManager.getInstance(context).getDonationStatus().getValue() == DonationStatus.DONATED)
            return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.getBoolean(KEY_SHOWN, false))
            new DonationSuggestionDialogFragment().show(fragmentManager, null);
    }

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_donation_suggestion, container, false);
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onContentViewCreated(view, savedInstanceState);

        setCancelable(false);

        setTitle(R.string.donate_dialog_title);

        getNegativeButton().setText(R.string.donate_dialog_close);
        getNegativeButton().setOnClickListener(v -> setShownAndDismiss());

        getPositiveButton().setText(R.string.donate_dialog_go_to_donate_page);
        getPositiveButton().setOnClickListener(v -> {
            DonateActivity.start(requireContext());
            setShownAndDismiss();
        });
    }

    private void setShownAndDismiss() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        prefs.edit().putBoolean(KEY_SHOWN, true).apply();
        dismiss();
    }
}
