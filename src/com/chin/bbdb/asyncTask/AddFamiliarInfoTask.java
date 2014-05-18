package com.chin.bbdb.asyncTask;

import java.text.DecimalFormat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chin.bbdb.FamStore;
import com.chin.bbdb.Util;
import com.chin.bbdb.R;
import com.chin.bbdb.FamStore.FamStats;
import com.chin.bbdb.activity.FamDetailActivity;
import com.chin.bbdb.activity.MainActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * The async task that populate the information in FamDetailActivity
 * It is put into a separate file since it is too long
 */
public class AddFamiliarInfoTask extends AsyncTask<String, Void, Void> {

    FamDetailActivity activity;
    String famName;
    FamStore famStore;

    // use to store the evolution names. 4 is the max number of fams in an
    // evo line (for now). This doesn't get cleared for each new fam but is
    // overwritten (can be partially overwritten), so it can potentially be
    // dangerous...
    public static String[] famEvoNames = new String[4];

    public AddFamiliarInfoTask(FamDetailActivity activity) {
        this.activity = activity;
        this.famStore = FamStore.getInstance();
    }

    @Override
    protected Void doInBackground(String... params) {
        this.famName = params[0];

        try { famStore.getGeneralInfo(this.famName);     } catch (Exception e) {e.printStackTrace();}
        if (isCancelled()) {return null; }; // attempt to return early

        try { famStore.getSkillHTMLString(this.famName); } catch (Exception e) {e.printStackTrace();}
        return null;
    }

    @Override
    protected void onPostExecute(Void params) {
        // all of these should be fast
        try { addFamImage();               } catch (Exception e) {e.printStackTrace();}
        try { addFamSkill();               } catch (Exception e) {e.printStackTrace();}
        try { addFamStat();                } catch (Exception e) {e.printStackTrace();}
        try { addFamDetail();              } catch (Exception e) {e.printStackTrace();}
        try { addFamSpecialInformation();  } catch (Exception e) {e.printStackTrace();}
        try { addFamEvolutionLine();       } catch (Exception e) {e.printStackTrace();}
    }

    public void addFamImage() {
        // remove the spinner
        ProgressBar pgrBar = (ProgressBar) activity.findViewById(R.id.progressBar1);
        LinearLayout layout = (LinearLayout) activity.findViewById(R.id.linearLayout1);
        layout.removeView(pgrBar);

        // set the image
        ImageView imgView = (ImageView) activity.findViewById(R.id.imageView_detail_fam);
        ImageLoader.getInstance().displayImage(famStore.getImageLink(famName), imgView);
    }

    public void addFamSkill() {

        String[] skillHTML = famStore.getSkillHTMLString(famName);
        TableLayout skillTable = (TableLayout) activity.findViewById(R.id.skillTable);

        // remove the spinner
        ProgressBar pgrBar = (ProgressBar) activity.findViewById(R.id.progressBar2);
        LinearLayout layout = (LinearLayout) activity.findViewById(R.id.linearLayout1);
        layout.removeView(pgrBar);

        for (int i = 0; i < skillHTML.length; i++) {

            if (skillHTML[i] == null) continue;

            Document skillDOM = Jsoup.parse(skillHTML[i]);
            Element infoBox = skillDOM.getElementsByClass("infobox").first();
            Elements skRows = infoBox.getElementsByTag("tbody").first().getElementsByTag("tr");

            int count = 0;

            for (Element row : skRows) {
                if (count == 0) {
                    // get the skill name
                    String skillName = row.getElementsByTag("th").text().trim();
                    Util.addRowWithTwoTextView(activity, skillTable, "Skill name", skillName, true, Typeface.DEFAULT_BOLD);
                    count++;
                }
                else {
                    Elements cells = row.getElementsByTag("td");
                    String st1 = "", st2 = "";
                    try {
                        st1 = cells.get(0).text().trim();
                        st2 = cells.get(1).text().trim();
                    } catch (Exception e) {}

                    if (!st1.equals("") || !st2.equals("")) {
                        Util.addRowWithTwoTextView(activity, skillTable, st1, st2, true);
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

    public void addFamStat() {

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

        boolean isWarlord = famStore.isWarlord(famName);
        FamStats stats = famStore.getStats(famName);

        if (isWarlord || stats.PEStats[0] == 0) {
            activity.findViewById(R.id.textView_ATK_right).setVisibility(View.GONE);
            peHP_textView.setVisibility(View.GONE);
            peATK_textView.setVisibility(View.GONE);
            peDEF_textView.setVisibility(View.GONE);
            peWIS_textView.setVisibility(View.GONE);
            peAGI_textView.setVisibility(View.GONE);
            peTotal_textView.setVisibility(View.GONE);
        }

        DecimalFormat formatter = new DecimalFormat("#,###");

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

        if (!isWarlord && stats.PEStats[0] != 0) {
            activity.findViewById(R.id.textView_ATK_right).setVisibility(View.VISIBLE);
            peHP_textView.setText(formatter.format(stats.PEStats[0]));
            peATK_textView.setText(formatter.format(stats.PEStats[1]));
            peDEF_textView.setText(formatter.format(stats.PEStats[2]));
            peWIS_textView.setText(formatter.format(stats.PEStats[3]));
            peAGI_textView.setText(formatter.format(stats.PEStats[4]));
            peTotal_textView.setText(formatter.format(stats.PEStats[5]));
        }

        // POPE row
        if (famStore.isFinalEvolution(famName) || isWarlord) {

            Util.addLineSeparator(activity, statTableLayout);
            TableRow popeRow = new TableRow(activity); statTableLayout.addView(popeRow);
            TextView tmpTv1 = new TextView(activity); tmpTv1.setText("POPE"); popeRow.addView(tmpTv1);

            for (int i = 0; i < 6; i++) {
                TextView tmpTv = new TextView(activity); tmpTv.setText(formatter.format(stats.POPEStats[i])); popeRow.addView(tmpTv);
            }
        }

        TableRow emptyRow = new TableRow(activity); statTableLayout.addView(emptyRow);
        TextView emptyTv = new TextView(activity); emptyRow.addView(emptyTv);

        // set the name label
        TextView nameLabelTV = (TextView) activity.findViewById(R.id.textView_detail_famName);
        nameLabelTV.setText(famName);
    }

    public void addFamDetail() {
        Document famDOM = famStore.getFamDOM(famName);
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
            else if (count == 4) { // evolution star and rarity
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
                Util.addLineSeparator(activity, detailTable);
            }
            else {
                if (count == 2 && !famStore.isWarlord(famName)) {
                    count++;
                    continue;
                }
                Elements cells = detailRow.getElementsByTag("td");
                String st1 = "", st2 = "";
                try {
                    st1 = cells.get(0).text().trim();
                    st2 = cells.get(1).text().trim();
                } catch (Exception e) {}

                if (st1.equals("Elite")){ // if this is the elite row, we need to add the elite seal image
                    // add the word "Elite"
                    TableRow tr = new TableRow(activity);
                    TextView tv1 = new TextView(activity); tv1.setText(st1);
                    tr.addView(tv1);

                    // create a layout for the elite image + event name
                    LinearLayout tmpViewGroup = new LinearLayout(activity);
                    tmpViewGroup.setLayoutParams(new TableRow.LayoutParams (ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                            ViewGroup.LayoutParams.MATCH_PARENT));
                    // set the elite image
                    ImageView imvElite = new ImageView(activity);
                    imvElite.setScaleType(ImageView.ScaleType.FIT_START);
                    imvElite.setLayoutParams(new TableRow.LayoutParams (ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                        ViewGroup.LayoutParams.MATCH_PARENT));
                    imvElite.setImageResource(R.drawable.elite);
                    tmpViewGroup.addView(imvElite);

                    // add the event name
                    TextView tv2 = new TextView(activity); tv2.setText(st2);
                    tmpViewGroup.addView(tv2);

                    // add viewgroup to row and row to table
                    tr.addView(tmpViewGroup);
                    detailTable.addView(tr);

                    // add the line separator
                    Util.addLineSeparator(activity, detailTable);
                }

                // this is important since there are empty filler rows like <tr></tr>, we skip those
                else if (!st1.equals("") || !st2.equals("")) {
                    Util.addRowWithTwoTextView(activity, detailTable, st1, st2, true);
                }
                count++;
            }
        }

        // the tier rows
        new AddTierInfoTask(detailTable, activity, famName).execute();

        detailTable.setColumnShrinkable(1, true);
        detailTable.setStretchAllColumns(true);
    }

    public void addFamSpecialInformation() {
        if (famStore.isWarlord(famName)) return;
        Document famDOM = famStore.getFamDOM(famName);
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

    public void addFamEvolutionLine() {
        if (famStore.isWarlord(famName)) return;
        TableLayout evoTable = (TableLayout) activity.findViewById(R.id.evoTable);
        Document famDOM = famStore.getFamDOM(famName);

        // get the evolution names
        Elements evos = famDOM.getElementById("mw-content-text").getElementsByTag("ol").first().getElementsByTag("li");

        int count = 1;
        for (Element evo : evos) {
            String text = evo.text();
            famEvoNames[count-1] = text;
            TableRow tr = new TableRow(activity);
            TextView tv1 = new TextView(activity); tv1.setText(count + ". " + text); tr.addView(tv1);
            evoTable.addView(tr);
            Util.addLineSeparator(activity, evoTable);
            tr.setId(count - 1); // set the row id to use for fam name retrieval later
            tr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, FamDetailActivity.class);
                    intent.putExtra(MainActivity.FAM_NAME, famEvoNames[v.getId()]);
                    activity.startActivity(intent);
                }
            });
            count++;
        }

    }
}