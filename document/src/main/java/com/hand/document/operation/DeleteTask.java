package com.hand.document.operation;

import android.content.ContentResolver;
import android.content.Context;
import com.hand.document.R;
import com.hand.document.core.DocumentInfo;
import com.hand.document.provider.DocumentsContract;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class DeleteTask extends BaseTask {

    private ArrayList<DocumentInfo> mSourceDocs;

    public DeleteTask(Context context, List docs) {
        super(context);
        mSourceDocs = new ArrayList<>();
        mSourceDocs.addAll(docs);
    }

    @Override
    protected TaskResult doInBackground(Void... voids) {
        super.doInBackground(voids);
        ContentResolver contentResolver = getContext().getContentResolver();
        ArrayList<DocumentInfo> failedList = new ArrayList<>();
        long max = mSourceDocs.size();
        long index = 0;
        for (DocumentInfo documentInfo : mSourceDocs) {
            if (isCancelled()) {
                return TaskResult.obtain(TaskResult.STATE_CANCEL, getContext().getString(R.string.task_result_cancel));
            }
            try {
                boolean result = DocumentsContract.deleteDocument(contentResolver, documentInfo.derivedUri);
                if (!result) {
                    failedList.add(documentInfo);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                failedList.add(documentInfo);
            }
            ++index;
            float progress = index / max;
            publishProgress(TaskProgress.obtain(progress, max));
        }

        if (failedList.size() > 0) {
            if (failedList.size() == max) {
                return TaskResult.obtain(TaskResult.STATE_FAIL, getContext().getString(R.string.task_result_fail));
            }
            return TaskResult.obtain(TaskResult.STATE_PART_FAIL, getContext().getString(R.string.task_result_part_fail));
        } else {
            return TaskResult.obtain(TaskResult.STATE_SUCCESS, getContext().getString(R.string.task_result_success));
        }
    }

    @Override
    protected Task generateTask() {
        return Task.obtain(Task.OPERATION_DELETE, mSourceDocs.size());
    }

}
