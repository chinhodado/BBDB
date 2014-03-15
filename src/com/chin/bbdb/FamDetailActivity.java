package com.chin.bbdb;

import java.util.HashMap;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class FamDetailActivity extends Activity {
	
	public static HashMap<String, Integer> evolutionMap = null;
	public static HashMap<String, String> pvpTierMap    = null;
	public static HashMap<String, String> raidTierMap   = null;
	public static HashMap<String, String> towerTierMap  = null;
	public String famName = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fam_detail);
		// Show the Up button in the action bar.
		setupActionBar();
		
		Intent intent = getIntent();
		famName = intent.getStringExtra(MainActivity.FAM_NAME);
		setTitle(famName);
		String famURL = "http://bloodbrothersgame.wikia.com" + intent.getStringExtra(MainActivity.FAM_LINK);		
		initialize();
		new AddStatDetailTask(this).execute(famURL);
 	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.fam_detail, menu);
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
}
