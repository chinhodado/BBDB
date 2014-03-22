package com.chin.bbdb;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * A storage for familiar information. Support lazy loading information.
 * @author Chin
 *
 */
public final class FamStore {
	
	/**
	 * A structure to hold a familiar's information
	 * @author Chin
	 *
	 */
	public static class FamDetail {		
		String name = null;
		FamStats stats = null;
		String starLevel = null;
		Document famDOM;
		String[] skillHTML = {null, null};
		
		boolean isFinalEvolution = false;
		boolean isWarlord = false;
		
		FamDetail(String name) {
			this.name = name;
		}
	}
	
	public static class FamStats {
		public int[] baseStats = null;
		public int[] maxStats  = null;
		public int[] PEStats   = null;
		public int[] POPEStats = null;
	}
		
	static final int MAX_CACHED_IMAGE = 5;    
    
    @SuppressWarnings("serial")
    LinkedHashMap<String, Bitmap> imageStore = new LinkedHashMap<String, Bitmap>(MAX_CACHED_IMAGE + 1) {
        protected boolean removeEldestEntry(Map.Entry<String, Bitmap> eldest) {
           return size() > MAX_CACHED_IMAGE;
        }
     };
	
	private static Hashtable<String, FamDetail> famStore = new Hashtable<String, FamDetail>();
	private static final FamStore FAMSTORE = new FamStore();
	
	private FamStore() {
        if (FAMSTORE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static FamStore getInstance() {
        return FAMSTORE;
    }
	
	/**
	 * Get all stats of a familiar. Careful, this can block!
	 * Although the name is getStats(), it also fetches the following:
	 * - starLevel
	 * - isFinalEvolution
	 * - isWarlord
	 * so if you want to use those make sure to call this function first
	 */
	public FamStats getStats(String famName) {
		
		FamDetail currentFam = famStore.get(famName);
		
		if (currentFam != null && currentFam.stats != null) return currentFam.stats;
		
		if (currentFam == null) {
			currentFam = new FamDetail(famName);
			famStore.put(famName, currentFam);
		}
		
		String famURL = "http://bloodbrothersgame.wikia.com" + MainActivity.famLinkTable.get(famName);
		
		String famHTML = null;
		try {
			famHTML = Jsoup.connect(famURL).ignoreContentType(true).execute().body();
		} catch (Exception e) {
			Log.e("FamDetail", "Error fetching the fam HTML page");
			e.printStackTrace();
		}
		Document famDOM = Jsoup.parse(famHTML);
		
		// save the DOM for later use. Should look into this so that it doesn't cause huge mem usage
		currentFam.famDOM = famDOM;
		
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
		currentFam.isWarlord = famDOM.getElementById("WikiaArticleCategories").text().contains("Warlord");
	    currentFam.isFinalEvolution = famDOM.getElementsByClass("container").first().html().indexOf("Final Evolution") != -1;


		if (!currentFam.isWarlord) {
			Element rowPE = statTable.first().getElementsByTag("tbody").first().getElementsByTag("tr").get(3);
			hpPE = rowPE.getElementsByTag("td").get(1).childNode(0).toString();
			atkPE = rowPE.getElementsByTag("td").get(2).childNode(0).toString();
			defPE = rowPE.getElementsByTag("td").get(3).childNode(0).toString();
			wisPE = rowPE.getElementsByTag("td").get(4).childNode(0).toString();
			agiPE = rowPE.getElementsByTag("td").get(5).childNode(0).toString();
		}
		
		// array to store stats: HP, ATK, DEF, WIS, AGI, Total
		currentFam.stats = new FamStats();
		currentFam.stats.baseStats = new int[6];		
		currentFam.stats.maxStats  = new int[6];
		currentFam.stats.PEStats   = new int[6];
		
		try {
			
			currentFam.stats.baseStats[0] = Integer.parseInt(hpBase.replace(",", "").replace(" ", ""));
			currentFam.stats.baseStats[1] = Integer.parseInt(atkBase.replace(",", "").replace(" ", "")); 
			currentFam.stats.baseStats[2] = Integer.parseInt(defBase.replace(",", "").replace(" ", ""));
			currentFam.stats.baseStats[3] = Integer.parseInt(wisBase.replace(",", "").replace(" ", ""));
			currentFam.stats.baseStats[4] = Integer.parseInt(agiBase.replace(",", "").replace(" ", ""));
			currentFam.stats.baseStats[5] = currentFam.stats.baseStats[0] + currentFam.stats.baseStats[1] + 
					                        currentFam.stats.baseStats[2] + currentFam.stats.baseStats[3] + 
					                        currentFam.stats.baseStats[4];
			
			currentFam.stats.maxStats[0] = Integer.parseInt(hpMax.replace(",", "").replace(" ", ""));
			currentFam.stats.maxStats[1] = Integer.parseInt(atkMax.replace(",", "").replace(" ", "")); 
			currentFam.stats.maxStats[2] = Integer.parseInt(defMax.replace(",", "").replace(" ", ""));
			currentFam.stats.maxStats[3] = Integer.parseInt(wisMax.replace(",", "").replace(" ", ""));
			currentFam.stats.maxStats[4] = Integer.parseInt(agiMax.replace(",", "").replace(" ", ""));
			currentFam.stats.maxStats[5] = currentFam.stats.maxStats[0] + currentFam.stats.maxStats[1] + 
					                       currentFam.stats.maxStats[2] + currentFam.stats.maxStats[3] + 
					                       currentFam.stats.maxStats[4];
			
			if (!currentFam.isWarlord) {
				currentFam.stats.PEStats[0] = Integer.parseInt(hpPE.replace(",", "").replace(" ", ""));
				currentFam.stats.PEStats[1] = Integer.parseInt(atkPE.replace(",", "").replace(" ", ""));
				currentFam.stats.PEStats[2] = Integer.parseInt(defPE.replace(",", "").replace(" ", ""));
				currentFam.stats.PEStats[3] = Integer.parseInt(wisPE.replace(",", "").replace(" ", ""));
				currentFam.stats.PEStats[4] = Integer.parseInt(agiPE.replace(",", "").replace(" ", ""));
				currentFam.stats.PEStats[5] = currentFam.stats.PEStats[0] + currentFam.stats.PEStats[1] + 
										      currentFam.stats.PEStats[2] + currentFam.stats.PEStats[3] +
											  currentFam.stats.PEStats[4];			
			}
				
		} catch (Exception e) {
			Log.i("FamDetail", "Error parsing number, probably N/A");
		}
		
		if (currentFam.isWarlord || currentFam.isFinalEvolution) {
	    	
	    	// the link to the star level image
	    	String starLevelLink = famDOM.getElementsByClass("infobox").first() // the detail box
	    						.getElementsByTag("tbody").first().getElementsByTag("tr").get(4) // the evolution row
	    						.getElementsByTag("td").get(1) // the second cell (star image)
	    						.getElementsByTag("a").last().attr("href"); // link to the image
	    	String starLevel =	starLevelLink.substring(starLevelLink.length() - 8, starLevelLink.length() - 4); // will be of form "AofB"
	    	currentFam.starLevel = starLevel;
	    	int toAdd = 0;

	    	if (currentFam.isWarlord || starLevel.startsWith("1")) toAdd = 500;      // 1 star
	    	else if (starLevel.startsWith("2")) toAdd = 550; // 2 star
	    	else if (starLevel.startsWith("3")) toAdd = 605; // 3 star
	    	else if (starLevel.startsWith("4")) toAdd = 666; // 4 star

	    	currentFam.stats.POPEStats = new int[6];
    		for (int i = 0; i < 6; i++) {
    			if (i <= 4) { // the individual stats
    				if (currentFam.isWarlord || starLevel.startsWith("1")) currentFam.stats.POPEStats[i] = currentFam.stats.maxStats[i] + toAdd;
    				else currentFam.stats.POPEStats[i] = currentFam.stats.PEStats[i] + toAdd;
    			}
    			else if (i == 5) { // the total
    				if (currentFam.isWarlord || starLevel.startsWith("1")) currentFam.stats.POPEStats[i] = currentFam.stats.maxStats[i] + toAdd*5;
    				else currentFam.stats.POPEStats[i] = currentFam.stats.PEStats[i] + toAdd*5;
    			}
    		}
	    }
		
		return currentFam.stats;
	}
	
    public Bitmap getImage(String famName) {
        Bitmap famImage = imageStore.get(famName);
        if (famImage != null) return famImage;
        
        FamDetail currentFam = famStore.get(famName);
        if (currentFam == null) {
			currentFam = new FamDetail(famName);
			famStore.put(famName, currentFam);
        }
        
        // if the famDOM is not available yet, call getStats(). Or maybe just fetch it directly?
        if (currentFam.famDOM == null) getStats(famName);
        
        Element infoBoxFam = null;
        try {
            infoBoxFam = currentFam.famDOM.getElementsByClass("infobox").first();
            String imageUrl = infoBoxFam.getElementsByTag("tbody").first().getElementsByTag("tr").get(1).getElementsByTag("th").first().getElementsByTag("a").first().attr("href");
            InputStream in = new java.net.URL(imageUrl).openStream();
            famImage = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("FamDetail", "Error displaying the fam image");
            e.printStackTrace();
        }
        imageStore.put(famName, famImage);
        return famImage;
    }
    
	/**
	 * No check for null right now. Always remember to call only after the info is available
	 * @param famName
	 * @return
	 */
	public boolean isFinalEvolution(String famName) {
		return famStore.get(famName).isFinalEvolution;
	}
	
	/**
	 * No check for null right now. Always remember to call only after the info is available
	 * @param famName
	 * @return
	 */
	public boolean isWarlord(String famName) {
		return famStore.get(famName).isWarlord;
	}
	
	public String getStarLevel(String famName) {
		return famStore.get(famName).starLevel;
	}
}

