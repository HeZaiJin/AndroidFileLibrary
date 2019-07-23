package com.hand.document.operation.widget;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hand.document.R;
import com.hand.document.operation.Task;

public class TaskProgressDialog extends BottomSheetDialog implements View.OnClickListener {
    private static final String TAG = "TaskProgressDialog";

    private TextView mTitle;
    private ProgressBar mProgressBar;
    private TextView mDesc;
    private Button mCancel;

    public TaskProgressDialog(@NonNull Context context) {
        this(context, 0);
    }

    public TaskProgressDialog(@NonNull Context context, int theme) {
        super(context, theme);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_bottom_progress_layout);
        mTitle = findViewById(R.id.title);
        mProgressBar = findViewById(R.id.progress);
        mDesc = findViewById(R.id.desc);
        mCancel = findViewById(R.id.cancel);
        mCancel.setClickable(false);
    }

    public void show(Task task) {
        mTitle.setText(task.getTitle());
        mDesc.setText(getContext().getResources().getString(R.string.task_progress_desc, 0, task.getCount()));
        show();
        mCancel.setClickable(true);
    }

    public void udpate(Object obj) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.cancel) {

        }
    }

}
