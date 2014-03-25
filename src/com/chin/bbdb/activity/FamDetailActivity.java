package com.chin.bbdb.activity;

import java.util.HashMap;

import com.chin.bbdb.R;
import com.chin.bbdb.asyncTask.AddFamiliarInfoTask;
import com.google.analytics.tracking.android.EasyTracker;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

public class FamDetailActivity extends ActionBarActivity {
	
	public final Activity activity = this;
	
	// map a string (the evolution level) to a number that represents
	// the image for that evolution level
	public static HashMap<String, Integer> evolutionMap = null;
	
	public static HashMap<String, String> pvpTierMap    = null;
	public static HashMap<String, String> raidTierMap   = null;
	public static HashMap<String, String> towerTierMap  = null;
	public static String famName = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fam_detail);
		
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
        
        ActionBar actionBar = getSupportActionBar();
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
//		getMenuInflater().inflate(R.menu.fam_detail_menu, menu);

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
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void addLineSeparator(ViewGroup view) {
		View tmpView = new View(this);
		tmpView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
		tmpView.setBackgroundColor(0xff444444);//dark grey
		view.addView(tmpView);
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
	
	/**
	 * Add a row with 2 TextView (e.g. Title/Description) to a table.
	 * @param table The table to add the row to
	 * @param textView1String The text of the first Textview
	 * @param textView2String The text of the second TextView
	 * @param showLineSeparator true if a line separator is added after the new row
	 */
	public void addRowWithTwoTextView(TableLayout table, String textView1String, String textView2String, boolean showLineSeparator) {
		TableRow tr = new TableRow(this);
		TextView tv1 = new TextView(this); tv1.setText(textView1String); tr.addView(tv1);
		TextView tv2 = new TextView(this); tv2.setText(textView2String); tr.addView(tv2);
		table.addView(tr);
		if (showLineSeparator) addLineSeparator(table);
	}
	
	/**
	 * Add a row with 2 TextView (e.g. Title/Description) to a table.
	 * @param table The table to add the row to
	 * @param textView1String The text of the first Textview
	 * @param textView2String The text of the second TextView
	 * @param typeface The typeface for the two TextView
	 * @param showLineSeparator true if a line separator is added after the new row
	 */
	public void addRowWithTwoTextView(TableLayout table, String textView1String, String textView2String, boolean showLineSeparator, Typeface typeface) {
		TableRow tr = new TableRow(this);
		TextView tv1 = new TextView(this); tv1.setText(textView1String); tr.addView(tv1);
		TextView tv2 = new TextView(this); tv2.setText(textView2String); tr.addView(tv2);
		table.addView(tr);
		if (showLineSeparator) addLineSeparator(table);
		tv1.setTypeface(typeface); tv2.setTypeface(typeface);
	}
	
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
