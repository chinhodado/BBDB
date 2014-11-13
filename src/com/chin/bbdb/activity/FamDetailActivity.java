package com.chin.bbdb.activity;

import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chin.bbdb.FamStore;
import com.chin.bbdb.R;
import com.chin.bbdb.asyncTask.AddFamiliarInfoTask;
import com.chin.common.TabListener;
import com.chin.common.Util;

import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v4.app.Fragment;

/**
 * Activity to show all details about a familiar
 */
public class FamDetailActivity extends BaseFragmentActivity {

    // map a string (the evolution level) to a number that represents
    // the image for that evolution level
    public static HashMap<String, Integer> evolutionMap = null;

    public String famName = null;

    // the last fam ever displayed in this activity (before going to comparison)
    public static String lastFam = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            famName = savedInstanceState.getString("FAMNAME");
            lastFam = famName;
        }
        else {
            Intent intent = getIntent(); // careful, this intent may not be the intent from MainActivity...
            String tmpName = intent.getStringExtra(MainActivity.FAM_NAME);
            if (tmpName != null) {
                famName = tmpName; // needed since we may come back from other activity, not just the main one
                lastFam = famName;
            }
        }

        // if at this point the famName is still null, we can assume that we're returning from FamCompareActivity
        if (famName == null) {
            famName = lastFam;
        }

        setTitle("");
        initialize();

        // add the AutoCompleteTextView to the ActionBar
        LayoutInflater inflator = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.actionbar_layout, null);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(v);

        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.compareAutoCompleteTextView);
        autoCompleteTextView.setAdapter(MainActivity.adapter);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String famName = (String)parent.getItemAtPosition(position);
                Intent intent = new Intent(view.getContext(), FamCompareActivity.class);
                intent.putExtra("FAM_NAME_RIGHT", famName);
                intent.putExtra("FAM_NAME_LEFT", FamDetailActivity.this.famName);
                startActivity(intent);
            }
        });

        // add the tabs
        ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        Bundle bundle = new Bundle();
        bundle.putString("FAMNAME", famName);
        bar.addTab(bar.newTab().setText("Information")
                .setTabListener(new TabListener<FamInfoFragment>(this, "fam info", FamInfoFragment.class, bundle, R.id.tab_viewgroup)));
        bar.addTab(bar.newTab().setText("Comment")
                .setTabListener(new TabListener<FamCommentFragment>(this, "fam comment", FamCommentFragment.class, bundle, R.id.tab_viewgroup)));

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
        bundle.putString("FAMNAME", famName);

        // Save the index of the currently selected tab
        bundle.putInt("TAB_INDEX", getActionBar().getSelectedTab().getPosition());
    }

    /**
     * Some one-time initialization will be done here
     */
    public void initialize() {
        if (evolutionMap == null) {
            evolutionMap = new HashMap<String, Integer>();
            evolutionMap.put("Common", R.drawable.common);
            evolutionMap.put("Uncommon", R.drawable.uncommon);
            evolutionMap.put("Rare", R.drawable.rare);
            evolutionMap.put("Epic", R.drawable.epic);
            evolutionMap.put("Legendary", R.drawable.legend);
            evolutionMap.put("Mythic", R.drawable.mythic);

            evolutionMap.put("1of1", R.drawable.star11);
            evolutionMap.put("1of2", R.drawable.star12);
            evolutionMap.put("2of2", R.drawable.star22);
            evolutionMap.put("1of3", R.drawable.star13);
            evolutionMap.put("2of3", R.drawable.star23);
            evolutionMap.put("3of3", R.drawable.star33);
            evolutionMap.put("1of4", R.drawable.star14);
            evolutionMap.put("2of4", R.drawable.star24);
            evolutionMap.put("3of4", R.drawable.star34);
            evolutionMap.put("4of4", R.drawable.star44);
        }
    }

    /**
     * Fragment for the familiar info view
     */
    public static class FamInfoFragment extends Fragment {

        AsyncTask<?, ?, ?> myTask = null;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Bundle bundle = getArguments();
            String famName = bundle.getString("FAMNAME");
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_fam_info, container, false);
            myTask = new AddFamiliarInfoTask((FamDetailActivity) getActivity()).execute(famName);
            return view;
        }

        @Override
        public void onPause() {
            super.onPause();
            if (myTask != null) {
                myTask.cancel(true);
                myTask = null;
            }
        }
    }

    /**
     * Fragment for the familiar comment view
     */
    public static class FamCommentFragment extends Fragment {
        PopulateCommentAsyncTask myTask;
        @SuppressLint("RtlHardcoded")
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Bundle bundle = getArguments();
            String famName = bundle.getString("FAMNAME");
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_general_linear, container, false);
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.fragment_layout);
            layout.setGravity(Gravity.RIGHT);

            String commentUrl = "http://bloodbrothersgame.wikia.com/wikia.php?controller=ArticleComments&method=Content&articleId="
                    + FamStore.famLinkTable.get(famName)[1];
            myTask = (PopulateCommentAsyncTask) new PopulateCommentAsyncTask(layout, (FamDetailActivity) getActivity(), 1)
                            .execute(commentUrl);
            return view;
        }

        @Override
        public void onPause() {
            super.onPause();
            if (myTask != null) {
                myTask.cancel(true);
                myTask = null;
            }
        }
    }

    public static class PopulateCommentAsyncTask extends AsyncTask<String, Void, Void> {

        LinearLayout layout;
        FamDetailActivity activity;
        Document dom;
        int page;
        String baseUrl;
        boolean exceptionOccurred = false;

        public PopulateCommentAsyncTask(LinearLayout layout, FamDetailActivity activity, int page) {
            this.layout = layout;
            this.activity = activity;
            this.page = page;
        }

        @Override
        protected Void doInBackground(String... params) {
            String html;
            try {
                FamStore.getInstance(activity);
                baseUrl = params[0];
                html = Jsoup.connect(baseUrl).ignoreContentType(true).execute().body();

                if (isCancelled()) {
                    return null;
                }

                dom = Jsoup.parse(html);
            } catch (Exception e) {
                e.printStackTrace();

                // set the flag so we can do something about this in onPostExecute()
                exceptionOccurred = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {

            if (exceptionOccurred) {
                // remove the spinner
                ProgressBar pgrBar = (ProgressBar) activity.findViewById(R.id.progressBar_fragment_general);
                layout.removeView(pgrBar);
                return;
            }

            try {
                // calculate the width of the textviews for subcomments to be displayed later on
                Display display = activity.getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int screenWidth = size.x;
                int scaleWidth = (int) (screenWidth * 0.75); // set it to be 3/4 of the screen width

                Elements comments = dom.getElementById("article-comments-ul")
                                       .children();
                boolean isFirstComment = true; // used for not showing the line separator above the first comment

                for (Element comment : comments) {
                    if (comment.tagName().equals("li")) { // the main comments
                        if (!isFirstComment || page != 1) { // so skip if isFirstComment && page == 1
                            Util.addLineSeparator(activity, layout);
                        }
                        String commentText = comment.getElementsByClass("speech-bubble-message").first()
                                                    .getElementsByClass("article-comm-text").first().text();
                        TextView tv = new TextView(activity);
                        layout.addView(tv);
                        tv.setText("\n" + commentText + "\n");
                        isFirstComment = false;
                    }
                    else if (comment.tagName().equals("ul")) { // the sub comments
                        Elements subcomments = comment.children();
                        for (Element subcomment : subcomments) {
                            String commentText = subcomment.getElementsByClass("speech-bubble-message").first()
                                                           .getElementsByClass("article-comm-text").first().text();
                            TextView tv = new TextView(activity);
                            tv.setLayoutParams(new LayoutParams(scaleWidth, LayoutParams.WRAP_CONTENT));
                            layout.addView(tv);
                            tv.setText(commentText + "\n");
                        }
                    }
                }

                // remove the spinner
                ProgressBar pgrBar = (ProgressBar) activity.findViewById(R.id.progressBar_fragment_general);
                layout.removeView(pgrBar);

                // if there's more comment, load them recursively
                Element next = dom.getElementById("article-comments-pagination-link-next");
                if (next != null) {
                    String nextLink = baseUrl + "&page=" + (page + 1);
                    new PopulateCommentAsyncTask(layout, activity, page + 1).execute(nextLink);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
