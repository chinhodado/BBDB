package com.chin.bbdb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
        int[] ccReduction = null;
        Element famDOM;

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

    public static HashMap<TierCategory, String[]> catTierList;
    static
    {
        // needed since each category may have different tiers...
        catTierList = new HashMap<TierCategory, String[]>();
        catTierList.put(TierCategory.PVP, new String[] {"X+", "X", "S+", "S", "A+", "A", "B", "C"});
        catTierList.put(TierCategory.RAID, new String[] {"X", "S+", "S", "A+", "A", "B", "C", "D", "E"});
        catTierList.put(TierCategory.TOWER, new String[] {"X+", "X", "S+", "S", "A+", "A", "B", "C"});
    }

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

    // a simple struct for holding 6 integers, used for holding the POPE stats
    public static class IntPOPE {
        int hpPOPE, atkPOPE, defPOPE, wisPOPE, agiPOPE, totalPOPE;

        public int[] toArray() {
            int[] toReturn = {hpPOPE, atkPOPE, defPOPE, wisPOPE, agiPOPE, totalPOPE};
            return toReturn;
        }
    }
    // the POPE stats table
    static HashMap<String, IntPOPE> popeTable = null;

    // the heart of this class, a storage for familiars' detail
    private static Hashtable<String, FamDetail> famStore = new Hashtable<String, FamDetail>();

    // maps a skill name to its DOM
    private static Hashtable<String, String> skillStore = new Hashtable<String, String>();

    private static FamStore FAMSTORE;
    private static Context context;

    /**
     * Private constructor. For singleton.
     */
    private FamStore(Context context) {
        if (FAMSTORE != null) {
            throw new IllegalStateException("Already instantiated");
        }
        FamStore.context = context;
    }

    /**
     * Get the only instance of this class. Because of singleton.
     * @return The only instance of this class.
     */
    public static FamStore getInstance(Context context) {
        if (FAMSTORE == null) {
            FAMSTORE = new FamStore(context);
        }
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
     * Note: this function calculates the POPE stats manually. Do not use the POPE
     *       stats calculated by this function for EP4 familiar! Use getPOPEStats()
     *       instead.
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
        HtmlCleaner.cleanHtml(famDOM);

        // save the DOM for later use. Should look into this so that it doesn't cause huge mem usage
        currentFam.famDOM = famDOM;

        Element rowBase = famDOM.getElementById("base");
        Element rowMax = famDOM.getElementById("max");

        Elements rowBaseCells = rowBase.getElementsByTag("td");
        String hpBase  = rowBaseCells.get(1).text();
        String atkBase = rowBaseCells.get(2).text();
        String defBase = rowBaseCells.get(3).text();
        String wisBase = rowBaseCells.get(4).text();
        String agiBase = rowBaseCells.get(5).text();

        Elements rowMaxCells = rowMax.getElementsByTag("td");
        String hpMax  = rowMaxCells.get(1).text();
        String atkMax = rowMaxCells.get(2).text();
        String defMax = rowMaxCells.get(3).text();
        String wisMax = rowMaxCells.get(4).text();
        String agiMax = rowMaxCells.get(5).text();

        String hpPE = null, atkPE = null, defPE = null, wisPE = null, agiPE = null;

        Element categories = famDOM.getElementById("articleCategories");
        if (categories == null) {
            categories = famDOM.getElementById("WikiaArticleCategories");
        }
        currentFam.isWarlord = categories.text().contains("Warlord");
        currentFam.isFinalEvolution = famDOM.getElementsByClass("container").first().html().indexOf("Final Evolution") != -1;


        if (!currentFam.isWarlord) {
            Element rowPE = famDOM.getElementById("pe");
            hpPE = rowPE.getElementsByTag("td").get(1).text();
            atkPE = rowPE.getElementsByTag("td").get(2).text();
            defPE = rowPE.getElementsByTag("td").get(3).text();
            wisPE = rowPE.getElementsByTag("td").get(4).text();
            agiPE = rowPE.getElementsByTag("td").get(5).text();
        }

        // array to store stats: HP, ATK, DEF, WIS, AGI, Total
        currentFam.stats = new FamStats();

        try {
            int[] baseStats = currentFam.stats.baseStats;
            baseStats[0] = getStatFromText(hpBase);
            baseStats[1] = getStatFromText(atkBase);
            baseStats[2] = getStatFromText(defBase);
            baseStats[3] = getStatFromText(wisBase);
            baseStats[4] = getStatFromText(agiBase);
            baseStats[5] = baseStats[0] + baseStats[1] + baseStats[2] + baseStats[3] + baseStats[4];

            int[] maxStats = currentFam.stats.maxStats;
            maxStats[0] = getStatFromText(hpMax);
            maxStats[1] = getStatFromText(atkMax);
            maxStats[2] = getStatFromText(defMax);
            maxStats[3] = getStatFromText(wisMax);
            maxStats[4] = getStatFromText(agiMax);
            maxStats[5] = maxStats[0] + maxStats[1] + maxStats[2] + maxStats[3] + maxStats[4];

            if (!currentFam.isWarlord) {
                int[] PEStats = currentFam.stats.PEStats;
                PEStats[0] = getStatFromText(hpPE);
                PEStats[1] = getStatFromText(atkPE);
                PEStats[2] = getStatFromText(defPE);
                PEStats[3] = getStatFromText(wisPE);
                PEStats[4] = getStatFromText(agiPE);
                PEStats[5] = PEStats[0] + PEStats[1] + PEStats[2] + PEStats[3] + PEStats[4];
            }

        } catch (Exception e) {
            Log.i("FamDetail", "Error parsing number, probably N/A");
        }

        // get fam star level
        int evolutionRowIndex = currentFam.isWarlord? 5 : 4;
        String starLevelLink = famDOM.getElementsByClass("infobox").first() // the detail box
                            .getElementsByTag("tbody").first().getElementsByTag("tr").get(evolutionRowIndex) // the evolution row
                            .getElementsByTag("td").get(1) // the second cell (star image)
                            .getElementsByTag("a").last().attr("href"); // link to the image
        int pngSuffixIndex = starLevelLink.indexOf(".png");
        String starLevel =  starLevelLink.substring(pngSuffixIndex - 4, pngSuffixIndex); // will be of form "AofB"
        currentFam.starLevel = starLevel;

        // get fam rarity
        // TODO: move this to a separate info parsing function
        String[] tmpArr = famDOM.getElementsByClass("infobox").first() // the detail box
                .getElementsByTag("tbody").first().getElementsByTag("tr").get(evolutionRowIndex) // the evolution row
                .getElementsByTag("td").get(1) // the second cell (star image)
                .getElementsByTag("a").first().attr("href") // link to the image
                .split("\\."); // split by .
        currentFam.rarity = tmpArr[tmpArr.length - 2]; // get the second last token

        // POPE and CC reduction
        if (currentFam.isWarlord || currentFam.isFinalEvolution) {

            // procedure: if is 4-star,
            //                try to get POPE from sqlite, if not success get it from POPE table
            //            if not 4-star, or if both the above fail, calculate it manually
            //                (with the risk of inaccurate 4-star POPE)
            if (starLevel.startsWith("4")) {
                IntPOPE pope = null;

                // first try to get from sqlite db
                try {
                    pope = getPOPEStatsFromSQLite(famName);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("FamDetail", "Error getting POPE from SQLite db.");
                }

                // ok, no choice but to go to our POPE table
                if (pope == null) {
                    try {
                        pope = getPOPEStatsFromPOPETable(famName);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("FamDetail", "Error getting POPE from POPE table.");
                    }
                }

                if (pope != null) {
                    currentFam.stats.POPEStats = pope.toArray();
                }

                // cc
                String evo3Name = famDOM.getElementById("evoStep3").text();
                int[] cc = new int[5];
                initializeCCList(evo3Name, cc);
                currentFam.ccReduction = cc;
            }

            if (currentFam.stats.POPEStats[0] == 0) { // fam not in POPE table, or not 4 stars
                // calculate the POPE manually
                int toAdd = 0;
                if (currentFam.isWarlord || starLevel.startsWith("1")) toAdd = 500; // 1 star
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
                Log.i("FamStore", "Calculated POPE manually.");
            }
        }
    }

    /**
     * Get the stat from the text
     * @param text The stat in text form
     * @return The stat
     */
    private int getStatFromText(String text) {
        return Integer.parseInt(text.replace(",", "").replace(" ", ""));
    }

    /**
     * Get the POPE stats of a fam from the POPE stats table
     * @param famName The fam to get POPE stats
     * @return the POPE stats of the fam, or null if the fam is not in the table
     */
    public IntPOPE getPOPEStatsFromPOPETable(String famName) {
        initializePOPETable();
        if (popeTable.containsKey(famName)) {
            IntPOPE popeStats = popeTable.get(famName);
            Log.i("FamStore", "Got POPE from POPE table.");
            return popeStats;
        }
        else {
            return null;
        }
    }

    /**
     * Get the POPE stats of a fam from the SQLite database
     * @param famName The fam to get POPE stats
     * @return the POPE stats of the fam, or null if the fam is not in the database
     */
    public IntPOPE getPOPEStatsFromSQLite(String famName) {
        SQLiteDatabase db = new DatabaseQuerier(FamStore.context).getDatabase();
        Cursor cursor = db.rawQuery("Select popeHp, popeAtk, popeDef, popeWis, popeAgi"
                + " from familiar where name = \"" + famName + "\"", null);
        IntPOPE pope = null;
        if (cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                pope = new IntPOPE();
                String popeHp  = cursor.getString(cursor.getColumnIndex("popeHp"));
                String popeAtk = cursor.getString(cursor.getColumnIndex("popeAtk"));
                String popeDef = cursor.getString(cursor.getColumnIndex("popeDef"));
                String popeWis = cursor.getString(cursor.getColumnIndex("popeWis"));
                String popeAgi = cursor.getString(cursor.getColumnIndex("popeAgi"));

                pope.hpPOPE  = Integer.parseInt(popeHp);
                pope.atkPOPE = Integer.parseInt(popeAtk);
                pope.defPOPE = Integer.parseInt(popeDef);
                pope.wisPOPE = Integer.parseInt(popeWis);
                pope.agiPOPE = Integer.parseInt(popeAgi);
                pope.totalPOPE = pope.hpPOPE + pope.atkPOPE + pope.defPOPE + pope.wisPOPE + pope.agiPOPE;

                Log.i("FamStore", "Got POPE from sqlite.");
                cursor.moveToNext();
            }
        }
        else {
            Log.i("POPE", "Not found in SQLite for POPE: " + famName);
        }

        return pope;
    }

    /**
     * Get the POPE stats table of all fams. Careful, it can block.
     */
    public void initializePOPETable() {

        if (popeTable != null) {
            // already initialized, return
            return;
        }

        Log.i("FamStore", "Initializing POPE table.");
        popeTable = new HashMap<String, IntPOPE>(512);
        String url = "http://bloodbrothersgame.wikia.com/wiki/POPE_Stats_Table";

        String popeHTML = null;
        try {
            popeHTML = Jsoup.connect(url).ignoreContentType(true).execute().body();
        } catch (Exception e) {
            Log.e("FamDetail", "Error fetching the POPE HTML page");
            e.printStackTrace();
        }
        Element doc = Jsoup.parse(popeHTML);

        Element table = doc.getElementsByClass("wikitable").first();
        Elements rows = table.getElementsByTag("tbody").first().getElementsByTag("tr");

        for (int i = 1; i < rows.size(); i++) { // first row is header
            try {
                Elements cells = rows.get(i).getElementsByTag("td");
                String cellFam = cells.get(2).text().trim();

                IntPOPE popeStats = new IntPOPE();

                popeStats.hpPOPE  = getStatFromText(cells.get(5).text());
                popeStats.atkPOPE = getStatFromText(cells.get(6).text());
                popeStats.defPOPE = getStatFromText(cells.get(7).text());
                popeStats.wisPOPE = getStatFromText(cells.get(8).text());
                popeStats.agiPOPE = getStatFromText(cells.get(9).text());
                popeStats.totalPOPE = popeStats.hpPOPE + popeStats.atkPOPE + popeStats.defPOPE + popeStats.wisPOPE + popeStats.agiPOPE;

                popeTable.put(cellFam, popeStats);

            } catch (Exception e) {
                Log.i("FamStore", "There's an error in parsing the POPE table.");
            }
        }
    }

    /**
     * Initialize the Last CC array of a fam with the specified evo 3 name
     * @param evo3Name The name of the 3-star evolution of the fam we want to get the Lass CC list
     * @param ccList The CC List. It cannot be null, and will be modified.
     */
    public void initializeCCList(String evo3Name, int[] ccList) {
        FamStats evo3Stats = getStats(evo3Name);

        for (int i = 0; i < 5; i++) {
            double stat = evo3Stats.PEStats[i];
            if (Math.round((stat + 605) / 10) - Math.round((stat + 600) / 10) > 0) {
                double b = Math.round((stat + 600) / 10);
                double c = Math.round((stat * 10 + 6000) / 10);
                double d = 0.5 - (c / 10 - b);
                ccList[i] = (int) Math.round(d * 10 / 0.5);
            } else {
                ccList[i] = 0;
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
        return famStore.get(famName).stats;
    }

    /**
     *
     * Get the cc reduction of a familiar. Careful, it can block.
     *
     * @param famName The name of the familiar
     * @return The cc reduction of that familiar
     */
    public int[] getCC(String famName) {
        FamDetail currentFam = famStore.get(famName);

        if (currentFam == null) {
            getGeneralInfo(famName);
        }
        return famStore.get(famName).ccReduction;
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

        // relying on the fact the the first link in the infobox is the image
        String imageUrl = infoBoxFam.getElementsByTag("a").first().attr("href");
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
            skillList = currentFam.famDOM.getElementsByClass("infobox").first().getElementsByTag("tr").get(8).getElementsByTag("a");
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
    public Element getFamDOM(String famName) {
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

        String tierHTML = FAMSTORE.getTierHTML(category);
        Document tierDOM   = Jsoup.parse(tierHTML);
        Elements tierTables   = tierDOM.getElementsByClass("wikitable");

        String[] tiers = catTierList.get(category);

        HashMap<String, String> tierMap = new HashMap<String, String>(512);

        for (int i = 0; i < tiers.length; i++){
            Elements rows = tierTables.get(i).getElementsByTag("tbody").first().getElementsByTag("tr"); // get all rows in each table
            int countRow = 0;
            for (Element row : rows) {
                countRow++;
                if (countRow == 1) continue; // row 1 is the column headers. This may be different in the DOM in browser

                // third cell's text
                String famName = row.getElementsByTag("td").get(2).text();
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

