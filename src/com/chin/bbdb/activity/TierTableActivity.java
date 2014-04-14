package com.chin.bbdb.activity;

import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chin.bbdb.FamStore;
import com.chin.bbdb.R;
import com.chin.bbdb.TabListener;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Log;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.ActionBar;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabWidget;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TierTableActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tier_table);

        // add the tabs
        ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        Bundle bundlePVP = new Bundle();
        bundlePVP.putString("category", "PVP");
        bar.addTab(bar.newTab().setText("PVP Tier")
                .setTabListener(new TabListener<TierFragment>(this, "pvp", TierFragment.class, bundlePVP)));

        Bundle bundleRAID = new Bundle();
        bundleRAID.putString("category", "RAID");
        bar.addTab(bar.newTab().setText("Raid Tier")
                .setTabListener(new TabListener<TierFragment>(this, "raid", TierFragment.class, bundleRAID)));

        Bundle bundleTOWER = new Bundle();
        bundleTOWER.putString("category", "TOWER");
        bar.addTab(bar.newTab().setText("Tower Tier")
                .setTabListener(new TabListener<TierFragment>(this, "tower", TierFragment.class, bundleTOWER)));

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
            Intent intent = new Intent(this, HelpActivity.class);
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

    public static class TierFragment extends Fragment {
        private FragmentTabHost mTabHost;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Bundle args = getArguments();
            String category = args.getString("category");

            mTabHost = new FragmentTabHost(getActivity());
            mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.tab_viewgroup);

            Bundle bundle = new Bundle();
            bundle.putString("category", category);
            bundle.putString("tier", "X");
            mTabHost.addTab(mTabHost.newTabSpec("X").setIndicator("Tier X"),
                    TierTableFragment.class, bundle);

            bundle = new Bundle();
            bundle.putString("category", category);
            bundle.putString("tier", "S+");
            mTabHost.addTab(mTabHost.newTabSpec("S+").setIndicator("Tier S+"),
                    TierTableFragment.class, bundle);

            bundle = new Bundle();
            bundle.putString("category", category);
            bundle.putString("tier", "S");
            mTabHost.addTab(mTabHost.newTabSpec("S").setIndicator("Tier S"),
                    TierTableFragment.class, bundle);

            bundle = new Bundle();
            bundle.putString("category", category);
            bundle.putString("tier", "A+");
            mTabHost.addTab(mTabHost.newTabSpec("A+").setIndicator("Tier A+"),
                    TierTableFragment.class, bundle);

            bundle = new Bundle();
            bundle.putString("category", category);
            bundle.putString("tier", "A");
            mTabHost.addTab(mTabHost.newTabSpec("A").setIndicator("Tier A"),
                    TierTableFragment.class, bundle);

            bundle = new Bundle();
            bundle.putString("category", category);
            bundle.putString("tier", "B");
            mTabHost.addTab(mTabHost.newTabSpec("B").setIndicator("Tier B"),
                    TierTableFragment.class, bundle);

            bundle = new Bundle();
            bundle.putString("category", category);
            bundle.putString("tier", "C");
            mTabHost.addTab(mTabHost.newTabSpec("C").setIndicator("Tier C"),
                    TierTableFragment.class, bundle);

            bundle = new Bundle();
            bundle.putString("category", category);
            bundle.putString("tier", "D");
            mTabHost.addTab(mTabHost.newTabSpec("D").setIndicator("Tier D"),
                    TierTableFragment.class, bundle);

            bundle = new Bundle();
            bundle.putString("category", category);
            bundle.putString("tier", "E");
            mTabHost.addTab(mTabHost.newTabSpec("E").setIndicator("Tier E"),
                    TierTableFragment.class, bundle);

            TabWidget tw = (TabWidget) mTabHost.findViewById(android.R.id.tabs);
            LinearLayout ll = (LinearLayout) tw.getParent();
            HorizontalScrollView hs = new HorizontalScrollView(getActivity());
            hs.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));
            ll.addView(hs, 0);
            ll.removeView(tw);
            hs.addView(tw);
            hs.setHorizontalScrollBarEnabled(false);

            return mTabHost;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mTabHost = null;
        }
    }

    public static class TierTableFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Bundle args = getArguments();
            String category = args.getString("category");
            String tier = args.getString("tier");
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_tier_list, container, false);
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.tier_layout);

            try {//TODO: see if we can be more error-tolerance
                new PopulateTierTableAsyncTask(getActivity(), layout).execute(category, tier);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return view;
        }
    }

    public static class PopulateTierTableAsyncTask extends AsyncTask<String, Void, String> {

        String mainHTML;
        Activity activity;
        LinearLayout layout;
        String tier;

        // map a tier string to an int (the table number)
        private static final HashMap<String, Integer> tierMap;
        static
        {
            tierMap = new HashMap<String, Integer>();
            tierMap.put("X", 0);
            tierMap.put("S+", 1);
            tierMap.put("S", 2);
            tierMap.put("A+", 3);
            tierMap.put("A", 4);
            tierMap.put("B", 5);
            tierMap.put("C", 6);
            tierMap.put("D", 7);
            tierMap.put("E", 8);
        }

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
            tier = params[1];
            Log.i("Creating fragment: " + params[0] + " " + params[1]);
            return mainHTML;
        }

        @Override
        protected void onPostExecute(String param) {
            Document pageDOM   = Jsoup.parse(mainHTML);

            Elements tierTables   = pageDOM.getElementsByClass("wikitable");

            // calculate the width of the images to be displayed later on
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int screenWidth = size.x;
            int scaleWidth = screenWidth / 12; // set it to be 1/12 of the screen width

            int tableIndex = tierMap.get(tier);
            Element tierTable = tierTables.get(tableIndex);

            TableLayout table = new TableLayout(activity);
            Elements rows = tierTable.getElementsByTag("tbody").first().getElementsByTag("tr"); // get all rows in each table
            int countRow = 0;
            for (Element row : rows) {
                countRow++;
                if (countRow == 1 || countRow == 2) {
                    // row 1 is the table title, row 2 is the column headers. This is different in the DOM in browser
                    continue;
                }
                else {
                    Elements cells = row.getElementsByTag("td");
                    TableRow tr = new TableRow(activity);
                    ImageView imgView = new ImageView(activity); tr.addView(imgView);

                    // get the thubnail image src
                    Element link = cells.get(0).getElementsByTag("a").first();
                    String imgSrc = link.getElementsByTag("img").first().attr("data-src");
                    if (imgSrc == null || imgSrc.equals("")) imgSrc = link.getElementsByTag("img").first().attr("src");

                    imgView.setLayoutParams(new TableRow.LayoutParams(scaleWidth, (int) (scaleWidth*1.5))); // the height's not exact
                    imgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    ImageLoader.getInstance().displayImage(imgSrc, imgView);

                    String famName = cells.get(1).text();
                    TextView tv = new TextView(activity);
                    tv.setText(famName);
                    tr.addView(tv);
                    tr.setGravity(0x10); //center vertical
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
