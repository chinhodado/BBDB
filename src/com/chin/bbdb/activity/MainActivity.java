package com.chin.bbdb.activity;

import java.util.ArrayList;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONObject;

import com.chin.bbdb.FamStore;
import com.chin.bbdb.NetworkDialogFragment;
import com.chin.bbdb.R;
import com.chin.bbdb.RegexFilterArrayAdapter;
import com.chin.bbdb.TabListener;
import com.chin.bbdb.asyncTask.NetworkTask;
import com.chin.bbdb.asyncTask.NewFamTask;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * The main activity, entry point of the app. It consists of two main view, the familiar search
 * list and the new famliars list
 */
public class MainActivity extends FragmentActivity {

    static boolean hasJustBeenStarted = true; // flag to determine if the app has just been started
    public final static String FAM_LINK = "com.chin.BBDB.LINK";
    public final static String FAM_NAME = "com.chin.BBDB.NAME";

    ActionBarDrawerToggle mDrawerToggle;

    public static RegexFilterArrayAdapter<String> adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

                // first just close the drawer
                DrawerLayout mDrawerLayout = (DrawerLayout) MainActivity.this.findViewById(R.id.drawer_layout);
                mDrawerLayout.closeDrawers();

                if (position == 0) { // Familiar
                    // since we're in this activity already, do nothing
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

        // if the app has just been started, show the drawer
        if (hasJustBeenStarted) {
            mDrawerLayout.openDrawer(mDrawerList);
        }

        getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setHomeButtonEnabled(true);

        // get the familiar list and their wiki url
        if (FamStore.famList == null) {
            try {
                // this will return up to 5000 articles in the Familiar category. Note that this is not always up-to-date,
                // as newly added articles may take a day or two before showing up in here
                String url = "http://bloodbrothersgame.wikia.com/api/v1/Articles/List?category=Familiars&limit=5000&namespaces=0";
                String jsonString = new NetworkTask().execute(url).get();
                JSONObject myJSON = new JSONObject(jsonString);

                FamStore.famList = new ArrayList<String>();
                FamStore.famLinkTable = new Hashtable<String, String[]>();

                JSONArray myArray = myJSON.getJSONArray("items");
                for (int i = 0; i < myArray.length(); i++) {
                    String famName = myArray.getJSONObject(i).getString("title");
                    FamStore.famList.add(famName);
                    String[] tmp = {myArray.getJSONObject(i).getString("url"),
                                    myArray.getJSONObject(i).getString("id")};
                    FamStore.famLinkTable.put(famName, tmp);
                }
            } catch (Exception e) {
                DialogFragment newFragment = new NetworkDialogFragment();
                newFragment.setCancelable(false);
                newFragment.show(getFragmentManager(), "no net");
                e.printStackTrace();
                return;
            }
        }

        // add the tabs
        ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        bar.addTab(bar.newTab().setText("All familiars")
                .setTabListener(new TabListener<SearchFamFragment>(this, "all fam", SearchFamFragment.class, null)));
        bar.addTab(bar.newTab().setText("New familiars")
                .setTabListener(new TabListener<NewFamFragment>(this, "new fam", NewFamFragment.class, null)));

        // Look up the AdView as a resource and load a request.
        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        // for our purposes, consider the app already opened at this point
        hasJustBeenStarted = false;
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
     * Fragment for the new familiar view
     */
    public static class NewFamFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_general_linear, container, false);
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.fragment_layout);
            new NewFamTask(getActivity(), layout).execute();
            return view;
        }
    }

    /**
     * Fragment for the search familiar view
     */
    public static class SearchFamFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_searchfam, container, false);

            try {
                if (adapter == null) adapter = new RegexFilterArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, FamStore.famList);

                EditText famEditText = (EditText) view.findViewById(R.id.famEditText);

                famEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        //adapter.getFilter().filter(s);
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void afterTextChanged(Editable s) {
                        adapter.getFilter().filter(s);
                    }
                });

                ListView famListView = (ListView) view.findViewById(R.id.famListView);
                famListView.setAdapter(adapter);
                famListView.setOnItemClickListener(new OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3)
                      {
                            String famName = (String)arg0.getItemAtPosition(position);
                            Intent intent = new Intent(v.getContext(), FamDetailActivity.class);
                            intent.putExtra(FAM_NAME, famName);
                            intent.putExtra(FAM_LINK, FamStore.famLinkTable.get(famName));
                            startActivity(intent);
                      }
                });

            } catch (Exception e) {
                Log.e("MainActivity", "Error setting up the fam list");
                e.printStackTrace();
            }

            return view;
        }
    }
}
