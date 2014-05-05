package com.chin.bbdb.activity;

import com.chin.bbdb.asyncTask.AddComparisonInfoTask;
import android.content.Intent;
import android.os.Bundle;

/**
 * Activity to compare two familiars
 */
public class FamCompareActivity extends BaseFragmentActivity {

    AddComparisonInfoTask myTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        myTask = (AddComparisonInfoTask) new AddComparisonInfoTask(this)
            .execute(intent.getStringExtra("FAM_NAME_LEFT"),
                     intent.getStringExtra("FAM_NAME_RIGHT"));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (myTask != null) {
            myTask.cancel(true);
            myTask = null;
        }
    }
}
