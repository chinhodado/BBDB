package com.chin.bbdb.activity;

import java.util.ArrayList;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONObject;

import com.chin.bbdb.FamStore;
import com.chin.bbdb.NetworkDialogFragment;
import com.chin.bbdb.R;
import com.chin.bbdb.RegexFilterArrayAdapter;
import com.chin.bbdb.asyncTask.NetworkTask;
import com.chin.bbdb.asyncTask.NewFamTask;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

public class MainActivity extends FragmentActivity {

    public static Activity activity = null;
    public final static String FAM_LINK = "com.chin.BBDB.LINK";
    public final static String FAM_NAME = "com.chin.BBDB.NAME";

    public static RegexFilterArrayAdapter<String> adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainActivity.activity = this;

        // get the familiar list and their wiki url
        if (FamStore.famList == null) {
            try {
                String url = "http://bloodbrothersgame.wikia.com/api/v1/Articles/List?category=Familiars&limit=5000&namespaces=0";
                String jsonString = new NetworkTask().execute(url).get();
                JSONObject myJSON = new JSONObject(jsonString);

                FamStore.famList = new ArrayList<String>();
                FamStore.famLinkTable = new Hashtable<String, String>();

                JSONArray myArray = myJSON.getJSONArray("items");
                for (int i = 0; i < myArray.length(); i++) {
                    String famName = myArray.getJSONObject(i).getString("title");
                    FamStore.famList.add(famName);
                    FamStore.famLinkTable.put(famName, myArray.getJSONObject(i).getString("url"));
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
                .setTabListener(new TabListener<SearchFamFragment>(this, "all fam", SearchFamFragment.class)));
        bar.addTab(bar.newTab().setText("New familiars")
                .setTabListener(new TabListener<NewFamFragment>(this, "new fam", NewFamFragment.class)));

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
                Intent intent = new Intent(activity, HelpActivity.class);
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

    public static class NewFamFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_newfam, container, false);
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.newfam_layout);
            new NewFamTask(MainActivity.activity, layout).execute();
            return view;
        }
    }

    public static class SearchFamFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_searchfam, container, false);

            Activity activity = MainActivity.activity;

            try {
                if (adapter == null) adapter = new RegexFilterArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, FamStore.famList);

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
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
                      {
                            String famName = (String)arg0.getItemAtPosition(position);
                            Intent intent = new Intent(MainActivity.activity, FamDetailActivity.class);
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

    public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private Fragment mFragment;
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;

        /** Constructor used each time a new tab is created.
          * @param activity  The host Activity, used to instantiate the fragment
          * @param tag  The identifier tag for the fragment
          * @param clz  The fragment's Class, used to instantiate the fragment
          */
        public TabListener(Activity activity, String tag, Class<T> clz) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
        }

        /* The following are each of the ActionBar.TabListener callbacks */

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            // Check if the fragment is already initialized
            if (mFragment == null) {
                // If not, instantiate and add it to the activity
                mFragment = Fragment.instantiate(mActivity, mClass.getName());
                ft.add(R.id.tab_viewgroup, mFragment, mTag);
            } else {
                // If it exists, simply attach it in order to show it
                ft.attach(mFragment);
            }
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                // Detach the fragment, because another one is being attached
                ft.detach(mFragment);
            }
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            // User selected the already selected tab. Usually do nothing.
        }
    }
}
