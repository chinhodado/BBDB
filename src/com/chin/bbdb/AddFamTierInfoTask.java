package com.chin.bbdb;

import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;

class AddFamTierInfoTask extends AsyncTask<Void, Void, Void> {
	TableLayout detailTable;
	FamDetailActivity activity;
	String famName;
	String pvpTierHTML = null, raidTierHTML = null, towerTierHTML = null;
    
    public AddFamTierInfoTask(TableLayout detailTable, FamDetailActivity activity, String famName) {
        this.detailTable = detailTable;
        this.activity = activity;
        this.famName = famName;
    }

    @Override
	protected Void doInBackground(Void... params) {
    	if (FamDetailActivity.pvpTierMap == null || FamDetailActivity.raidTierMap == null || FamDetailActivity.towerTierMap == null) {
			try {
				fetchTierMaps();
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
		return null;
    }

	@Override
    protected void onPostExecute(Void param) {
		String famPVPTier = null, famRaidTier = null, famTowerTier = null;
		famPVPTier   = FamDetailActivity.pvpTierMap.get(famName);
		famRaidTier  = FamDetailActivity.raidTierMap.get(famName);
		famTowerTier = FamDetailActivity.towerTierMap.get(famName);
		
    	// remove the spinner
    	ProgressBar pgrBar = (ProgressBar) activity.findViewById(R.id.progressBar3);
    	LinearLayout layout = (LinearLayout) activity.findViewById(R.id.linearLayout1);
    	layout.removeView(pgrBar);
		
		activity.addRowWithTwoTextView(detailTable, "PVP tier", famPVPTier==null? "N/A" : famPVPTier, true);
		activity.addRowWithTwoTextView(detailTable, "Raid tier", famRaidTier==null? "N/A" : famRaidTier, true);
		activity.addRowWithTwoTextView(detailTable, "Tower tier", famTowerTier==null? "N/A" : famTowerTier, true);
    }
    
	/**
	 * Initialize pvpTierMap, raidTierMap and towerTierMap 
	 */
	public void fetchTierMaps() {
		
		try {
			pvpTierHTML = Jsoup.connect("http://bloodbrothersgame.wikia.com/index.php?title=Familiar_Tier_List/PvP&action=render").ignoreContentType(true).execute().body();
			raidTierHTML = Jsoup.connect("http://bloodbrothersgame.wikia.com/index.php?title=Familiar_Tier_List/Raid&action=render").ignoreContentType(true).execute().body();
			towerTierHTML = Jsoup.connect("http://bloodbrothersgame.wikia.com/index.php?title=Familiar_Tier_List/Tower&action=render").ignoreContentType(true).execute().body();
		} catch (Exception e) {
			Log.e("FamDetail", "Error fetching the tier HTML pages");
			e.printStackTrace();
		}
		
		Document pvpDOM   = Jsoup.parse(pvpTierHTML);
		Document raidDOM  = Jsoup.parse(raidTierHTML);
		Document towerDOM = Jsoup.parse(towerTierHTML);
		
	    Elements pvpTables   = pvpDOM.getElementsByClass("wikitable");
	    Elements raidTables  = raidDOM.getElementsByClass("wikitable");
	    Elements towerTables = towerDOM.getElementsByClass("wikitable");

	    String[] tiers = {"X", "S+", "S", "A+", "A", "B", "C", "D", "E"};
	    
	    FamDetailActivity.pvpTierMap = new HashMap<String, String>();
	    FamDetailActivity.raidTierMap = new HashMap<String, String>();
	    FamDetailActivity.towerTierMap = new HashMap<String, String>();

	    for (int i = 0; i < 9; i++){ // 9 tables		
	    	Elements pvpRows = pvpTables.get(i).getElementsByTag("tbody").first().getElementsByTag("tr"); // get all rows in each table
	    	int countRow = 0;
	    	for (Element pvpRow : pvpRows) {
		    	countRow++;
				if (countRow < 3) continue; // row 1 is the table title, row 2 is the column headers. This is different in the DOM in browser
	    		
	    		// second cell, bold text, a, text
	    		String famName = pvpRow.getElementsByTag("td").get(1).getElementsByTag("b").first().getElementsByTag("a").first().childNode(0).toString();
	    		FamDetailActivity.pvpTierMap.put(famName, tiers[i]);
	    	}
	    	
	    	Elements raidRows = raidTables.get(i).getElementsByTag("tbody").first().getElementsByTag("tr"); // get all rows in each table
	    	countRow = 0;
	    	for (Element raidRow : raidRows) {
		    	countRow++;
				if (countRow < 3) continue; // row 1 is the table title, row 2 is the column headers. This is different in the DOM in browser
	    		
	    		// second cell, bold text, a, text
	    		String famName = raidRow.getElementsByTag("td").get(1).getElementsByTag("b").first().getElementsByTag("a").first().childNode(0).toString();
	    		FamDetailActivity.raidTierMap.put(famName, tiers[i]);
	    	}
	    	
	    	Elements towerRows = towerTables.get(i).getElementsByTag("tbody").first().getElementsByTag("tr"); // get all rows in each table
	    	countRow = 0;
	    	for (Element towerRow : towerRows) {
		    	countRow++;
				if (countRow < 3) continue; // row 1 is the table title, row 2 is the column headers. This is different in the DOM in browser
	    		
	    		// second cell, bold text, a, text
	    		String famName = towerRow.getElementsByTag("td").get(1).getElementsByTag("b").first().getElementsByTag("a").first().childNode(0).toString();
	    		FamDetailActivity.towerTierMap.put(famName, tiers[i]);
	    	}
	    }
	}
}