package com.hand.document.operation;

public class TaskProgress {
    private static final String TAG = "TaskProgress";

    private float progress;
    private long max;

    public static TaskProgress obtain(float progress, long max) {
        TaskProgress taskProgress = new TaskProgress();
        taskProgress.progress = progress;
        taskProgress.max = max;
        return taskProgress;
    }

    public float getProgress() {
        return progress;
    }

    public long getMax() {
        return max;
    }
}
