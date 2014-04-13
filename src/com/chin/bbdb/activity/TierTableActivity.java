package com.chin.bbdb.activity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chin.bbdb.FamStore;
import com.chin.bbdb.LayoutUtil;
import com.chin.bbdb.R;
import com.chin.bbdb.TabListener;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
        bar.addTab(bar.newTab().setText("Raid Tier")
                .setTabListener(new TabListener<RaidFragment>(this, "raid", RaidFragment.class)));
        bar.addTab(bar.newTab().setText("Tower Tier")
                .setTabListener(new TabListener<TowerFragment>(this, "tower", TowerFragment.class)));

        // Look up the AdView as a resource and load a request.
        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_help) {
            Intent intent = new Intent(activity, HelpActivity.class);
            startActivity(intent);
            return true;
        }
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
            View view = inflater.inflate(R.layout.fragment_tier_list, container, false);
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.tier_layout);
            new PopulateTierTableAsyncTask(TierTableActivity.activity, layout).execute("PVP");
            return view;
        }
    }

    public static class RaidFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_tier_list, container, false);
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.tier_layout);
            new PopulateTierTableAsyncTask(TierTableActivity.activity, layout).execute("RAID");
            return view;
        }
    }

    public static class TowerFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_tier_list, container, false);
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.tier_layout);
            new PopulateTierTableAsyncTask(TierTableActivity.activity, layout).execute("TOWER");
            return view;
        }
    }

    public static class PopulateTierTableAsyncTask extends AsyncTask<String, Void, String> {

        String mainHTML;
        Activity activity;
        LinearLayout layout;

        public PopulateTierTableAsyncTask(Activity activity, LinearLayout layout) {
            this.activity = activity;
            this.layout = layout;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                mainHTML = FamStore.getInstance().getTierHTML(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mainHTML;
        }

        @Override
        protected void onPostExecute(String param) {
            Document pageDOM   = Jsoup.parse(mainHTML);

            Elements tierTables   = pageDOM.getElementsByClass("wikitable");

            for (int i = 0; i < 9; i++){ // 9 tables
                TableLayout table = new TableLayout(activity);
                Elements rows = tierTables.get(i).getElementsByTag("tbody").first().getElementsByTag("tr"); // get all rows in each table
                int countRow = 0;
                for (Element row : rows) {
                    countRow++;
                    if (countRow == 1) { // row 1 is the table title, row 2 is the column headers. This is different in the DOM in browser
                        LayoutUtil.addRowWithOneTextView(activity, table, row.text(), true);
                    }
                    else if (countRow == 2) {
                        continue; // column headers. Since we only show the image and fam name, there's no need for header
                    }
                    else {
                        Elements cells = row.getElementsByTag("td");
                        TableRow tr = new TableRow(activity);
                        ImageView imgView = new ImageView(activity); tr.addView(imgView);

                        // get the thubnail image src
                        Element link = cells.get(0).getElementsByTag("a").first();
                        String imgSrc = link.getElementsByTag("img").first().attr("data-src");
                        if (imgSrc == null || imgSrc.equals("")) imgSrc = link.getElementsByTag("img").first().attr("src");

                        ImageLoader.getInstance().displayImage(imgSrc, imgView);

                        String famName = cells.get(1).text();
                        TextView tv = new TextView(activity);
                        tv.setText(famName);
                        tr.addView(tv);
                        table.addView(tr);

                        tr.setTag(famName);
                        tr.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(activity, FamDetailActivity.class);
                                intent.putExtra(MainActivity.FAM_NAME, (String) v.getTag());
                                activity.startActivity(intent);
                            }
                        });
                    }
                }
                layout.addView(table);
            }
        }
    }
}
