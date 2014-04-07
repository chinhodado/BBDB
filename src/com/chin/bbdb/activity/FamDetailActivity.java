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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.support.v4.app.NavUtils;

public class FamDetailActivity extends Activity {

    public final Activity activity = this;

    // map a string (the evolution level) to a number that represents
    // the image for that evolution level
    public static HashMap<String, Integer> evolutionMap = null;

    public static String famName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fam_detail);

        // Look up the AdView as a resource and load a request.
        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        Intent intent = getIntent(); // careful, this intent may not be the intent from MainActivity...
        String tmpName = intent.getStringExtra(MainActivity.FAM_NAME);
        if (tmpName != null) {
            famName = tmpName; // needed since we may come back from other activity, not just the main one
        }

        setTitle("");
        initialize();
        new AddFamiliarInfoTask(this).execute(famName);

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
                Intent intent = new Intent(activity, FamCompareActivity.class);
                intent.putExtra("FAM_NAME_RIGHT", famName);
                intent.putExtra("FAM_NAME_LEFT", FamDetailActivity.famName);
                startActivity(intent);
            }
        });
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
