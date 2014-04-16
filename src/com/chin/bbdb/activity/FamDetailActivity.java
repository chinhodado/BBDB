package com.chin.bbdb.activity;

import java.util.HashMap;

import com.chin.bbdb.R;
import com.chin.bbdb.asyncTask.AddFamiliarInfoTask;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;

/**
 * Activity to show all details about a familiar
 */
public class FamDetailActivity extends Activity {

    // map a string (the evolution level) to a number that represents
    // the image for that evolution level
    public static HashMap<String, Integer> evolutionMap = null;

    ActionBarDrawerToggle mDrawerToggle;
    public String famName = null;

    // the last fam ever displayed in this activity (before going to comparison)
    public static String lastFam = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fam_detail);

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

        // Look up the AdView as a resource and load a request.
        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        if (savedInstanceState != null) {
            famName = savedInstanceState.getString("FAMNAME");
            lastFam = famName;
        }
        else {
            Intent intent = getIntent(); // careful, this intent may not be the intent from MainActivity...
            String tmpName = intent.getStringExtra(MainActivity.FAM_NAME);
            if (tmpName != null) {
                famName = tmpName; // needed since we may come back from other activity, not just the main one
                lastFam = famName;
            }
        }

        // if at this point the famName is still null, we can assume that we're returning from FamCompareActivity
        if (famName == null) {
            famName = lastFam;
        }

        setTitle("");
        initialize();

        new AddFamiliarInfoTask(this).execute(famName);

        // add the AutoCompleteTextView to the ActionBar
        LayoutInflater inflator = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.actionbar_layout, null);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(v);

        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.compareAutoCompleteTextView);
        autoCompleteTextView.setAdapter(MainActivity.adapter);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String famName = (String)parent.getItemAtPosition(position);
                Intent intent = new Intent(view.getContext(), FamCompareActivity.class);
                intent.putExtra("FAM_NAME_RIGHT", famName);
                intent.putExtra("FAM_NAME_LEFT", FamDetailActivity.this.famName);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putString("FAMNAME", famName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_help:
                Intent intent = new Intent(this, HelpActivity.class);
                startActivity(intent);
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

    /**
     * Some one-time initialization will be done here
     */
    public void initialize() {
        if (evolutionMap == null) {
            evolutionMap = new HashMap<String, Integer>();
            evolutionMap.put("Common", R.drawable.common);
            evolutionMap.put("Uncommon", R.drawable.uncommon);
            evolutionMap.put("Rare", R.drawable.rare);
            evolutionMap.put("Epic", R.drawable.epic);
            evolutionMap.put("Legendary", R.drawable.legend);
            evolutionMap.put("Mythic", R.drawable.mythic);

            evolutionMap.put("1of1", R.drawable.star11);
            evolutionMap.put("1of2", R.drawable.star12);
            evolutionMap.put("2of2", R.drawable.star22);
            evolutionMap.put("1of3", R.drawable.star13);
            evolutionMap.put("2of3", R.drawable.star23);
            evolutionMap.put("3of3", R.drawable.star33);
            evolutionMap.put("1of4", R.drawable.star14);
            evolutionMap.put("2of4", R.drawable.star24);
            evolutionMap.put("3of4", R.drawable.star34);
            evolutionMap.put("4of4", R.drawable.star44);
        }
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
