package com.al.ai.service;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskRunner {

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public interface Complete<RS> {
        void complete(RS result);
    }

    public interface Prepare<R> {
        R prepare();
    }

    public interface Process<R, RS> {
        RS process(R request);
    }

    public <R, RS> void executeAsync(Prepare<R> before, Process<R, RS> process, Complete<RS> callback) {
        executor.execute(() -> {
            R request = before.prepare();
            RS result = process.process(request);
            handler.post(() -> callback.complete(result));
        });
    }
}
