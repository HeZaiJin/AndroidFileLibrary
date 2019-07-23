package com.hand.document.operation;

public class TaskResult {

    public static final int STATE_FAIL = -1;
    public static final int STATE_PART_FAIL = -2;
    public static final int STATE_CANCEL = 1001;
    public static final int STATE_SUCCESS = 1;

    private int state = STATE_FAIL;

    private String msg;

    public static TaskResult obtain(int state, String msg) {
        TaskResult result = new TaskResult();
        result.state = state;
        result.msg = msg;
        return result;
    }

    public int getState() {
        return state;
    }

    public String getMsg() {
        return msg;
    }
}
