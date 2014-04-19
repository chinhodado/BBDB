package com.chin.bbdb.asyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chin.bbdb.FamStore;
import com.chin.bbdb.R;
import com.chin.bbdb.activity.FamDetailActivity;
import com.chin.bbdb.activity.MainActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * An AsyncTask that populates the information in the NewFamFragment
 */
public class NewFamTask extends AsyncTask<Void, Void, String> {

    static String mainHTML = null;

    Activity activity;
    LinearLayout layout;

    public NewFamTask(Activity activity, LinearLayout layout) {
        this.activity = activity;
        this.layout = layout;
    }

    @Override
    protected String doInBackground(Void... params) {
        if (mainHTML == null) {
            try {
                mainHTML = Jsoup.connect("http://bloodbrothersgame.wikia.com/wiki/Blood_Brothers_Wiki")
                        .ignoreContentType(true).execute().body();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mainHTML;
    }

    @Override
    protected void onPostExecute(String param) {
        try {
            Document dom = Jsoup.parse(param);
            Elements newFamRows = dom.getElementsByClass("lcs-container").first()
                    .getElementsByTag("table").first() // first table is the new fam box
                    .getElementsByTag("tbody").first()
                    .getElementsByTag("tr");

            // calculate the width of the images to be displayed later on
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int screenWidth = size.x;
            int scaleWidth = screenWidth / 10; // set it to be 1/10 of the screen width

            int count = 0;
            for (Element row : newFamRows) {
                if (count == 0) { // first row is just the header
                    count++;
                    continue;
                }
                Elements cells = row.getElementsByTag("td");
                int countCell = 0;
                for (Element cell : cells) {
                    if (countCell % 2 == 0) { // the date
                        TextView tmpTv = new TextView(activity);
                        tmpTv.setText(cell.text());
                        tmpTv.setLayoutParams(new LinearLayout.LayoutParams (ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                        tmpTv.setBackgroundColor(Color.parseColor("#6b0c00"));
                        tmpTv.setGravity(0x11); //center
                        tmpTv.setTextColor(Color.WHITE);
                        layout.addView(tmpTv);
                    }
                    else { // the images
                        Elements links = cell.getElementsByTag("a");
                        LinearLayout tmpLayout = new LinearLayout(activity);
                        tmpLayout.setLayoutParams(new LinearLayout.LayoutParams (ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                        layout.addView(tmpLayout);
                        for (Element link : links) {
                            // add the fam to our fam list if it's not already there. This is usually needed for new fams
                            // that are recently added to the wiki within 1-2 days which the API doesn't return in the list
                            String name = link.attr("title"); // the fam name
                            String famUrl = link.attr("href"); // the link to the fam page, without the domain part
                            if (!FamStore.famList.contains(name)) {
                                FamStore.famList.add(name);
                            }
                            if (FamStore.famLinkTable.get(name) == null) {
                                // TODO: see if we can find the id for new fams
                                String[] tmpStr = {famUrl, null};
                                FamStore.famLinkTable.put(name, tmpStr);
                            }

                            // get the thubnail image src
                            String imgSrc = link.getElementsByTag("img").first().attr("data-src");
                            if (imgSrc == null || imgSrc.equals("")) imgSrc = link.getElementsByTag("img").first().attr("src");

                            // create a new image view and add it
                            ImageView imgView = new ImageView(activity);
                            imgView.setTag(name); // set the tag of this ImageView to be the fam name
                            tmpLayout.addView(imgView);

                            // set the image view's dimensions
                            imgView.getLayoutParams().width = scaleWidth;
                            imgView.getLayoutParams().height = (int) (scaleWidth*1.5);
                            imgView.requestLayout();
                            imgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            ImageLoader.getInstance().displayImage(imgSrc, imgView);

                            // set listener for the image view
                            imgView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(activity, FamDetailActivity.class);
                                    intent.putExtra(MainActivity.FAM_NAME, (String)v.getTag());
                                    activity.startActivity(intent);
                                }
                            });
                        }
                    }
                    countCell++;
                }
                count++;
            }

            // remove the spinner
            ProgressBar pgrBar = (ProgressBar) activity.findViewById(R.id.progressBar_fragment_general);
            layout.removeView(pgrBar);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
