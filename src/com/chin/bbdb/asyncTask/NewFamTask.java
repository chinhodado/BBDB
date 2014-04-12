package com.chin.bbdb.asyncTask;

import java.io.InputStream;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chin.bbdb.FamStore;
import com.chin.bbdb.activity.FamDetailActivity;
import com.chin.bbdb.activity.MainActivity;
import com.google.analytics.tracking.android.Log;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NewFamTask extends AsyncTask<Void, Void, String> {

    // store cached thumnail images of new fams. There's no limit to the amount of images
    // that can be stored here since the thumnails are quite small, and the maximum number
    // of them are also limited (shown on the front page)
    static HashMap<String, Bitmap> thumbnails = new HashMap<String, Bitmap>();

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
                                FamStore.famLinkTable.put(name, famUrl);
                            }

                            // get the thubnail image src
                            String imgSrc = link.getElementsByTag("img").first().attr("data-src");
                            if (imgSrc == null || imgSrc.equals("")) imgSrc = link.getElementsByTag("img").first().attr("src");

                            // create a new image view and add it
                            ImageView imgView = new ImageView(activity);
                            imgView.setTag(name); // set the tag of this ImageView to be the fam name
                            tmpLayout.addView(imgView);
                            new DownloadImageTask(imgView, activity).execute(imgSrc);

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
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        Activity activity;
        ImageView imgView;
        public DownloadImageTask(ImageView imgView, Activity activity) {
            this.activity = activity;
            this.imgView = imgView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap img = thumbnails.get(urldisplay);
            if (img == null) { // only fetch the image if it's not already cached
                try {
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    img = BitmapFactory.decodeStream(in);
                    thumbnails.put(urldisplay, img); // cache the image
                } catch (Exception e) {
                    Log.i(urldisplay);
                    e.printStackTrace();
                }
            }
            return img;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            //scale the image
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int screenWidth = size.x;

            int scaleWidth = screenWidth / 10;
            double scaleHeight = (scaleWidth / ((double) result.getWidth() / result.getHeight()));

            // set the image
            imgView.setImageBitmap(Bitmap.createScaledBitmap(result, scaleWidth, (int) scaleHeight, false));
        }
    }
}
