package com.installer.apkinstaller.common;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.installer.apkinstaller.backup2.impl.db.BackupComponentEntity;
import com.installer.apkinstaller.backup2.impl.db.BackupDao;
import com.installer.apkinstaller.backup2.impl.db.BackupEntity;

@Database(entities = {BackupEntity.class, BackupComponentEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase sInstance;

    public synchronized static AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "room").build();
        }

        return sInstance;
    }


    public abstract BackupDao backupDao();
}
