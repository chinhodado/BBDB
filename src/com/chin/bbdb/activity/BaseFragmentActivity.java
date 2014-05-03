package com.chin.bbdb.activity;

import com.chin.bbdb.R;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * A base class for a FragmentActivity with a navigation drawer, Google Analytics and ads
 * @author Chin
 *
 */
public class BaseFragmentActivity extends FragmentActivity{
    ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setContentView based on the actual class of this object
        // not sure if this is the right approach. It is ugly, obviously, but it works
        // TODO: change to use the abstract method approach
        if (this instanceof MainActivity) {
            setContentView(R.layout.activity_main);
        }
        else if (this instanceof FamCompareActivity) {
            setContentView(R.layout.activity_fam_compare);
        }
        else if (this instanceof FamDetailActivity) {
            setContentView(R.layout.activity_fam_detail);
        }
        else if (this instanceof HelpAboutActivity) {
            setContentView(R.layout.activity_help);
        }
        else if (this instanceof TierTableActivity) {
            setContentView(R.layout.activity_tier_table);
        }
        else if (this instanceof BuildBrigActivity) {
            setContentView(R.layout.activity_build_brig);
        }

        // create the navigation drawer
        String[] mListTitles = {"Familiar", "Tier lists", "Build brigade"};
        final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerList.setAdapter(new ArrayAdapter<String>(this, // set the adapter for the list view
                android.R.layout.simple_list_item_1, mListTitles));

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                Intent intent = null;

                // first just close the drawer
                mDrawerLayout.closeDrawers();

                if (position == 0) { // Familiar
                    intent = new Intent(v.getContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
                else if (position == 1) { // Tier list
                    intent = new Intent(v.getContext(), TierTableActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
                else if (position == 2) { // Build brigade
                    intent = new Intent(v.getContext(), BuildBrigActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
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
            {
                Intent intent = new Intent(this, HelpAboutActivity.class);
                intent.putExtra("INTENT", "help");
                startActivity(intent);
                break;
            }
            case R.id.action_about:
            {
                Intent intent = new Intent(this, HelpAboutActivity.class);
                intent.putExtra("INTENT", "about");
                startActivity(intent);
                break;
            }
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
