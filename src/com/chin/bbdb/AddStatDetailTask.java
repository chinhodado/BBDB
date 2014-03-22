package com.chin.bbdb;

import java.text.DecimalFormat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chin.bbdb.FamStore.FamStats;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

class AddStatDetailTask extends AsyncTask<String, Void, FamStore> {

	FamDetailActivity activity;
	String famName;
    
    public AddStatDetailTask(FamDetailActivity activity) {
        this.activity = activity;
    }

    @Override
	protected FamStore doInBackground(String... params) {
    	this.famName = params[0];
    	MainActivity.famStore.getStats(this.famName);
		return MainActivity.famStore;
    }

	@Override
    protected void onPostExecute(FamStore famStore) {
		// these need to be execute first so they can spawn async tasks to run in background
//		addFamImage(famDOM);
//
//		addFamSkill(famDOM, isWarlord);
//		addFamDetail(famDOM, isWarlord);
		
		// this should be fast
		addFamStat(famStore);
//		addFamSpecialInformation(famDOM, isWarlord);

    }
	
	public void addFamImage(Document famDOM) {
		Element infoBoxFam = null;
		try {
			infoBoxFam = famDOM.getElementsByClass("infobox").first();
			String imageUrl = infoBoxFam.getElementsByTag("tbody").first().getElementsByTag("tr").get(1).getElementsByTag("th").first().getElementsByTag("a").first().attr("href");
			new DownloadImageTask(activity).execute(imageUrl);
		} catch (Exception e) {
			Log.e("FamDetail", "Error displaying the fam image");
			e.printStackTrace();
		}
	}
	
	public void addFamSkill(Document famDOM, boolean isWarlord) {
		TableLayout skillTable = (TableLayout) activity.findViewById(R.id.skillTable);
		new AddFamSkillInfoTask(skillTable, activity, isWarlord).execute(famDOM);		
	}
	
	public void addFamStat(FamStore famStore) {
		
		TableLayout statTableLayout = (TableLayout) activity.findViewById(R.id.statTable);
		
		TextView baseHP_textView    = (TextView) activity.findViewById(R.id.baseHP_textView);
		TextView baseATK_textView   = (TextView) activity.findViewById(R.id.baseATK_textView);
		TextView baseDEF_textView   = (TextView) activity.findViewById(R.id.baseDEF_textView);
		TextView baseWIS_textView   = (TextView) activity.findViewById(R.id.baseWIS_textView);
		TextView baseAGI_textView   = (TextView) activity.findViewById(R.id.baseAGI_textView);
		
		TextView maxHP_textView    = (TextView) activity.findViewById(R.id.maxHP_textView);
		TextView maxATK_textView   = (TextView) activity.findViewById(R.id.maxATK_textView);
		TextView maxDEF_textView   = (TextView) activity.findViewById(R.id.maxDEF_textView);
		TextView maxWIS_textView   = (TextView) activity.findViewById(R.id.maxWIS_textView);
		TextView maxAGI_textView   = (TextView) activity.findViewById(R.id.maxAGI_textView);
		TextView maxTotal_textView = (TextView) activity.findViewById(R.id.maxTotal_textView);
		
		TextView peHP_textView    = (TextView) activity.findViewById(R.id.peHP_textView);
		TextView peATK_textView   = (TextView) activity.findViewById(R.id.peATK_textView);
		TextView peDEF_textView   = (TextView) activity.findViewById(R.id.peDEF_textView);
		TextView peWIS_textView   = (TextView) activity.findViewById(R.id.peWIS_textView);
		TextView peAGI_textView   = (TextView) activity.findViewById(R.id.peAGI_textView);
		TextView peTotal_textView = (TextView) activity.findViewById(R.id.peTotal_textView);
		
		if (famStore.isWarlord(famName)) {
			activity.findViewById(R.id.textView3).setVisibility(View.GONE);
			peHP_textView.setVisibility(View.GONE);
			peATK_textView.setVisibility(View.GONE);
			peDEF_textView.setVisibility(View.GONE);
			peWIS_textView.setVisibility(View.GONE);
			peAGI_textView.setVisibility(View.GONE);
			peTotal_textView.setVisibility(View.GONE);
		}
		
		String maxTotalString = "";
		String peTotalString = "N/A";
		
		FamStats stats = famStore.getStats(famName);
		
		DecimalFormat formatter = new DecimalFormat("#,###");
		maxTotalString = formatter.format(stats.maxStats[5]);
		peTotalString = formatter.format(stats.PEStats[5]);
		
		baseHP_textView.setText(formatter.format(stats.baseStats[0])); 
		baseATK_textView.setText(formatter.format(stats.baseStats[1])); 
		baseDEF_textView.setText(formatter.format(stats.baseStats[2]));
		baseWIS_textView.setText(formatter.format(stats.baseStats[3])); 
		baseAGI_textView.setText(formatter.format(stats.baseStats[4])); 
		
		maxHP_textView.setText(formatter.format(stats.maxStats[0]));
		maxATK_textView.setText(formatter.format(stats.maxStats[1]));
		maxDEF_textView.setText(formatter.format(stats.maxStats[2]));
		maxWIS_textView.setText(formatter.format(stats.maxStats[3]));
		maxAGI_textView.setText(formatter.format(stats.maxStats[4]));
		maxTotal_textView.setText(formatter.format(stats.maxStats[5]));
	
		if (!famStore.isWarlord(famName)) {
			peHP_textView.setText(formatter.format(stats.PEStats[0]));
			peATK_textView.setText(formatter.format(stats.PEStats[1]));
			peDEF_textView.setText(formatter.format(stats.PEStats[2]));
			peWIS_textView.setText(formatter.format(stats.PEStats[3]));
			peAGI_textView.setText(formatter.format(stats.PEStats[4]));
			peTotal_textView.setText(formatter.format(stats.PEStats[5]));
		}
		
		// POPE row	    
	    
	    if (famStore.isFinalEvolution(famName) || famStore.isWarlord(famName)) {
	    	
    		activity.addLineSeparator(statTableLayout);
    		TableRow popeRow = new TableRow(activity); statTableLayout.addView(popeRow);
    		TextView tmpTv1 = new TextView(activity); tmpTv1.setText("POPE"); popeRow.addView(tmpTv1);

    		for (int i = 0; i < 6; i++) {
    			TextView tmpTv = new TextView(activity); tmpTv.setText(formatter.format(stats.POPEStats[i])); popeRow.addView(tmpTv);
    		}
	    }
	    
	    TableRow emptyRow = new TableRow(activity); statTableLayout.addView(emptyRow);
	    TextView emptyTv = new TextView(activity); emptyRow.addView(emptyTv);
	}
	
	public void addFamDetail(Document famDOM, boolean isWarlord) {
		int count = 0;
		TableLayout detailTable = (TableLayout) activity.findViewById(R.id.detailTable);
		Element infoBoxFam = famDOM.getElementsByClass("infobox").first();
		Elements detailRows = infoBoxFam.getElementsByTag("tbody").first().getElementsByTag("tr");
		
		for (Element detailRow : detailRows) {
			if (count <= 3 && count!=2) {
				// 0: fam name, 1: image, 2: detail, 3: skill/race(on warlord)
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
				
				TableRow tr = new TableRow(activity);
				TextView tv1 = new TextView(activity); tv1.setText("Evolution"); // not sure if it should be hard-coded like this...
				
				LinearLayout tmpViewGroup = new LinearLayout(activity);
				tmpViewGroup.setLayoutParams(new TableRow.LayoutParams (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
				
				try {
					ImageView imvRarity = new ImageView(activity);				
					imvRarity.setScaleType(ImageView.ScaleType.FIT_START);
					imvRarity.setLayoutParams(new TableRow.LayoutParams (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
					imvRarity.setImageResource(FamDetailActivity.evolutionMap.get(st1));
					
					ImageView imvStar = new ImageView(activity);
					imvStar.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
					imvStar.setLayoutParams(new TableRow.LayoutParams (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
					imvStar.setImageResource(FamDetailActivity.evolutionMap.get(st2));
					
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
				activity.addLineSeparator(detailTable);
			}			
			else {
				if (count == 2 && !isWarlord) {
					count++;
					continue;
				}
				Elements cells = detailRow.getElementsByTag("td");
				String st1 = "", st2 = "";
				try {
					st1 = cells.get(0).text().trim();
					st2 = cells.get(1).text().trim();
				} catch (Exception e) {}

				// this is important since there are empty filler rows like <tr></tr>, we skip those
				if (!st1.equals("") || !st2.equals("")) {
					activity.addRowWithTwoTextView(detailTable, st1, st2, true);
				}
				count++;
			}
		}
		
		// the tier rows
		new AddFamTierInfoTask(detailTable, activity, activity.famName).execute();
				
		detailTable.setColumnShrinkable(1, true);
		detailTable.setStretchAllColumns(true);		
	}

	public void addFamSpecialInformation(Document famDOM, boolean isWarlord) {
		if (isWarlord) return;
		Element div = famDOM.getElementById("mw-content-text");
		boolean hasSpecialInformation = false;
		for (Element child : div.children()) {
			String text = child.text();
			if (text.equals("Special Information")) {
				hasSpecialInformation = true;
			}
			else if (text.equals("Evolution Line") || text.equals("Locations")) {
				hasSpecialInformation = false; // just to be safe
				break;
			}
			else if (hasSpecialInformation) {
				if (text.startsWith("See") || text.endsWith("tier.") || text.endsWith("origins.") || text.equals("")) continue;
				else {
					// maybe called several times, doesn't matter
					activity.findViewById(R.id.textViewSpecialInformationLabel).setVisibility(View.VISIBLE);
					activity.findViewById(R.id.textViewSpecialInformation).setVisibility(View.VISIBLE);
					
					// the actual special information
					((TextView) activity.findViewById(R.id.textViewSpecialInformation)).append(text + "\n\n");
				}				
			}
		}
		
		String categories = famDOM.getElementById("WikiaArticleCategories").text();
		if (categories.contains("Mounted Familiars")) {
			// maybe called several times, doesn't matter
			activity.findViewById(R.id.textViewSpecialInformationLabel).setVisibility(View.VISIBLE);
			activity.findViewById(R.id.textViewSpecialInformation).setVisibility(View.VISIBLE);
			
			// the actual special information
			((TextView) activity.findViewById(R.id.textViewSpecialInformation)).append(
					"This is a mounted familiar. Mounted familiars gets two attacks each turn and has two skills. "
					+ "The first attack it does in a turn can only trigger the first skill, "
					+ "while the second attack can only trigger the second skill." + "\n\n");
		}
	}
}