package com.project.contacts;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;

public class TextChangeDelayedExecutor {
    private String current;
    private final OnTextChange onTextChange;
    private Disposable disposable = null;

    TextChangeDelayedExecutor(String current, OnTextChange onTextChange) {
        this.current = current;
        this.onTextChange = onTextChange;
    }

    public void update(String changedStr) {
        this.current = changedStr;
        if (disposable != null && !disposable.isDisposed()) disposable.dispose();
        disposable = Completable
                .timer(1000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(()-> onTextChange.textChanged(current));
    }

    public interface OnTextChange {
        void textChanged(String text);
    }
}
