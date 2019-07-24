package com.hand.document.operation;

public class TaskProgress {
    private static final String TAG = "TaskProgress";

    private long progress;
    private float percent;
    private long max;

    public static TaskProgress obtain(long progress, long max) {
        TaskProgress taskProgress = new TaskProgress();
        taskProgress.progress = progress;
        taskProgress.max = max;
        taskProgress.percent = progress * 1.0f / max;
        return taskProgress;
    }

    public long getProgress() {
        return progress;
    }

    public float getPercent() {
        return percent;
    }

    public long getMax() {
        return max;
    }
}
