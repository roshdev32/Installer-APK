package com.installer.apkinstaller.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.installer.apkinstaller.utils.MiuiUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MiEntryViewModel extends AndroidViewModel {
    private static final int CONTINUE_DELAY_SECONDS = 30;

    private ScheduledFuture mTimerScheduledFuture;

    private int mCountdown = CONTINUE_DELAY_SECONDS;
    private MutableLiveData<Integer> mCountdownLiveData = new MutableLiveData<>(CONTINUE_DELAY_SECONDS);

    private AtomicBoolean mPaused = new AtomicBoolean(false);

    public MiEntryViewModel(@NonNull Application application) {
        super(application);

        if (!MiuiUtils.isFixedMiui()) {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            mTimerScheduledFuture = scheduler.scheduleAtFixedRate(() -> {
                if (mPaused.get())
                    return;

                mCountdown--;
                mCountdownLiveData.postValue(mCountdown);

                if (mCountdown == 0)
                    mTimerScheduledFuture.cancel(false);

            }, 0, 1, TimeUnit.SECONDS);
        } else {
            mCountdown = 0;
            mCountdownLiveData.setValue(mCountdown);
        }
    }

    public LiveData<Integer> getCountdown() {
        return mCountdownLiveData;
    }

    public void setPaused(boolean paused) {
        mPaused.set(paused);
    }

    @Override
    protected void onCleared() {
        if (mTimerScheduledFuture != null)
            mTimerScheduledFuture.cancel(false);
    }
}
