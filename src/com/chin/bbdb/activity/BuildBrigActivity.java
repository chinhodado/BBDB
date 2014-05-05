package com.chin.bbdb.activity;

import com.chin.bbdb.FamStore;
import com.chin.bbdb.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
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
    static int currentFormationIndex = 0;
    static final IntPair[][] formationTypes = {
        {new IntPair(1, 3), new IntPair(2, 2), new IntPair(3, 1), new IntPair(4, 2), new IntPair(5, 3)}, // 5-skein
        {new IntPair(1, 1), new IntPair(2, 2), new IntPair(3, 3), new IntPair(4, 2), new IntPair(5, 1)}, // 5-valley
        {new IntPair(1, 1), new IntPair(2, 3), new IntPair(3, 1), new IntPair(4, 3), new IntPair(5, 1)}, // 5-tooth
        {new IntPair(1, 3), new IntPair(2, 1), new IntPair(3, 2), new IntPair(4, 1), new IntPair(5, 3)}, // 5-wave
        {new IntPair(1, 1), new IntPair(2, 1), new IntPair(3, 1), new IntPair(4, 1), new IntPair(5, 1)}, // 5-front
        {new IntPair(1, 2), new IntPair(2, 2), new IntPair(3, 2), new IntPair(4, 2), new IntPair(5, 2)}, // 5-mid
        {new IntPair(1, 3), new IntPair(2, 3), new IntPair(3, 3), new IntPair(4, 3), new IntPair(5, 3)}, // 5-rear
        {new IntPair(1, 3), new IntPair(2, 3), new IntPair(3, 1), new IntPair(4, 3), new IntPair(5, 3)}, // 5-pike
        {new IntPair(1, 1), new IntPair(2, 1), new IntPair(3, 3), new IntPair(4, 1), new IntPair(5, 1)}, // 5-shield
    };

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
        final LinearLayout brigLayout = (LinearLayout) findViewById(R.id.brig);
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

        LinearLayout formationWrapper = (LinearLayout) findViewById(R.id.formation);
        final MyLinearLayout formation = new MyLinearLayout(this);
        formation.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        formation.setBackgroundColor(Color.parseColor("#00ffffff"));
        formationWrapper.addView(formation);

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

        ImageView nextFormation = (ImageView) findViewById(R.id.imageView_next_formation);
        ImageView prevFormation = (ImageView) findViewById(R.id.imageView_prev_formation);

        nextFormation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentFormationIndex >= 8) {
                    return;
                }
                currentFormationIndex++;
                formation.invalidate();
            }
        });

        prevFormation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentFormationIndex <= 0) {
                    return;
                }
                currentFormationIndex--;
                formation.invalidate();
            }
        });
    }

    /**
     * Just a pair of integer. Nothing fancy.
     * @author Chin
     *
     */
    public static class IntPair {
        public int x, y;
        public IntPair (int x, int y){
            this.x = x;
            this.y = y;
        }
    }

    /**
     * A custom view that we can draw on
     * @author Chin
     *
     */
    public static class MyLinearLayout extends LinearLayout {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public MyLinearLayout(Context context) {
            super(context);
            paint.setColor(0xFF000000);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            IntPair[] formationType = formationTypes[currentFormationIndex];

            int horizontalStep = getWidth() / 10;
            int verticalStep = getHeight() / 6;

            for (int i = 0; i < 4; i++) {
                int bullet1X = (formationType[i].x * 2 - 1) * horizontalStep;
                int bullet1Y = (formationType[i].y * 2 - 1) * verticalStep - 1;

                int bullet2X = (formationType[i + 1].x * 2 - 1) * horizontalStep;
                int bullet2Y = (formationType[i + 1].y * 2 - 1) * verticalStep;

                canvas.drawLine(bullet1X, bullet1Y, bullet2X, bullet2Y, paint);
                canvas.drawCircle(bullet1X, bullet1Y, 10, paint);
                canvas.drawCircle(bullet2X, bullet2Y, 10, paint);
            }
        }
    }
}
