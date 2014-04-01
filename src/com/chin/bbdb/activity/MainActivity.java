package com.chin.bbdb.activity;

import java.util.ArrayList;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONObject;

import com.chin.bbdb.NetworkDialogFragment;
import com.chin.bbdb.R;
import com.chin.bbdb.RegexFilterArrayAdapter;
import com.chin.bbdb.asyncTask.NetworkTask;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.os.Bundle;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends Activity {

    public final Activity activity = this;
    public final static String FAM_LINK = "com.chin.BBDB.LINK";
    public final static String FAM_NAME = "com.chin.BBDB.NAME";
    private EditText famEditText;
    private ListView famListView;
    public static String jsonString = null;
    public static ArrayList<String> famList = null;
    public static Hashtable<String, String> famLinkTable = null;
    public static RegexFilterArrayAdapter<String> adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Look up the AdView as a resource and load a request.
        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        // get the familiar list and their wiki url
        if (famList == null) {
            try {
                String url = "http://bloodbrothersgame.wikia.com/api/v1/Articles/List?category=Familiars&limit=5000&namespaces=0";
                jsonString = new NetworkTask().execute(url).get();
                JSONObject myJSON = new JSONObject(jsonString);

                famList = new ArrayList<String>();
                famLinkTable = new Hashtable<String, String>();

                JSONArray myArray = myJSON.getJSONArray("items");
                for (int i = 0; i < myArray.length(); i++) {
                    String famName = myArray.getJSONObject(i).getString("title");
                    famList.add(famName);
                    famLinkTable.put(famName, myArray.getJSONObject(i).getString("url"));
                }
            } catch (Exception e) {
                    DialogFragment newFragment = new NetworkDialogFragment();
                    newFragment.setCancelable(false);
                    newFragment.show(getFragmentManager(), "no net");
                    e.printStackTrace();
                return;
            }
        }

        try {
            if (adapter == null) adapter = new RegexFilterArrayAdapter<String>(this, android.R.layout.simple_list_item_1, famList);

            famEditText = (EditText) findViewById(R.id.famEditText);

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

            famListView = (ListView) findViewById(R.id.famListView);
            famListView.setAdapter(adapter);
            famListView.setOnItemClickListener(new OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
                  {
                        String famName = (String)arg0.getItemAtPosition(position);
                        Intent intent = new Intent(activity, FamDetailActivity.class);
                        intent.putExtra(FAM_NAME, famName);
                        intent.putExtra(FAM_LINK, MainActivity.famLinkTable.get(famName));
                        startActivity(intent);
                  }
            });
        } catch (Exception e) {
            Log.e("MainActivity", "Error setting up the fam list");
            e.printStackTrace();
        }
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
}
