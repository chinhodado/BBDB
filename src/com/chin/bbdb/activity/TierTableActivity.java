package com.chin.bbdb.activity;

import java.io.InputStream;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chin.bbdb.LayoutUtil;
import com.chin.bbdb.R;
import com.chin.bbdb.TabListener;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Log;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TierTableActivity extends Activity {

    public static Activity activity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tier_table);

        TierTableActivity.activity = this;

        // add the tabs
        ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        bar.addTab(bar.newTab().setText("PVP Tier")
                .setTabListener(new TabListener<PVPFragment>(this, "pvp", PVPFragment.class)));

        // Look up the AdView as a resource and load a request.
        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.tier_table, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Google Analytics
        EasyTracker.getInstance(this).activityStart(this);
      }

    @Override
    public void onStop() {
        super.onStop();
        // Google Analytics
        EasyTracker.getInstance(this).activityStop(this);
    }

    public static class PVPFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_tier_pvp, container, false);
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.tier_layout);
            new PopulateTierTableAsyncTask(TierTableActivity.activity, layout).execute();
            return view;
        }
    }
    public static class PopulateTierTableAsyncTask extends AsyncTask<Void, Void, String> {

        static String mainHTML = null;

        Activity activity;
        LinearLayout layout;

        public PopulateTierTableAsyncTask(Activity activity, LinearLayout layout) {
            this.activity = activity;
            this.layout = layout;
        }

        @Override
        protected String doInBackground(Void... params) {
            if (mainHTML == null) {
                try {
                    mainHTML = Jsoup.connect("http://bloodbrothersgame.wikia.com/wiki/Familiar_Tier_List/PvP")
                            .ignoreContentType(true).execute().body();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return mainHTML;
        }

        @Override
        protected void onPostExecute(String param) {
            Document pvpDOM   = Jsoup.parse(mainHTML);

            Elements pvpTables   = pvpDOM.getElementsByClass("wikitable");

            for (int i = 0; i < 9; i++){ // 9 tables
                TableLayout table = new TableLayout(activity);
                Elements pvpRows = pvpTables.get(i).getElementsByTag("tbody").first().getElementsByTag("tr"); // get all rows in each table
                int countRow = 0;
                for (Element pvpRow : pvpRows) {
                    countRow++;
                    if (countRow == 1) { // row 1 is the table title, row 2 is the column headers. This is different in the DOM in browser
                        LayoutUtil.addRowWithOneTextView(activity, table, pvpRow.text(), true);
                    }
                    else if (countRow == 2) {
                        String[] labels = {"Image", "Familiar"};
                        LayoutUtil.addRowWithNTextView(activity, table, 2, labels, true);
                    }
                    else {
                        Elements cells = pvpRow.getElementsByTag("td");
                        TableRow tr = new TableRow(activity);
                        ImageView imgView = new ImageView(activity); tr.addView(imgView);

                        // get the thubnail image src
                        Element link = cells.get(0).getElementsByTag("a").first();
                        String imgSrc = link.getElementsByTag("img").first().attr("data-src");
                        if (imgSrc == null || imgSrc.equals("")) imgSrc = link.getElementsByTag("img").first().attr("src");
                        new DownloadImageTask(imgView, activity).execute(imgSrc);

                        TextView tv = new TextView(activity);
                        tv.setText(cells.get(1).text()); //the fam name
                        tr.addView(tv);
                        table.addView(tr);
                    }
                }
                layout.addView(table);
            }
        }
    }

    public static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        // store cached thumnail images of fams. There's no limit to the amount of images
        // that can be stored here since the thumnails are quite small, and the maximum number
        // of them are also limited (shown on the front page)
        static HashMap<String, Bitmap> thumbnails = new HashMap<String, Bitmap>();

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
            // set the image
            imgView.setImageBitmap(result);
        }
    }
}
