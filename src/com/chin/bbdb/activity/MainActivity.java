package com.chin.bbdb.activity;

import java.util.ArrayList;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.chin.bbdb.FamStore;
import com.chin.bbdb.R;
import com.chin.bbdb.asyncTask.NetworkTask;
import com.chin.bbdb.asyncTask.NewFamTask;
import com.chin.common.CustomDialogFragment;
import com.chin.common.RegexFilterArrayAdapter;
import com.chin.common.TabListener;
import com.chin.common.Util;

/**
 * The main activity, entry point of the app. It consists of two main view, the familiar search
 * list and the new famliars list
 */
public class MainActivity extends BaseFragmentActivity {

    static boolean hasJustBeenStarted = true; // flag to determine if the app has just been started
    public final static String FAM_LINK = "com.chin.BBDB.LINK";
    public final static String FAM_NAME = "com.chin.BBDB.NAME";

    public static RegexFilterArrayAdapter<String> adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // if the app has just been started, show the drawer. Otherwise close it
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);
        if (hasJustBeenStarted) {
            mDrawerLayout.openDrawer(mDrawerList);
            Util.checkNewVersion(this, "https://api.github.com/repos/chinhodado/BBDB/releases/latest",
                    "https://github.com/chinhodado/BBDB/releases", false);
        }

        // get the familiar list and their wiki url
        if (FamStore.famList == null) {
            try {
                // this will return up to 5000 articles in the Familiar category. Note that this is not always up-to-date,
                // as newly added articles may take a day or two before showing up in here
                String url = "http://bloodbrothersgame.wikia.com/api/v1/Articles/List?category=Familiars&limit=5000&namespaces=0";
                String jsonString = new NetworkTask().execute(url).get();
                JSONObject myJSON = new JSONObject(jsonString);

                FamStore.famList = new ArrayList<String>();
                FamStore.famLinkTable = new Hashtable<String, String[]>(1536);

                JSONArray myArray = myJSON.getJSONArray("items");
                for (int i = 0; i < myArray.length(); i++) {
                    String famName = myArray.getJSONObject(i).getString("title");
                    FamStore.famList.add(famName);
                    String[] tmp = {myArray.getJSONObject(i).getString("url"),
                                    myArray.getJSONObject(i).getString("id")};
                    FamStore.famLinkTable.put(famName, tmp);
                }
            } catch (Exception e) {
                DialogFragment newFragment = new CustomDialogFragment("Unable to get familiar list. "
                        + "Make sure you are connected to the internet and try again");
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
                .setTabListener(new TabListener<SearchFamFragment>(this, "all fam", SearchFamFragment.class, null, R.id.tab_viewgroup)));
        bar.addTab(bar.newTab().setText("New familiars")
                .setTabListener(new TabListener<NewFamFragment>(this, "new fam", NewFamFragment.class, null, R.id.tab_viewgroup)));

        // if we're resuming the activity, re-select the tab that was selected before
        if (savedInstanceState != null) {
            // Select the tab that was selected before orientation change
            int index = savedInstanceState.getInt("TAB_INDEX");
            bar.setSelectedNavigationItem(index);
        }

        // for our purposes, consider the app already opened at this point
        hasJustBeenStarted = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
      super.onSaveInstanceState(bundle);
      // Save the index of the currently selected tab
      bundle.putInt("TAB_INDEX", getActionBar().getSelectedTab().getPosition());
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
                    public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
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
