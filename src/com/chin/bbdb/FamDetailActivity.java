package com.chin.bbdb;

import java.text.DecimalFormat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class FamDetailActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fam_detail);
		// Show the Up button in the action bar.
		setupActionBar();
		
		Intent intent = getIntent();
		setTitle(intent.getStringExtra(MainActivity.FAM_NAME));
		String famURL = "http://bloodbrothersgame.wikia.com" + intent.getStringExtra(MainActivity.FAM_LINK);
		
		String famHTML = null;
		try {
			famHTML = new NetworkTask().execute(famURL).get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Document famDOM = Jsoup.parse(famHTML);
		
		////////////////////////////////////////////////////////////////////////////////////
		// Image section
		////////////////////////////////////////////////////////////////////////////////////
		Element infoBoxFam = famDOM.getElementsByClass("infobox").first();
		String imageUrl = infoBoxFam.getElementsByTag("tbody").first().getElementsByTag("tr").get(1).getElementsByTag("th").first().getElementsByTag("a").first().attr("href");
		new DownloadImageTask((ImageView) findViewById(R.id.imageView1)).execute(imageUrl);

		////////////////////////////////////////////////////////////////////////////////////
		// Stats section
		////////////////////////////////////////////////////////////////////////////////////
		
		TextView baseHP_textView    = (TextView) findViewById(R.id.baseHP_textView);
		TextView baseATK_textView   = (TextView) findViewById(R.id.baseATK_textView);
		TextView baseDEF_textView   = (TextView) findViewById(R.id.baseDEF_textView);
		TextView baseWIS_textView   = (TextView) findViewById(R.id.baseWIS_textView);
		TextView baseAGI_textView   = (TextView) findViewById(R.id.baseAGI_textView);
		TextView baseTotal_textView = (TextView) findViewById(R.id.baseTotal_textView);
		
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
		try {
			int totalPE = Integer.parseInt(hpPE.replace(",", "").replace(" ", "")) +
						 Integer.parseInt(atkPE.replace(",", "").replace(" ", "")) + 
						 Integer.parseInt(wisPE.replace(",", "").replace(" ", "")) + 
						 Integer.parseInt(defPE.replace(",", "").replace(" ", "")) + 
						 Integer.parseInt(agiPE.replace(",", "").replace(" ", ""));
			
			int totalMax = Integer.parseInt(hpMax.replace(",", "").replace(" ", "")) +
						 Integer.parseInt(atkMax.replace(",", "").replace(" ", "")) + 
						 Integer.parseInt(wisMax.replace(",", "").replace(" ", "")) + 
						 Integer.parseInt(defMax.replace(",", "").replace(" ", "")) + 
						 Integer.parseInt(agiMax.replace(",", "").replace(" ", ""));
			
			DecimalFormat formatter = new DecimalFormat("#,###");
			peTotalString = formatter.format(totalPE);
			maxTotalString = formatter.format(totalMax);
			
		} catch (Exception e) {
			e.printStackTrace();
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
		
		/////////////////////////////////////////////////////////////////////////
		// Skill section
		/////////////////////////////////////////////////////////////////////////
		Elements skillList = famDOM.getElementsByClass("infobox").first().getElementsByTag("tr").get(3).getElementsByTag("a");
		String skillLink1 = skillList.first().attr("href");
		
		String skillURL = "http://bloodbrothersgame.wikia.com" + skillLink1;
		String skillHTML = null;
		try {
			skillHTML = new NetworkTask().execute(skillURL).get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Document skillDOM = Jsoup.parse(skillHTML);
		Element infoBox = skillDOM.getElementsByClass("infobox").first();
		Elements skRows = infoBox.getElementsByTag("tbody").first().getElementsByTag("tr");
		
		int count = 0;
		TableLayout skillTable = (TableLayout) findViewById(R.id.skillTable);
		
		for (Element row : skRows) {
			if (count == 0) {
				// get the skill name
				String skillName = row.getElementsByTag("th").first().childNode(0).toString().trim();
				
				TableRow tr = new TableRow(this);
				TextView tv1 = new TextView(this);//title
				TextView tv2 = new TextView(this);//desc
				tv1.setText("Skill name");
				tv2.setText(skillName);
				tr.addView(tv1);
				tr.addView(tv2);
				skillTable.addView(tr);
				
				// add the line separator
				View tmpView = new View(this);
				tmpView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
				tmpView.setBackgroundColor(0xff444444);//dark grey
				skillTable.addView(tmpView);

				count++;
			}
			else if (count == 1) {
				// get the skill description
				String skillDesc = row.getElementsByTag("div").first().childNode(0).toString().trim();
				
				TableRow tr = new TableRow(this);
				TextView tv1 = new TextView(this);//title
				TextView tv2 = new TextView(this);//desc
				tv1.setText("Skill name");
				tv2.setText(skillDesc);
				tr.addView(tv1);
				tr.addView(tv2);
				skillTable.addView(tr);
				
				// add the line separator
				View tmpView = new View(this);
				tmpView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
				tmpView.setBackgroundColor(0xff444444);//dark grey
				skillTable.addView(tmpView);
				
				count++;
			}			
			else {
				Elements cells = row.getElementsByTag("td");
				String st1 = "", st2 = "";
				try {
					st1 = cells.get(0).getElementsByTag("b").first().childNode(0).toString().trim();
					st2 = cells.get(1).childNode(0).toString().replace("&amp;", "&").trim();
				} catch (Exception e) {}

				if (!st1.equals("") || !st2.equals("")) {
					TableRow tr = new TableRow(this);
					TextView tv1 = new TextView(this);//title
					TextView tv2 = new TextView(this);//desc
					tv1.setText(st1);
					tv2.setText(st2);
					tr.addView(tv1);
					tr.addView(tv2);
					skillTable.addView(tr);
					
					// add the line separator
					View tmpView = new View(this);
					tmpView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
					tmpView.setBackgroundColor(0xff444444);//dark grey
					skillTable.addView(tmpView);
				}
				count++;
			}
			skillTable.setShrinkAllColumns(true);
			skillTable.setStretchAllColumns(true);
			
		}
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

}
