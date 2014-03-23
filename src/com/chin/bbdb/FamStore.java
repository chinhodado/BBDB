package com.chin.bbdb;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chin.bbdb.activity.MainActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * A singleton class that acts as a storage for familiar information. Support lazy loading information.
 * 
 * For now:
 * - the famDOM is saved, so if you want the details from it you need to parse it manually, except the 
 *   stats, which is inconsistent, but I'm lazy and let's just make it this way for now
 * - the HTML string of the skills are saved, and will have to be parsed manually
 * 
 * These inconsistent behaviors will probably change in the future, but not now.
 *   
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
		
		boolean isFinalEvolution = false;
		boolean isWarlord = false;
		
		FamDetail(String name) {
			this.name = name;
		}
	}
	
	public static class FamStats {
		// all initialized to 0
		public int[] baseStats = new int[6];
		public int[] maxStats  = new int[6];
		public int[] PEStats   = new int[6];
		public int[] POPEStats = new int[6];
		
		public static final int HIGHEST_IS_MAX  = 100;
		public static final int HIGHEST_IS_PE   = 101;
		public static final int HIGHEST_IS_POPE = 102;
		
		/**
		 * Get the highest available stat category of this familiar
		 * @return The highest stat category available for this familiar
		 */
		public int getHighestAvailableStatCategory() {
			if (POPEStats[0] != 0) return HIGHEST_IS_POPE;
			else if (PEStats[0] != 0) return HIGHEST_IS_PE;
			else return HIGHEST_IS_MAX; // the check is probably redundant. What fam doesn't have max stats?
		}
	}
	
    // the heart of this class, a storage for familiars' detail
	private static Hashtable<String, FamDetail> famStore = new Hashtable<String, FamDetail>();
	
	// maximum number of image to be cached. Don't set it too high, or you'll run short on memory
	static final int MAX_CACHED_IMAGE = 5;    
    
	// maps a fam name to its image. Remove the last inserted image when the capacity is exceeded
    @SuppressWarnings("serial")
    LinkedHashMap<String, Bitmap> imageStore = new LinkedHashMap<String, Bitmap>(MAX_CACHED_IMAGE + 1) {
        protected boolean removeEldestEntry(Map.Entry<String, Bitmap> eldest) {
           return size() > MAX_CACHED_IMAGE;
        }
     };
	
	// maps a skill name to its DOM
	private static Hashtable<String, String> skillStore = new Hashtable<String, String>();
	
	private static final FamStore FAMSTORE = new FamStore();
	
	/**
	 * Private constructor. For singleton.
	 */
	private FamStore() {
        if (FAMSTORE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

	/**
	 * Get the only instance of this class. Because of singleton.
	 * @return The only instance of this class.
	 */
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
	
	/**
	 * Get the image of a familiar. If that image is in the last 5 loaded images, a cached version of it will
	 * be returned. Otherwise it will be fetched from the net. Careful, it may block.
	 * 
	 * @param famName The name of the familiar
	 * @return The image of that familiar
	 */
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
     * Get the HTML string of the skill page(s) of a familiar. You'll have to parse it manually.
     * 
     * @param famName The name of the familiar
     * @return An array (size 2) of skill page(s) HTML. If there's no skill 2, the second element
     *         in the array is null
     */
    public String[] getSkillHTMLString(String famName) {
    	
    	FamDetail currentFam = famStore.get(famName);
        if (currentFam == null) {
			currentFam = new FamDetail(famName);
			famStore.put(famName, currentFam);
        }
        
        // if the famDOM is not available yet, call getStats(). Or maybe just fetch it directly?
        if (currentFam.famDOM == null) getStats(famName);
    	
    	// get the skill(s) name
    	Elements skillList = null;
    	if (currentFam.isWarlord) {
    		skillList = currentFam.famDOM.getElementsByClass("infobox").first().getElementsByTag("tr").get(7).getElementsByTag("a");
    	}
    	else {
    		skillList = currentFam.famDOM.getElementsByClass("infobox").first().getElementsByTag("tr").get(3).getElementsByTag("a");
    	}
    	
    	boolean allCached = true;    	

    	String skill1 = skillList.get(0).text();
    	
    	if (skillStore.get(skill1) == null)  // if the first skill is not cached
    		allCached = false;
    	try {
	    	if (skillList.get(1) == null || skillStore.get(skillList.get(1).text()) == null)  // if the second skill is not cached
	    		allCached = false;
    	} catch (Exception e) {
    		allCached = false;
    	}
    	
    	if (allCached) // if both are available, return
    		return new String[] {skillStore.get(skill1), skillStore.get(skillList.get(1).text())};
    	
    	// if not, fetch the missing
    	String[] skillLink = {null, null};
    	String[] skillHTML = {null, null};
    	
		skillLink[0] = skillList.get(0).attr("href");
		try {
			skillLink[1] = skillList.get(1).attr("href");
		} catch (Exception e) {};		
	
		for (int i = 0; i < skillLink.length; i++) {
			if (skillLink[i] == null || skillStore.get(skillList.get(i).text()) != null) continue;
			String skillURL = "http://bloodbrothersgame.wikia.com" + skillLink[i];
			
			try {
				skillHTML[i] = Jsoup.connect(skillURL).ignoreContentType(true).execute().body();
			} catch (Exception e) {
				Log.e("FamDetail", "Error fetching the fam skill page");
				e.printStackTrace();
			}
			skillStore.put(skillList.get(i).text(), skillHTML[i]);
		}
		
		String[] toReturn = {null, null};
		toReturn[0] = skillStore.get(skillList.get(0).text());
		
		try {
			toReturn[1] = skillStore.get(skillList.get(1).text());
		} catch (Exception e) {}
		
		return toReturn;
    	
    }
    
	/**
	 * No check for null right now. Always remember to call only after the info is available
	 * @param famName The name of the familiar
	 * @return true if the familiar is the final evolution in the line
	 */
	public boolean isFinalEvolution(String famName) {
		return famStore.get(famName).isFinalEvolution;
	}
	
	/**
	 * No check for null right now. Always remember to call only after the info is available
	 * @param famName The name of the familiar
	 * @return true if the familiar is a warlord
	 */
	public boolean isWarlord(String famName) {
		return famStore.get(famName).isWarlord;
	}

	/**
	 * No check for null right now. Always remember to call only after the info is available
	 * @param famName The name of the familiar
	 * @return A string of form "AofB" that represents the star level of the familiar
	 */
	public String getStarLevel(String famName) {
		return famStore.get(famName).starLevel;
	}
	
	/**
	 * No check for null right now. Always remember to call only after the info is available
	 * @param famName The name of the familiar
	 * @return The DOM of the familiar's wiki page
	 */
	public Document getFamDOM(String famName) {
		return famStore.get(famName).famDOM;
	}
}

