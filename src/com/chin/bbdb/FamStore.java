package com.chin.bbdb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
        String rarity = null;
        Document famDOM;

        boolean isFinalEvolution = false;
        boolean isWarlord = false;

        FamDetail(String name) {
            this.name = name;
        }
    }

    /**
     * A struct holding a fam's stat. All stats are initialized to 0
     * @author Chin
     *
     */
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

    // a list of all fam available, initialized in MainActivity's onCreate()
    public static ArrayList<String> famList = null;

    // map a fam name to its wiki page url, initialized in MainActivity's onCreate()
    public static Hashtable<String, String[]> famLinkTable = null;

    // map a fam name to its tiers, initialized in AddTierInfoTask
    public static HashMap<String, String> pvpTierMap    = null;
    public static HashMap<String, String> raidTierMap   = null;
    public static HashMap<String, String> towerTierMap  = null;

    // the raw tier HTML pages for anyone interested in parsing them
    private static String pvpHTML;
    private static String raidHTML;
    private static String towerHTML;

    // an enum for the tier categories
    public enum TierCategory {
        PVP,
        RAID,
        TOWER
    }

    // a simple struct for holding 5 integers, used for holding the POPE stats
    static class IntPOPE {
        int hpPOPE, atkPOPE, defPOPE, wisPOPE, agiPOPE;
    }
    // the POPE stats table
    static HashMap<String, IntPOPE> popeTable = null;

    // the heart of this class, a storage for familiars' detail
    private static Hashtable<String, FamDetail> famStore = new Hashtable<String, FamDetail>();

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
     * Get some general info about a familiar. Careful, this can block!
     * This function fetches the following and make them available for query later:
     * - stats
     * - starLevel
     * - isFinalEvolution
     * - isWarlord
     * so if you want to use those make sure to call this function first
     *
     * @param famName The familiar name
     */
    public void getGeneralInfo(String famName) {

        FamDetail currentFam = famStore.get(famName);

        if (currentFam != null) {
            return; // already initialized, just return
        }

        currentFam = new FamDetail(famName);
        famStore.put(famName, currentFam);

        String famURL = "http://bloodbrothersgame.wikia.com" + FamStore.famLinkTable.get(famName)[0];

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

        // get fam star level
        String starLevelLink = famDOM.getElementsByClass("infobox").first() // the detail box
                            .getElementsByTag("tbody").first().getElementsByTag("tr").get(4) // the evolution row
                            .getElementsByTag("td").get(1) // the second cell (star image)
                            .getElementsByTag("a").last().attr("href"); // link to the image
        String starLevel =  starLevelLink.substring(starLevelLink.length() - 8, starLevelLink.length() - 4); // will be of form "AofB"
        currentFam.starLevel = starLevel;

        // get fam rarity
        // TODO: move this to a separate info parsing function
        String[] tmpArr = famDOM.getElementsByClass("infobox").first() // the detail box
                .getElementsByTag("tbody").first().getElementsByTag("tr").get(4) // the evolution row
                .getElementsByTag("td").get(1) // the second cell (star image)
                .getElementsByTag("a").first().attr("href") // link to the image
                .split("\\."); // split by .
        currentFam.rarity = tmpArr[tmpArr.length - 2]; // get the second last token

        if (currentFam.isWarlord || currentFam.isFinalEvolution) {
            // POPE stats
            initializePOPETable();
            if (popeTable.containsKey(famName)) {
                IntPOPE popeStats = popeTable.get(famName);
                currentFam.stats.POPEStats[0] = popeStats.hpPOPE;
                currentFam.stats.POPEStats[1] = popeStats.atkPOPE;
                currentFam.stats.POPEStats[2] = popeStats.defPOPE;
                currentFam.stats.POPEStats[3] = popeStats.wisPOPE;
                currentFam.stats.POPEStats[4] = popeStats.agiPOPE;
                currentFam.stats.POPEStats[5] = popeStats.hpPOPE + popeStats.atkPOPE + popeStats.defPOPE +
                                                    popeStats.wisPOPE + popeStats.agiPOPE;
            }
            else { // fam is not in POPE table, we calculate the POPE manually
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
        }
    }

    /**
     * Get the POPE stats table of all fams. Careful, it can block.
     */
    public void initializePOPETable() {

        if (popeTable != null) {
            // already initialized, return
            return;
        }

        popeTable = new HashMap<String, IntPOPE>();
        String url = "http://bloodbrothersgame.wikia.com/wiki/POPE_Stats_Table";

        String popeHTML = null;
        try {
            popeHTML = Jsoup.connect(url).ignoreContentType(true).execute().body();
        } catch (Exception e) {
            Log.e("FamDetail", "Error fetching the POPE HTML page");
            e.printStackTrace();
        }
        Document doc = Jsoup.parse(popeHTML);

        Element table = doc.getElementsByClass("wikitable").first();
        Elements rows = table.getElementsByTag("tbody").first().getElementsByTag("tr");

        for (int i = rows.size() - 1; i >= 2; i--) { // first two rows are headers and such
            try {
                Elements cells = rows.get(i).getElementsByTag("td");
                String cellFam = cells.get(2).text().trim();

                IntPOPE popeStats = new IntPOPE();

                popeStats.hpPOPE  = Integer.parseInt(cells.get(5).text().replace(",", "").replace(" ", ""));
                popeStats.atkPOPE = Integer.parseInt(cells.get(6).text().replace(",", "").replace(" ", ""));
                popeStats.defPOPE = Integer.parseInt(cells.get(7).text().replace(",", "").replace(" ", ""));
                popeStats.wisPOPE = Integer.parseInt(cells.get(8).text().replace(",", "").replace(" ", ""));
                popeStats.agiPOPE = Integer.parseInt(cells.get(9).text().replace(",", "").replace(" ", ""));

                popeTable.put(cellFam, popeStats);

            } catch (Exception e) {
                Log.i("FamStore", "There's an error in parsing the POPE table, probably the tier section dividers");
            }
        }
    }

    /**
     *
     * Get the stats of a familiar. Careful, it can block.
     *
     * @param famName The name of the familiar
     * @return The stats of that familiar
     */
    public FamStats getStats(String famName) {
        FamDetail currentFam = famStore.get(famName);

        if (currentFam != null && currentFam.stats != null) {
            return currentFam.stats;
        }

        if (currentFam == null) {
            getGeneralInfo(famName);
        }
        return currentFam.stats;
    }

    /**
     * Get the image link of a familiar.
     *
     * @param famName The name of the familiar
     * @return The image link of that familiar
     */
    public String getImageLink(String famName) {

        FamDetail currentFam = famStore.get(famName);
        if (currentFam == null) {
            currentFam = new FamDetail(famName);
            famStore.put(famName, currentFam);
        }

        // if the famDOM is not available yet, call getGeneralInfo(). Or maybe just fetch it directly?
        if (currentFam.famDOM == null) getGeneralInfo(famName);

        Element infoBoxFam = null;
        infoBoxFam = currentFam.famDOM.getElementsByClass("infobox").first();
        String imageUrl = infoBoxFam.getElementsByTag("tbody").first().getElementsByTag("tr").get(1).getElementsByTag("th").first().getElementsByTag("a").first().attr("href");
        return imageUrl;
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

        // if the famDOM is not available yet, call getGeneralInfo(). Or maybe just fetch it directly?
        if (currentFam.famDOM == null) getGeneralInfo(famName);

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
     * @return The rarity of the familiar
     */
    public String getRarity(String famName) {
        return famStore.get(famName).rarity;
    }

    /**
     * No check for null right now. Always remember to call only after the info is available
     * @param famName The name of the familiar
     * @return The DOM of the familiar's wiki page
     */
    public Document getFamDOM(String famName) {
        return famStore.get(famName).famDOM;
    }

    /**
     * No check for null right now. Always remember to call only after the info is available
     * @param famName The name of the familiar
     * @param category One of PVP, RAID or TOWER
     * @return
     */
    public String getFamTier(String famName, TierCategory category) {
        String tier = null;

        try {
            initializeTierMap(category);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (category == TierCategory.PVP) {
            tier = pvpTierMap.get(famName);
        }
        else if (category == TierCategory.RAID) {
            tier = raidTierMap.get(famName);
        }
        else if (category == TierCategory.TOWER) {
            tier = towerTierMap.get(famName);
        }

        if (tier == null) {
            tier = "N/A";
        }

        return tier;
    }

    /**
     * Initialize all tier maps
     * @throws IOException
     */
    public void initializeAllTierMap() throws IOException {
        initializeTierMap(TierCategory.PVP);
        initializeTierMap(TierCategory.RAID);
        initializeTierMap(TierCategory.TOWER);
    }

    /**
     * Initialize the tier map of a category
     * @param category Either PVP, RAID or TOWER
     * @throws IOException
     */
    public void initializeTierMap(TierCategory category) throws IOException {

        // if the tier map for this category has already been initialized, just return
        if (category == TierCategory.PVP && FamStore.pvpTierMap != null) {
            return;
        }
        else if (category == TierCategory.RAID && FamStore.raidTierMap != null) {
            return;
        }
        else if (category == TierCategory.TOWER && FamStore.towerTierMap != null) {
            return;
        }

        String tierHTML = FamStore.getInstance().getTierHTML(category);
        Document tierDOM   = Jsoup.parse(tierHTML);
        Elements tierTables   = tierDOM.getElementsByClass("wikitable");

        String[] tiers = {"X", "S+", "S", "A+", "A", "B", "C"};

        HashMap<String, String> tierMap = new HashMap<String, String>();

        for (int i = 0; i < 7; i++){ // 7 tables
            Elements rows = tierTables.get(i).getElementsByTag("tbody").first().getElementsByTag("tr"); // get all rows in each table
            int countRow = 0;
            for (Element row : rows) {
                countRow++;
                if (countRow < 3) continue; // row 1 is the table title, row 2 is the column headers. This is different in the DOM in browser

                // second cell's text
                String famName = row.getElementsByTag("td").get(1).text();
                tierMap.put(famName, tiers[i]);
            }
        }

        if (category == TierCategory.PVP) {
            FamStore.pvpTierMap = tierMap;
        }
        else if (category == TierCategory.RAID) {
            FamStore.raidTierMap = tierMap;
        }
        else if (category == TierCategory.TOWER) {
            FamStore.towerTierMap = tierMap;
        }

    }

    /**
     * Get the HTML of a tier page. If it is already fetched before it will be returned
     * immediately, otherwise it will be fetched from the internet.
     *
     * Be careful as this may block, and may raise an exception if you call it on the main thread
     *
     * @param category Either PVP, RAID or TOWER
     * @return The HTML of the tier page of the specified category
     * @throws IOException Something's probably wrong with the network
     */
    public String getTierHTML(TierCategory category) throws IOException {
        String HTML = null;
        if (category == TierCategory.PVP) {
            if (FamStore.pvpHTML == null) {
                pvpHTML = Jsoup.connect("http://bloodbrothersgame.wikia.com/wiki/Familiar_Tier_List/PvP")
                        .ignoreContentType(true).execute().body();
            }
            HTML = FamStore.pvpHTML;
        }
        else if (category == TierCategory.RAID) {
            if (FamStore.raidHTML == null) {
                raidHTML = Jsoup.connect("http://bloodbrothersgame.wikia.com/wiki/Familiar_Tier_List/Raid")
                        .ignoreContentType(true).execute().body();
            }
            HTML = FamStore.raidHTML;
        }
        else if (category == TierCategory.TOWER) {
            if (FamStore.towerHTML == null) {
                towerHTML = Jsoup.connect("http://bloodbrothersgame.wikia.com/wiki/Familiar_Tier_List/Tower")
                        .ignoreContentType(true).execute().body();
            }
            HTML = FamStore.towerHTML;
        }

        return HTML;
    }
}

