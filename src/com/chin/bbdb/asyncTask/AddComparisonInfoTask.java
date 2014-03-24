package com.chin.bbdb.asyncTask;

import java.text.DecimalFormat;

import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;

import com.chin.bbdb.FamStore;
import com.chin.bbdb.R;
import com.chin.bbdb.FamStore.FamStats;
import com.chin.bbdb.activity.FamCompareActivity;

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
    	famStore.getStats(this.famNameLeft);
    	famStore.getImage(this.famNameLeft);
    	famStore.getStats(this.famNameRight);
    	famStore.getImage(this.famNameRight);
		return null;
    }
    
    protected void onPostExecute(Void params) {
		// all of these should be fast
		addFamImage();
		addFamStat();
    }
	
	public void addFamImage() {
    	// remove the spinner
//    	ProgressBar pgrBar = (ProgressBar) activity.findViewById(R.id.progressBar1);
//    	LinearLayout layout = (LinearLayout) activity.findViewById(R.id.linearLayout1);
//    	layout.removeView(pgrBar);
    	
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
	}
}
