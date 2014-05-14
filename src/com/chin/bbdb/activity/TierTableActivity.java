package com.chin.bbdb.activity;

import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chin.bbdb.FamStore;
import com.chin.bbdb.FamStore.TierCategory;
import com.chin.bbdb.R;
import com.chin.bbdb.TabListener;
import com.chin.bbdb.Util;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.ActionBar;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TabWidget;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * An activity that displays the PVP, Raid and Tower tier tables
 */
public class TierTableActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // add the tabs
        ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        Bundle bundlePVP = new Bundle();
        bundlePVP.putSerializable("category", TierCategory.PVP);
        bar.addTab(bar.newTab().setText("PVP Tier")
                .setTabListener(new TabListener<TierFragment>(this, "pvp", TierFragment.class, bundlePVP)));

        Bundle bundleRAID = new Bundle();
        bundleRAID.putSerializable("category", TierCategory.RAID);
        bar.addTab(bar.newTab().setText("Raid Tier")
                .setTabListener(new TabListener<TierFragment>(this, "raid", TierFragment.class, bundleRAID)));

        Bundle bundleTOWER = new Bundle();
        bundleTOWER.putSerializable("category", TierCategory.TOWER);
        bar.addTab(bar.newTab().setText("Tower Tier")
                .setTabListener(new TabListener<TierFragment>(this, "tower", TierFragment.class, bundleTOWER)));

        // if we're resuming the activity, re-select the tab that was selected before
        if (savedInstanceState != null) {
            // Select the tab that was selected before orientation change
            int index = savedInstanceState.getInt("TAB_INDEX");
            bar.setSelectedNavigationItem(index);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        // Save the index of the currently selected tab
        bundle.putInt("TAB_INDEX", getActionBar().getSelectedTab().getPosition());
    }

    /**
     * A fragment for a tier category (PVP, raid or tower)
     * There will be many TierTableFragment nested inside it
     */
    public static class TierFragment extends Fragment {
        private FragmentTabHost mTabHost;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Bundle args = getArguments();
            TierCategory category = (TierCategory) args.getSerializable("category");

            mTabHost = new FragmentTabHost(getActivity());
            mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.tab_viewgroup);

            Bundle bundle = new Bundle();
            bundle.putSerializable("category", category);
            bundle.putString("tier", "X");
            mTabHost.addTab(mTabHost.newTabSpec("X").setIndicator("Tier X"),
                    TierTableFragment.class, bundle);

            bundle = new Bundle();
            bundle.putSerializable("category", category);
            bundle.putString("tier", "S+");
            mTabHost.addTab(mTabHost.newTabSpec("S+").setIndicator("Tier S+"),
                    TierTableFragment.class, bundle);

            bundle = new Bundle();
            bundle.putSerializable("category", category);
            bundle.putString("tier", "S");
            mTabHost.addTab(mTabHost.newTabSpec("S").setIndicator("Tier S"),
                    TierTableFragment.class, bundle);

            bundle = new Bundle();
            bundle.putSerializable("category", category);
            bundle.putString("tier", "A+");
            mTabHost.addTab(mTabHost.newTabSpec("A+").setIndicator("Tier A+"),
                    TierTableFragment.class, bundle);

            bundle = new Bundle();
            bundle.putSerializable("category", category);
            bundle.putString("tier", "A");
            mTabHost.addTab(mTabHost.newTabSpec("A").setIndicator("Tier A"),
                    TierTableFragment.class, bundle);

            bundle = new Bundle();
            bundle.putSerializable("category", category);
            bundle.putString("tier", "B");
            mTabHost.addTab(mTabHost.newTabSpec("B").setIndicator("Tier B"),
                    TierTableFragment.class, bundle);

            bundle = new Bundle();
            bundle.putSerializable("category", category);
            bundle.putString("tier", "C");
            mTabHost.addTab(mTabHost.newTabSpec("C").setIndicator("Tier C"),
                    TierTableFragment.class, bundle);

            // make the tabs scrollable
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

    /**
     * A fragment for a specific category-tier pair that displays all fams in that tier and category
     */
    public static class TierTableFragment extends Fragment {

        @SuppressWarnings("rawtypes")
        AsyncTask myTask;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Bundle args = getArguments();
            TierCategory category = (TierCategory) args.getSerializable("category");
            String tier = args.getString("tier");
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_tier_list, container, false);
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.tier_layout);

            try {//TODO: see if we can be more error-tolerance
                myTask = new PopulateTierTableAsyncTask(getActivity(), layout, category).execute(tier);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return view;
        }

        @Override
        public void onPause() {
            super.onPause();
            if (myTask != null) {
                myTask.cancel(true);
            }
            myTask = null;
        }
    }

    /**
     * An AsyncTask that populates a TierTableFragment
     * @author Chin
     *
     */
    public static class PopulateTierTableAsyncTask extends AsyncTask<String, Void, Void> {

        Activity activity;
        LinearLayout layout;
        String tier;
        Document pageDOM;
        TierCategory category;

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

        public PopulateTierTableAsyncTask(Activity activity, LinearLayout layout, TierCategory category) {
            this.activity = activity;
            this.layout = layout;
            this.category = category;
        }

        @Override
        protected Void doInBackground(String... params) {
            String mainHTML = null;
            try {
                mainHTML = FamStore.getInstance().getTierHTML(category);

                if (isCancelled()) {
                    return null; // try to return early if possible
                }

                tier = params[0];
                pageDOM  = Jsoup.parse(mainHTML);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {

            if (pageDOM == null) {
                return; // instead of try-catch
            }

            Elements tierTables   = pageDOM.getElementsByClass("wikitable");

            // calculate the width of the images to be displayed later on
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int screenWidth = size.x;
            int scaleWidth = screenWidth / 10; // set it to be 1/10 of the screen width

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

                    // get the scaled image link and display it
                    String newScaledLink = Util.getScaledWikiaImageLink(imgSrc, scaleWidth);
                    ImageLoader.getInstance().displayImage(newScaledLink, imgView);

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
            //TODO: center the spinner horizontally
            //remove the spinner
            ProgressBar progressBar = (ProgressBar) activity.findViewById(R.id.progressBar_tierTable);
            layout.removeView(progressBar);
        }
    }
}
