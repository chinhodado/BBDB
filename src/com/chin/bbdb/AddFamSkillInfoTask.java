package com.chin.bbdb;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

class AddFamSkillInfoTask extends AsyncTask<Document, Void, Void> {
	TableLayout skillTable;
	FamDetailActivity activity;
	String[] skillLink = {null, null};
    
    public AddFamSkillInfoTask(TableLayout skillTable, FamDetailActivity activity) {
        this.skillTable = skillTable;
        this.activity = activity;
    }

    @Override
	protected Void doInBackground(Document... params) {
    	Elements skillList = params[0].getElementsByClass("infobox").first().getElementsByTag("tr").get(3).getElementsByTag("a");
		skillLink[0] = skillList.get(0).attr("href");
		try {
			skillLink[1] = skillList.get(1).attr("href");
		} catch (Exception e) {};
		
		return null;
    }

	@Override
    protected void onPostExecute(Void param) {
		for (int i = 0; i < skillLink.length; i++) {
			if (skillLink[i] == null) continue;
			String skillURL = "http://bloodbrothersgame.wikia.com" + skillLink[i];
			String skillHTML = null;
			try {
				skillHTML = new NetworkTask().execute(skillURL).get();
			} catch (Exception e) {
				Log.e("FamDetail", "Error fetching the fam skill page");
				e.printStackTrace();
			}
			Document skillDOM = Jsoup.parse(skillHTML);
			Element infoBox = skillDOM.getElementsByClass("infobox").first();
			Elements skRows = infoBox.getElementsByTag("tbody").first().getElementsByTag("tr");
			
			int count = 0;
	
			for (Element row : skRows) {
				if (count == 0) {
					// get the skill name
					String skillName = row.getElementsByTag("th").first().childNode(0).toString().trim();				
					activity.addRowWithTwoTextView(skillTable, "Skill name", skillName, true);
					count++;
				}
				else if (count == 1) {
					// get the skill description
					String skillDesc = row.getElementsByTag("div").first().childNode(0).toString().trim();				
					activity.addRowWithTwoTextView(skillTable, "Description", skillDesc, true);				
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
						activity.addRowWithTwoTextView(skillTable, st1, st2, true);
					}
					count++;
				}
			}
			
			// add an empty row as a separator
			TableRow trtmp = new TableRow(activity);
			TextView tvtmp = new TextView(activity);
			trtmp.addView(tvtmp);
			skillTable.addView(trtmp);
		}
		skillTable.setColumnShrinkable(1, true);
		skillTable.setStretchAllColumns(true);
    }    
}