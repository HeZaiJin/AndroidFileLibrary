package com.hand.document.operation;

import android.content.Context;
import android.os.AsyncTask;
import com.hand.document.operation.widget.TaskProgressDialog;

public abstract class BaseTask extends AsyncTask<Void, TaskProgress, TaskResult> {

    private Context mContext;
    private Observer mObserver;
    private TaskProgressDialog mProgressDialog;

    public BaseTask(Context context) {
        mContext = context;
    }

    @Override
    protected TaskResult doInBackground(Void... voids) {
        Task task = generateTask();
        if (null != mObserver) {
            mObserver.onTaskStart(task);
        }

        if (showProgressDialog()) {
            mProgressDialog = new TaskProgressDialog(mContext);
        }

        return null;
    }

    protected abstract Task generateTask();

    public boolean showProgressDialog() {
        return true;
    }

    protected Context getContext() {
        return mContext;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(TaskResult taskResult) {
        if (null != mObserver) {
            mObserver.onTaskComplete(taskResult);
        }
        release();
    }

    protected void release() {
        mObserver = null;
        mContext = null;
    }

    @Override
    protected void onProgressUpdate(TaskProgress... values) {
        if (null != mObserver) {
            mObserver.onTaskProgress(values[0]);
        }
    }

    public void run(Observer observer) {
        mObserver = observer;
        execute();
    }

    public interface Observer {

        void onTaskStart(Task task);

        void onTaskProgress(TaskProgress progress);

        void onTaskComplete(TaskResult result);
    }

}
