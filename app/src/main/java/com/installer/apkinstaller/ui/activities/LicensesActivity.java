package com.installer.apkinstaller.ui.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import installer.apk.xapk.apkinstaller.R;
import com.installer.apkinstaller.adapters.LicensesAdapter;
import com.installer.apkinstaller.viewmodels.LicensesViewModel;

public class LicensesActivity extends ThemedActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);

        RecyclerView recyclerView = findViewById(R.id.rv_licenses);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 16);

        LicensesAdapter adapter = new LicensesAdapter(this);
        recyclerView.setAdapter(adapter);

        LicensesViewModel viewModel = new ViewModelProvider(this).get(LicensesViewModel.class);
        viewModel.getLicenses().observe(this, adapter::setLicenses);
    }
}
