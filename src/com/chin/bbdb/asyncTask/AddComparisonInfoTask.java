package com.chin.bbdb.asyncTask;

import java.text.DecimalFormat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.chin.bbdb.FamStore;
import com.chin.bbdb.R;
import com.chin.bbdb.FamStore.FamStats;
import com.chin.bbdb.activity.FamCompareActivity;
import com.chin.bbdb.activity.FamDetailActivity;

public class AddComparisonInfoTask extends AsyncTask<String, Void, Void>{

    FamCompareActivity activity;
    String famNameLeft, famNameRight;
    FamStore famStore;

    public AddComparisonInfoTask(FamCompareActivity activity) {
        this.activity = activity;
        this.famStore = FamStore.getInstance();
    }

    @Override
    protected Void doInBackground(String... params) {
        this.famNameLeft = params[0];
        this.famNameRight = params[1];

        // each of these can fail independently (?)
        try { famStore.getStats(this.famNameLeft);            } catch (Exception e) {e.printStackTrace();}
        try { famStore.getImage(this.famNameLeft);            } catch (Exception e) {e.printStackTrace();}
        try { famStore.getStats(this.famNameRight);           } catch (Exception e) {e.printStackTrace();}
        try { famStore.getImage(this.famNameRight);           } catch (Exception e) {e.printStackTrace();}
        try { famStore.getSkillHTMLString(this.famNameLeft);  } catch (Exception e) {e.printStackTrace();}
        try { famStore.getSkillHTMLString(this.famNameRight); } catch (Exception e) {e.printStackTrace();}
        return null;
    }

    @Override
    protected void onPostExecute(Void params) {
        // all of these should be fast
        try { addFamImage(); } catch (Exception e) {e.printStackTrace();}
        try { addFamStat();  } catch (Exception e) {e.printStackTrace();}
        try { addFamSkill(); } catch (Exception e) {e.printStackTrace();}
    }

    public void addFamImage() {
        // remove the spinner row
        TableLayout compareTable = (TableLayout) activity.findViewById(R.id.compareTable);
        TableRow row = (TableRow) activity.findViewById(R.id.tableRow_waiting);
        compareTable.removeView(row);

        // set the image
        ImageView bmImage = (ImageView) activity.findViewById(R.id.imageView_leftFam);
        bmImage.setImageBitmap(famStore.getImage(famNameLeft));

        bmImage = (ImageView) activity.findViewById(R.id.imageView_rightFam);
        bmImage.setImageBitmap(famStore.getImage(famNameRight));
    }

    public void addFamStat() {

        TextView[] textViewStatsLeft = {null, null, null, null, null, null};
        TextView[] textViewStatsRight = {null, null, null, null, null, null};

        textViewStatsLeft[0]   = (TextView) activity.findViewById(R.id.textView_HP_left);
        textViewStatsLeft[1]   = (TextView) activity.findViewById(R.id.textView_ATK_left);
        textViewStatsLeft[2]   = (TextView) activity.findViewById(R.id.textView_DEF_left);
        textViewStatsLeft[3]   = (TextView) activity.findViewById(R.id.textView_WIS_left);
        textViewStatsLeft[4]   = (TextView) activity.findViewById(R.id.textView_AGI_left);
        textViewStatsLeft[5]   = (TextView) activity.findViewById(R.id.textView_total_left);

        textViewStatsRight[0]   = (TextView) activity.findViewById(R.id.textView_HP_right);
        textViewStatsRight[1]   = (TextView) activity.findViewById(R.id.textView_ATK_right);
        textViewStatsRight[2]   = (TextView) activity.findViewById(R.id.textView_DEF_right);
        textViewStatsRight[3]   = (TextView) activity.findViewById(R.id.textView_WIS_right);
        textViewStatsRight[4]   = (TextView) activity.findViewById(R.id.textView_AGI_right);
        textViewStatsRight[5]   = (TextView) activity.findViewById(R.id.textView_total_right);

        FamStats statsLeft = famStore.getStats(famNameLeft);
        FamStats statsRight = famStore.getStats(famNameRight);

        int highestCommonStatCategory = (statsLeft.getHighestAvailableStatCategory() < statsRight.getHighestAvailableStatCategory())?
                                  statsLeft.getHighestAvailableStatCategory() : statsRight.getHighestAvailableStatCategory();
        int[] highestCommonLeft = null, highestCommonRight = null;
        String label = null;
        if (highestCommonStatCategory == FamStats.HIGHEST_IS_POPE) {
            highestCommonLeft = statsLeft.POPEStats;
            highestCommonRight = statsRight.POPEStats;
            label = "POPE Stats";
        }
        else if (highestCommonStatCategory == FamStats.HIGHEST_IS_PE) {
            highestCommonLeft = statsLeft.PEStats;
            highestCommonRight = statsRight.PEStats;
            label = "PE Stats";
        }
        else if (highestCommonStatCategory == FamStats.HIGHEST_IS_MAX) {
            highestCommonLeft = statsLeft.maxStats;
            highestCommonRight = statsRight.maxStats;
            label = "Max stats";
        }

        DecimalFormat formatter = new DecimalFormat("#,###");

        int RED = Color.parseColor("#CC0000");
        int GREEN = Color.parseColor("#669900");

        for (int i = 0; i < 6; i++) {
            int leftVSRight = highestCommonLeft[i] - highestCommonRight[i];

            // get the strings
            String leftVSRightString = leftVSRight == 0? "" : (" (" + (((leftVSRight > 0)? "+" : "") + leftVSRight) + ")");
            int rightVSLeft = -1 * leftVSRight;
            String rightVSLeftString = leftVSRight == 0? "" : (" (" + (((rightVSLeft > 0)? "+" : "") + rightVSLeft) + ")");

            textViewStatsLeft[i].append(formatter.format(highestCommonLeft[i]) + leftVSRightString);
            textViewStatsRight[i].append(formatter.format(highestCommonRight[i]) + rightVSLeftString);
            if (leftVSRight!= 0 ) {
                textViewStatsLeft[i].setTextColor(leftVSRight > 0? GREEN : RED);
                textViewStatsRight[i].setTextColor(leftVSRight < 0? GREEN : RED);
            }
        }

        ((TextView) activity.findViewById(R.id.statsLabel)).setText(label);

        // add the name label
        ((TextView) activity.findViewById(R.id.textView_name_left)).setText(famNameLeft);
        ((TextView) activity.findViewById(R.id.textView_name_right)).setText(famNameRight);

        // add star and rarity images
        ImageView img_star_left = (ImageView) activity.findViewById(R.id.imageView_leftFam_star);
        img_star_left.setImageResource(FamDetailActivity.evolutionMap.get(famStore.getStarLevel(famNameLeft)));

        ImageView img_rarity_left = (ImageView) activity.findViewById(R.id.imageView_leftFam_rarity);
        img_rarity_left.setImageResource(FamDetailActivity.evolutionMap.get(famStore.getRarity(famNameLeft)));

        ImageView img_star_right = (ImageView) activity.findViewById(R.id.imageView_rightFam_star);
        img_star_right.setImageResource(FamDetailActivity.evolutionMap.get(famStore.getStarLevel(famNameRight)));

        ImageView img_rarity_right = (ImageView) activity.findViewById(R.id.imageView_rightFam_rarity);
        img_rarity_right.setImageResource(FamDetailActivity.evolutionMap.get(famStore.getRarity(famNameRight)));
    }

    public void addFamSkill() {
        String[] skillHTMLLeft = famStore.getSkillHTMLString(famNameLeft);
        String[] skillHTMLRight = famStore.getSkillHTMLString(famNameRight);

        // help with looping
        String[] skillHTMLs = {skillHTMLLeft[0], skillHTMLLeft[1], skillHTMLRight[0], skillHTMLRight[1]};

        for (int i = 0; i < skillHTMLs.length; i++) {

            if (skillHTMLs[i] == null) continue;

            Document skillDOM = Jsoup.parse(skillHTMLs[i]);
            Element infoBox = skillDOM.getElementsByClass("infobox").first();
            Elements skRows = infoBox.getElementsByTag("tbody").first().getElementsByTag("tr");

            TextView tvtmp = null;
            if (i == 0) {
                tvtmp = (TextView) activity.findViewById(R.id.skill1Left);
            } else if (i == 1) {
                tvtmp = (TextView) activity.findViewById(R.id.skill2Left);
            } else if (i == 2) {
                tvtmp = (TextView) activity.findViewById(R.id.skill1Right);
            } else {
                tvtmp = (TextView) activity.findViewById(R.id.skill2Right);
            }

            int count = 0;
            String str = "";

            for (Element row : skRows) {
                if (count == 0) {
                    // get the skill name
                    String skillName = row.getElementsByTag("th").text().trim();
                    skillName = "<b>" + skillName + "</b> ";
                    str += (skillName + "<br/>");
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
                        st1 = "<b>" + st1 + "</b> ";
                        str += (st1 + ": " + st2 + "<br/>");
                    }
                    count++;
                }
            }
            tvtmp.setText(Html.fromHtml(str));
        }
    }
}
