package com.chin.bbdb.activity;

import com.chin.bbdb.FamStore;
import com.chin.bbdb.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Activity for building brig
 */
public class BuildBrigActivity extends BaseFragmentActivity {

    static String currentSelectedTag = "imgView_build_brig_0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // build our brig
        // calculate the width of the images to be displayed later on
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int scaleWidth = screenWidth / 6; // set it to be 1/6 of the screen width

        final LinearLayout mainLayout = (LinearLayout) findViewById(R.id.linearLayout_main_build_brig);
        final AutoCompleteTextView atv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_build_brig);
        final LinearLayout brigLayout = (LinearLayout) findViewById(R.id.brig_layout_wrapper);
        final ImageView[] imgViewArray = {
                (ImageView) findViewById(R.id.fam0), (ImageView) findViewById(R.id.fam1), (ImageView) findViewById(R.id.fam2),
                (ImageView) findViewById(R.id.fam3), (ImageView) findViewById(R.id.fam4), (ImageView) findViewById(R.id.fam5),
                (ImageView) findViewById(R.id.fam6), (ImageView) findViewById(R.id.fam7), (ImageView) findViewById(R.id.fam8),
                (ImageView) findViewById(R.id.fam9)
        };

        for (int i = 0; i < 10; i++) {
            ImageView imgView = imgViewArray[i]; // for convenience

            imgView.setFocusableInTouchMode(true);
            imgView.getLayoutParams().width = scaleWidth;
            imgView.getLayoutParams().height = (int) (scaleWidth*1.5);
            imgView.requestLayout();
            //imgView.setImageResource(R.drawable.ic_action_search);
            imgView.setTag("imgView_build_brig_" + i);

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
                        for (int i = 0; i < 10; i++) {
                            imgViewArray[i].setBackgroundColor(0x00000000);
                        }

                        v.setBackgroundColor(Color.argb(70, 192, 192, 192));

                        // set focus to the atv
                        atv.requestFocus();
                    }
                }
            });
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

        Button saveImgButton = (Button) findViewById(R.id.button1);
        saveImgButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // clear the bg
                for (int i = 0; i < 10; i++) {
                    imgViewArray[i].setBackgroundColor(0x00000000);
                }

                brigLayout.setBackgroundColor(Color.parseColor("#f3f3f3"));

                brigLayout.setDrawingCacheEnabled(true);
                Bitmap bitmap = brigLayout.getDrawingCache();
                try {
                    MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "foo.png" , "bar");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                brigLayout.setBackgroundColor(Color.parseColor("#00ffffff"));
            }
        });
    }
}
