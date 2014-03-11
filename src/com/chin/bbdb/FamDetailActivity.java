package com.chin.bbdb;

import java.text.DecimalFormat;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class FamDetailActivity extends Activity {
	
	public static HashMap<String, Integer> evolutionMap = null;
	public static HashMap<String, String> pvpTierMap    = null;
	public static HashMap<String, String> raidTierMap   = null;
	public static HashMap<String, String> towerTierMap  = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fam_detail);
		// Show the Up button in the action bar.
		setupActionBar();
		
		Intent intent = getIntent();
		setTitle(intent.getStringExtra(MainActivity.FAM_NAME));
		String famURL = "http://bloodbrothersgame.wikia.com" + intent.getStringExtra(MainActivity.FAM_LINK);
		
		initialize();
		
		String famHTML = null;
		try {
			famHTML = new NetworkTask().execute(famURL).get();
		} catch (Exception e) {
			Log.e("FamDetail", "Error fetching the fam HTML page");
			e.printStackTrace();
		}
		Document famDOM = Jsoup.parse(famHTML);
		
		////////////////////////////////////////////////////////////////////////////////////
		// Image section
		////////////////////////////////////////////////////////////////////////////////////
		Element infoBoxFam = null;
		try {
			infoBoxFam = famDOM.getElementsByClass("infobox").first();
			String imageUrl = infoBoxFam.getElementsByTag("tbody").first().getElementsByTag("tr").get(1).getElementsByTag("th").first().getElementsByTag("a").first().attr("href");
			new DownloadImageTask(this).execute(imageUrl);
		} catch (Exception e) {
			Log.e("FamDetail", "Error displaying the fam image");
			e.printStackTrace();
		}

		////////////////////////////////////////////////////////////////////////////////////
		// Stats section
		////////////////////////////////////////////////////////////////////////////////////
		
		TableLayout statTableLayout = (TableLayout) findViewById(R.id.statTable);
		
		TextView baseHP_textView    = (TextView) findViewById(R.id.baseHP_textView);
		TextView baseATK_textView   = (TextView) findViewById(R.id.baseATK_textView);
		TextView baseDEF_textView   = (TextView) findViewById(R.id.baseDEF_textView);
		TextView baseWIS_textView   = (TextView) findViewById(R.id.baseWIS_textView);
		TextView baseAGI_textView   = (TextView) findViewById(R.id.baseAGI_textView);
		
		TextView maxHP_textView    = (TextView) findViewById(R.id.maxHP_textView);
		TextView maxATK_textView   = (TextView) findViewById(R.id.maxATK_textView);
		TextView maxDEF_textView   = (TextView) findViewById(R.id.maxDEF_textView);
		TextView maxWIS_textView   = (TextView) findViewById(R.id.maxWIS_textView);
		TextView maxAGI_textView   = (TextView) findViewById(R.id.maxAGI_textView);
		TextView maxTotal_textView = (TextView) findViewById(R.id.maxTotal_textView);
		
		TextView peHP_textView    = (TextView) findViewById(R.id.peHP_textView);
		TextView peATK_textView   = (TextView) findViewById(R.id.peATK_textView);
		TextView peDEF_textView   = (TextView) findViewById(R.id.peDEF_textView);
		TextView peWIS_textView   = (TextView) findViewById(R.id.peWIS_textView);
		TextView peAGI_textView   = (TextView) findViewById(R.id.peAGI_textView);
		TextView peTotal_textView = (TextView) findViewById(R.id.peTotal_textView);
		
		Elements statTable = famDOM.getElementsByClass("article-table");
		Element rowBase = statTable.first().getElementsByTag("tbody").first().getElementsByTag("tr").get(1);
		Element rowMax = statTable.first().getElementsByTag("tbody").first().getElementsByTag("tr").get(2);
		Element rowPE = statTable.first().getElementsByTag("tbody").first().getElementsByTag("tr").get(3);
		
		String hpBase  = rowBase.getElementsByTag("td").get(1).childNode(0).toString();		
		String atkBase = rowBase.getElementsByTag("td").get(2).childNode(0).toString();
		String defBase = rowBase.getElementsByTag("td").get(3).childNode(0).toString();
		String wisBase = rowBase.getElementsByTag("td").get(4).childNode(0).toString();
		String agiBase = rowBase.getElementsByTag("td").get(5).childNode(0).toString();
		
		String hpMax  = rowMax.getElementsByTag("td").get(1).childNode(0).toString();		
		String atkMax = rowMax.getElementsByTag("td").get(2).childNode(0).toString();
		String defMax = rowMax.getElementsByTag("td").get(3).childNode(0).toString();
		String wisMax = rowMax.getElementsByTag("td").get(4).childNode(0).toString();
		String agiMax = rowMax.getElementsByTag("td").get(5).childNode(0).toString();
		
		String hpPE  = rowPE.getElementsByTag("td").get(1).childNode(0).toString();		
		String atkPE = rowPE.getElementsByTag("td").get(2).childNode(0).toString();
		String defPE = rowPE.getElementsByTag("td").get(3).childNode(0).toString();
		String wisPE = rowPE.getElementsByTag("td").get(4).childNode(0).toString();
		String agiPE = rowPE.getElementsByTag("td").get(5).childNode(0).toString();
		
		String maxTotalString = "";
		String peTotalString = "N/A";
		
		// array to store stats: HP, ATK, DEF, WIS, AGI, Total
		int[] PEStats = new int[6];
		
		try {
			PEStats[0] = Integer.parseInt(hpPE.replace(",", "").replace(" ", ""));
			PEStats[1] = Integer.parseInt(atkPE.replace(",", "").replace(" ", ""));
			PEStats[2] = Integer.parseInt(defPE.replace(",", "").replace(" ", ""));
			PEStats[3] = Integer.parseInt(wisPE.replace(",", "").replace(" ", ""));
			PEStats[4] = Integer.parseInt(agiPE.replace(",", "").replace(" ", ""));
			PEStats[5] = PEStats[0] + PEStats[1] + PEStats[2] + PEStats[3] + PEStats[4];
			
			int totalMax = Integer.parseInt(hpMax.replace(",", "").replace(" ", "")) +
						 Integer.parseInt(atkMax.replace(",", "").replace(" ", "")) + 
						 Integer.parseInt(wisMax.replace(",", "").replace(" ", "")) + 
						 Integer.parseInt(defMax.replace(",", "").replace(" ", "")) + 
						 Integer.parseInt(agiMax.replace(",", "").replace(" ", ""));
			
			DecimalFormat formatter = new DecimalFormat("#,###");
			peTotalString = formatter.format(PEStats[5]);
			maxTotalString = formatter.format(totalMax);			
		} catch (Exception e) {
			Log.i("FamDetail", "Error parsing number, probably N/A");
		}
		
		baseHP_textView.setText(hpBase); 
		baseATK_textView.setText(atkBase); 
		baseDEF_textView.setText(defBase);
		baseWIS_textView.setText(wisBase); 
		baseAGI_textView.setText(agiBase); 
		
		maxHP_textView.setText(hpMax);
		maxATK_textView.setText(atkMax);
		maxDEF_textView.setText(defMax);
		maxWIS_textView.setText(wisMax);
		maxAGI_textView.setText(agiMax);
		maxTotal_textView.setText(maxTotalString);
	
		peHP_textView.setText(hpPE);
		peATK_textView.setText(atkPE);
		peDEF_textView.setText(defPE);
		peWIS_textView.setText(wisPE);
		peAGI_textView.setText(agiPE);
		peTotal_textView.setText(peTotalString);
		
		// POPE row
		String famCategory = famDOM.getElementsByClass("name").first().getElementsByTag("a").first().childNode(0).toString();
		int toAdd = 0;
	    if (famCategory.equals("Epic 4")) toAdd = 666;           // POPE EP4
	    else if (famCategory.equals("Epic 2")) toAdd = 550;      // POPE EP2
	    else if (famCategory.equals("Legendary 2")) toAdd = 550; // POPE L2
	    else if (famCategory.equals("Legendary 3")) toAdd = 605; // POPE L3
	    else if (famCategory.equals("Mythic 2")) toAdd = 550;    // POPE M2
	    
	    if (toAdd != 0) {
	    	addLineSeparator(statTableLayout);
	    	TableRow popeRow = new TableRow(this); statTableLayout.addView(popeRow);
	    	TextView tmpTv1 = new TextView(this); tmpTv1.setText("POPE"); popeRow.addView(tmpTv1);
	    	
			DecimalFormat formatter = new DecimalFormat("#,###");
	    	
	    	int[] POPEStats = new int[6];
	    	for (int i = 0; i < 6; i++) {
	    		if (i <= 4) { // the individual stats
	    			POPEStats[i] = PEStats[i] + toAdd;
	    		}
	    		else if (i == 5) { // the total
	    			POPEStats[i] = PEStats[i] + toAdd*5;
	    		}
	    		TextView tmpTv = new TextView(this); tmpTv.setText(formatter.format(POPEStats[i])); popeRow.addView(tmpTv);
	    	}
	    }
	    
	    TableRow emptyRow = new TableRow(this); statTableLayout.addView(emptyRow);
	    TextView emptyTv = new TextView(this); emptyRow.addView(emptyTv);
		
		///////////////////////////////////////////////////////////////////////////////////
		// Skill section
		///////////////////////////////////////////////////////////////////////////////////
		
		TableLayout skillTable = (TableLayout) findViewById(R.id.skillTable);
		new AddFamSkillInfoTask(skillTable, this).execute(famDOM);		
		
		/////////////////////////////////////////////////////////////////////////////////
		// Details section
		/////////////////////////////////////////////////////////////////////////////////
	
		int count = 0;
		TableLayout detailTable = (TableLayout) findViewById(R.id.detailTable);
		Elements detailRows = infoBoxFam.getElementsByTag("tbody").first().getElementsByTag("tr");
		
		for (Element detailRow : detailRows) {
			if (count <= 3) {
				// 0: fam name, 1: image, 2: detail, 3: skill
				// do nothing
				count++;
			}
			else if (count == 4) {
				Elements cells = detailRow.getElementsByTag("td");
				String st1 = "", st2 = "";
				try {
					// try to get the rarity and stars so we can display our offline image (avoid downloading)
					// get the rarity
					String[] tmpArr = cells.get(1).getElementsByTag("a").first().attr("href").split("\\."); // split by .
					st1 = tmpArr[tmpArr.length - 2]; // get the second last token
					
					// get the star name
					String tmpStr = cells.get(1).getElementsByTag("a").last().attr("href");
					st2 = tmpStr.substring(tmpStr.length() - 8, tmpStr.length() - 4); // will be of form "AofB"
				} catch (Exception e) {
					Log.e("FamDetail", "Error getting the evolution images' details");
				}
				
				TableRow tr = new TableRow(this);
				TextView tv1 = new TextView(this); tv1.setText("Evolution"); // not sure if it should be hard-coded like this...
				
				LinearLayout tmpViewGroup = new LinearLayout(this);
				tmpViewGroup.setLayoutParams(new TableRow.LayoutParams (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
				
				try {
					ImageView imvRarity = new ImageView(this);				
					imvRarity.setScaleType(ImageView.ScaleType.FIT_START);
					imvRarity.setLayoutParams(new TableRow.LayoutParams (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
					imvRarity.setImageResource(evolutionMap.get(st1));
					
					ImageView imvStar = new ImageView(this);
					imvStar.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
					imvStar.setLayoutParams(new TableRow.LayoutParams (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
					imvStar.setImageResource(evolutionMap.get(st2));
					
					tr.addView(tv1);
					tmpViewGroup.addView(imvRarity); tmpViewGroup.addView(imvStar);
					tr.addView(tmpViewGroup);
					detailTable.addView(tr);
				} catch (Exception e) {
					Log.e("FamDetail", "Error setting the evolution images");
					e.printStackTrace();
				}
				count++;
				
				// add the line separator
				addLineSeparator(detailTable);
			}			
			else {
				Elements cells = detailRow.getElementsByTag("td");
				String st1 = "", st2 = "";
				try {
					st1 = cells.get(0).text().trim();
					st2 = cells.get(1).text().trim();
				} catch (Exception e) {}

				// this is important since there are empty filler rows like <tr></tr>, we skip those
				if (!st1.equals("") || !st2.equals("")) {
					addRowWithTwoTextView(detailTable, st1, st2, true);
				}
				count++;
			}
		}
		
		// the tier rows
		new AddFamTierInfoTask(detailTable, this, intent.getStringExtra(MainActivity.FAM_NAME)).execute();
				
		detailTable.setColumnShrinkable(1, true);
		detailTable.setStretchAllColumns(true);
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
		getMenuInflater().inflate(R.menu.fam_detail, menu);
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
}
