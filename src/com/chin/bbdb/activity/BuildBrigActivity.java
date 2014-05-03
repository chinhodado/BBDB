package com.chin.bbdb.activity;

import com.chin.bbdb.FamStore;
import com.chin.bbdb.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Activity for building brig
 */
public class BuildBrigActivity extends BaseFragmentActivity {

    static String currentSelectedTag = "imgView_build_brig_0_0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // build our brig
        // calculate the width of the images to be displayed later on
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int scaleWidth = screenWidth / 5; // set it to be 1/6 of the screen width

        final LinearLayout mainLayout = (LinearLayout) findViewById(R.id.linearLayout_main_build_brig);
        final AutoCompleteTextView atv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_build_brig);
        final ImageView[][] imgViewArray = new ImageView[3][5];
        RelativeLayout brigLayout = new RelativeLayout(this);
        brigLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mainLayout.addView(brigLayout);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 5; j++) {
                ImageView imgView = new ImageView(this);
                RelativeLayout.LayoutParams layoutParams =
                        new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins((int) (0.75 * scaleWidth*j), (int) (0.4 * scaleWidth * i), 0, 0);
                imgView.setLayoutParams(layoutParams);
                brigLayout.addView(imgView);
                imgViewArray[i][j] = imgView;

                imgView.setFocusableInTouchMode(true);
                imgView.getLayoutParams().width = scaleWidth;
                imgView.getLayoutParams().height = (int) (scaleWidth*1.5);
                imgView.requestLayout();
                //imgView.setImageResource(R.drawable.ic_action_search);
                imgView.setTag("imgView_build_brig_" + i + "_" + j);

                imgView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentSelectedTag = (String) v.getTag();
                        atv.setText("");
                    }
                });

                imgView.setOnFocusChangeListener(new OnFocusChangeListener() {

                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            // a hack, since with setFocusableInTouchMode set to true, the view will need
                            // 2 clicks to fire the onClick event
                            v.performClick();

                            // clear the bg
                            for (int i = 0; i < 3; i++) {
                                for (int j = 0; j < 5; j++) {
                                    imgViewArray[i][j].setBackgroundColor(0x00000000);
                                }
                            }

                            v.setBackgroundColor(Color.argb(70, 192, 192, 192));

                            // set focus to the atv
                            atv.requestFocus();
                        }
                    }
                });
            }
        }

        atv.setAdapter(MainActivity.adapter);
        atv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String famName = (String)parent.getItemAtPosition(position);
                final ImageView imgView1 = (ImageView) mainLayout.findViewWithTag(currentSelectedTag);

                new AsyncTask<Void, Void, Void>(){
                    @Override
                    protected Void doInBackground(Void... params) {
                        FamStore.getInstance().getGeneralInfo(famName);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void param) {
                        ImageLoader.getInstance()
                        .displayImage(FamStore.getInstance()
                                                .getImageLink(famName), imgView1);
                    }
                }.execute();
            }
        });
    }
}
