package com.chin.bbdb.asyncTask;

import com.chin.bbdb.FamStore;
import com.chin.bbdb.LayoutUtil;
import com.chin.bbdb.R;
import com.chin.bbdb.FamStore.TierCategory;
import com.chin.bbdb.activity.FamDetailActivity;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;

/**
 * The async task that populate the tier information in FamDetailActivity
 * 
 */
class AddTierInfoTask extends AsyncTask<Void, Void, Void> {
    TableLayout detailTable;
    FamDetailActivity activity;
    String famName;
    String pvpTierHTML = null, raidTierHTML = null, towerTierHTML = null;

    public AddTierInfoTask(TableLayout detailTable, FamDetailActivity activity, String famName) {
        this.detailTable = detailTable;
        this.activity = activity;
        this.famName = famName;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (FamStore.pvpTierMap == null || FamStore.raidTierMap == null || FamStore.towerTierMap == null) {
            try {
                FamStore.getInstance().initializeTierMap(TierCategory.PVP);
                FamStore.getInstance().initializeTierMap(TierCategory.RAID);
                FamStore.getInstance().initializeTierMap(TierCategory.TOWER);
            } catch (Exception e) {
                Log.e("FamDetail", "Error fetching the tier HTML pages");
                cancel(true);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void param) {

        //just in case...
        if (FamStore.pvpTierMap == null || FamStore.raidTierMap == null || FamStore.towerTierMap == null)
            return;

        String famPVPTier = null, famRaidTier = null, famTowerTier = null;
        famPVPTier   = FamStore.pvpTierMap.get(famName);
        famRaidTier  = FamStore.raidTierMap.get(famName);
        famTowerTier = FamStore.towerTierMap.get(famName);

        // remove the spinner
        ProgressBar pgrBar = (ProgressBar) activity.findViewById(R.id.progressBar3);
        LinearLayout layout = (LinearLayout) activity.findViewById(R.id.linearLayout1);
        layout.removeView(pgrBar);

        LayoutUtil.addRowWithTwoTextView(activity, detailTable, "PVP tier", famPVPTier==null? "N/A" : famPVPTier, true);
        LayoutUtil.addRowWithTwoTextView(activity, detailTable, "Raid tier", famRaidTier==null? "N/A" : famRaidTier, true);
        LayoutUtil.addRowWithTwoTextView(activity, detailTable, "Tower tier", famTowerTier==null? "N/A" : famTowerTier, true);
    }
}