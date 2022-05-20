package com.installer.apkinstaller.utils;

public interface Locker<T> {

    Object getLockFor(T t);

    default void withLock(T t, Runnable action) {
        synchronized (getLockFor(t)) {
            action.run();
        }
    }

    void clearLock(T t);

}
