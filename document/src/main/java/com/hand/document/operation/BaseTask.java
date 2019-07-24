package com.hand.document.operation;

import android.content.Context;
import android.os.AsyncTask;
import com.hand.document.operation.widget.TaskProgressDialog;

/**
 * @author hand
 */
public abstract class BaseTask extends AsyncTask<Void, TaskProgress, TaskResult> implements CancelAble{

    private Context mContext;
    private Observer mObserver;
    private TaskProgressDialog mProgressDialog;

    public BaseTask(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        Task task = generateTask();
        if (null != mObserver) {
            mObserver.onTaskStart(task);
        }

        if (showProgressDialog()) {
            mProgressDialog = new TaskProgressDialog(mContext);
            mProgressDialog.show(task, this);
        }
    }

    @Override
    protected TaskResult doInBackground(Void... voids) {

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
        if (null != mProgressDialog) {
            mProgressDialog.dismiss();
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
        if (null != mProgressDialog) {
            mProgressDialog.update(values[0]);
        }
    }

    @Override
    protected void onCancelled(TaskResult result) {
        super.onCancelled(result);
        if (null != mObserver) {
            mObserver.onTaskComplete(result);
        }
        if (null != mProgressDialog) {
            mProgressDialog.dismiss();
        }
        release();
    }

    @Override
    public void cancel() {
        cancel(true);
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
