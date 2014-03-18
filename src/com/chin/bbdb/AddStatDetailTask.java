package com.chin.bbdb;

import java.text.DecimalFormat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

class AddStatDetailTask extends AsyncTask<String, Void, Document> {

	FamDetailActivity activity;
    
    public AddStatDetailTask(FamDetailActivity activity) {
        this.activity = activity;
    }

    @Override
	protected Document doInBackground(String... params) {
		String famHTML = null;
		try {
			famHTML = Jsoup.connect(params[0]).ignoreContentType(true).execute().body();
		} catch (Exception e) {
			Log.e("FamDetail", "Error fetching the fam HTML page");
			e.printStackTrace();
		}
		Document famDOM = Jsoup.parse(famHTML);
		return famDOM;
    }

	@Override
    protected void onPostExecute(Document famDOM) {
		// these need to be execute first so they can spawn async tasks to run in background
		addFamImage(famDOM);
		boolean isWarlord = famDOM.getElementById("WikiaArticleCategories").text().contains("Warlord");

		addFamSkill(famDOM, isWarlord);
		addFamDetail(famDOM, isWarlord);
		
		// this should be fast
		addFamStat(famDOM, isWarlord);
		addFamSpecialInformation(famDOM, isWarlord);

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
	
	public void addFamStat(Document famDOM, boolean isWarlord) {
		
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
		
		if (isWarlord) {
			activity.findViewById(R.id.textView3).setVisibility(View.GONE);
			peHP_textView.setVisibility(View.GONE);
			peATK_textView.setVisibility(View.GONE);
			peDEF_textView.setVisibility(View.GONE);
			peWIS_textView.setVisibility(View.GONE);
			peAGI_textView.setVisibility(View.GONE);
			peTotal_textView.setVisibility(View.GONE);
		}
		
		Elements statTable = famDOM.getElementsByClass("article-table");
		Element rowBase = statTable.first().getElementsByTag("tbody").first().getElementsByTag("tr").get(1);
		Element rowMax = statTable.first().getElementsByTag("tbody").first().getElementsByTag("tr").get(2);
		
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
		
		String hpPE = null, atkPE = null, defPE = null, wisPE = null, agiPE = null;
		if (!isWarlord) {
			Element rowPE = statTable.first().getElementsByTag("tbody").first().getElementsByTag("tr").get(3);
			hpPE = rowPE.getElementsByTag("td").get(1).childNode(0).toString();
			atkPE = rowPE.getElementsByTag("td").get(2).childNode(0).toString();
			defPE = rowPE.getElementsByTag("td").get(3).childNode(0).toString();
			wisPE = rowPE.getElementsByTag("td").get(4).childNode(0).toString();
			agiPE = rowPE.getElementsByTag("td").get(5).childNode(0).toString();
		}
		
		String maxTotalString = "";
		String peTotalString = "N/A";
		
		// array to store stats: HP, ATK, DEF, WIS, AGI, Total
		int[] PEStats = new int[6];
		int[] MaxStats = new int[6];
		
		try {
			DecimalFormat formatter = new DecimalFormat("#,###");
			
			MaxStats[0] = Integer.parseInt(hpMax.replace(",", "").replace(" ", ""));
			MaxStats[1] = Integer.parseInt(atkMax.replace(",", "").replace(" ", "")); 
			MaxStats[2] = Integer.parseInt(defMax.replace(",", "").replace(" ", ""));
			MaxStats[3] = Integer.parseInt(wisMax.replace(",", "").replace(" ", ""));
			MaxStats[4] = Integer.parseInt(agiMax.replace(",", "").replace(" ", ""));
			MaxStats[5] = MaxStats[0] + MaxStats[1] + MaxStats[2] + MaxStats[3] + MaxStats[4];
			maxTotalString = formatter.format(MaxStats[5]);
			
			if (!isWarlord) {
				PEStats[0] = Integer.parseInt(hpPE.replace(",", "").replace(" ", ""));
				PEStats[1] = Integer.parseInt(atkPE.replace(",", "").replace(" ", ""));
				PEStats[2] = Integer.parseInt(defPE.replace(",", "").replace(" ", ""));
				PEStats[3] = Integer.parseInt(wisPE.replace(",", "").replace(" ", ""));
				PEStats[4] = Integer.parseInt(agiPE.replace(",", "").replace(" ", ""));
				PEStats[5] = PEStats[0] + PEStats[1] + PEStats[2] + PEStats[3] + PEStats[4];			
				peTotalString = formatter.format(PEStats[5]);
			}
				
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
	
		if (!isWarlord) {
			peHP_textView.setText(hpPE);
			peATK_textView.setText(atkPE);
			peDEF_textView.setText(defPE);
			peWIS_textView.setText(wisPE);
			peAGI_textView.setText(agiPE);
			peTotal_textView.setText(peTotalString);
		}
		
		// POPE row	    
	    boolean isFinalEvolution = famDOM.getElementsByClass("container").first().html().indexOf("Final Evolution") != -1;
	    
	    if (isFinalEvolution || isWarlord) {
	    	
	    	// the link to the star level image
	    	String starLevelLink = famDOM.getElementsByClass("infobox").first() // the detail box
	    						.getElementsByTag("tbody").first().getElementsByTag("tr").get(4) // the evolution row
	    						.getElementsByTag("td").get(1) // the second cell (star image)
	    						.getElementsByTag("a").last().attr("href"); // link to the image
	    	String starLevel =	starLevelLink.substring(starLevelLink.length() - 8, starLevelLink.length() - 4); // will be of form "AofB"

	    	int toAdd = 0;

	    	if (isWarlord || starLevel.startsWith("1")) toAdd = 500;      // 1 star
	    	else if (starLevel.startsWith("2")) toAdd = 550; // 2 star
	    	else if (starLevel.startsWith("3")) toAdd = 605; // 3 star
	    	else if (starLevel.startsWith("4")) toAdd = 666; // 4 star

    		activity.addLineSeparator(statTableLayout);
    		TableRow popeRow = new TableRow(activity); statTableLayout.addView(popeRow);
    		TextView tmpTv1 = new TextView(activity); tmpTv1.setText("POPE"); popeRow.addView(tmpTv1);

    		DecimalFormat formatter = new DecimalFormat("#,###");

    		int[] POPEStats = new int[6];
    		for (int i = 0; i < 6; i++) {
    			if (i <= 4) { // the individual stats
    				if (isWarlord || starLevel.startsWith("1")) POPEStats[i] = MaxStats[i] + toAdd;
    				else POPEStats[i] = PEStats[i] + toAdd;
    			}
    			else if (i == 5) { // the total
    				if (isWarlord || starLevel.startsWith("1")) POPEStats[i] = MaxStats[i] + toAdd*5;
    				else POPEStats[i] = PEStats[i] + toAdd*5;
    			}
    			TextView tmpTv = new TextView(activity); tmpTv.setText(formatter.format(POPEStats[i])); popeRow.addView(tmpTv);
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