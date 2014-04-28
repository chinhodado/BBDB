package com.chin.bbdb.activity;

import com.chin.bbdb.FamStore;
import com.chin.bbdb.R;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;

/**
 * Activity for building brig
 */
public class BuildBrigActivity extends Activity {

    ActionBarDrawerToggle mDrawerToggle;
    static String currentSelectedTag = "imgView_build_brig_0_0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_brig);

        // create the navigation drawer
        String[] mListTitles = {"Familiar", "Tier lists"};
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerList.setAdapter(new ArrayAdapter<String>(this, // set the adapter for the list view
                android.R.layout.simple_list_item_1, mListTitles));

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                Intent intent = null;
                if (position == 0) { // Familiar
                    intent = new Intent(v.getContext(), MainActivity.class);
                    startActivity(intent);
                }
                else if (position == 1) { // Tier list
                    intent = new Intent(v.getContext(), TierTableActivity.class);
                    startActivity(intent);
                }
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
                ) {

            /** Called when a drawer has settled in a completely closed state. */
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getActionBar().setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getActionBar().setTitle(mDrawerTitle);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);

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

        // Look up the AdView as a resource and load a request.
        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.help, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
          return true;
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Google Analytics
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Google Analytics
        EasyTracker.getInstance(this).activityStop(this);
    }
}
