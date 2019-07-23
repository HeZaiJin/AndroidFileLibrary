package com.hand.document.operation;

import com.hand.document.R;

public class Task {

    public static final String OPERATION_DELETE = "operation_delete";
    public static final String OPERATION_MOVE = "operation_move";
    public static final String OPERATION_COPY = "operation_copy";

    private String mOperation;

    private long mCount;
    private long mSize;

    public static Task obtain(String operation, long count) {
        Task task = new Task();
        task.mOperation = operation;
        task.mCount = count;
        return task;
    }


    public int getTitle() {
        switch (mOperation) {
            case OPERATION_DELETE:
                return R.string.task_delete;
            case OPERATION_MOVE:
                return R.string.task_move;
            case OPERATION_COPY:
                return R.string.task_copy;
            default:
                break;
        }
        throw new IllegalArgumentException("operation [" + mOperation + "] not support");
    }

    public long getCount() {
        return mCount;
    }
}
