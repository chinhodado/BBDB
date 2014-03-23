package com.chin.bbdb.asyncTask;

import java.text.DecimalFormat;

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
	
		TextView textView_HP_left    = (TextView) activity.findViewById(R.id.textView_HP_left);
		TextView textView_ATK_left   = (TextView) activity.findViewById(R.id.textView_ATK_left);
		TextView textView_DEF_left   = (TextView) activity.findViewById(R.id.textView_DEF_left);
		TextView textView_WIS_left   = (TextView) activity.findViewById(R.id.textView_WIS_left);
		TextView textView_AGI_left   = (TextView) activity.findViewById(R.id.textView_AGI_left);
		TextView textView_total_left   = (TextView) activity.findViewById(R.id.textView_total_left);
		
		TextView textView_HP_right    = (TextView) activity.findViewById(R.id.textView_HP_right);
		TextView textView_ATK_right   = (TextView) activity.findViewById(R.id.textView_ATK_right);
		TextView textView_DEF_right   = (TextView) activity.findViewById(R.id.textView_DEF_right);
		TextView textView_WIS_right   = (TextView) activity.findViewById(R.id.textView_WIS_right);
		TextView textView_AGI_right   = (TextView) activity.findViewById(R.id.textView_AGI_right);
		TextView textView_total_right   = (TextView) activity.findViewById(R.id.textView_total_right);
	
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
		
		String[] leftVSRightPercent = new String[6];
		String[] rightVSLeftPercent = new String[6];
		
		for (int i = 0; i < 6; i++) {
			double leftVSRight = ((double) highestCommonLeft[i] - highestCommonRight[i]) / highestCommonRight[i] * 100;
			// rounding to 2 decimal places
			leftVSRight = (double)Math.round(leftVSRight * 100) / 100;
			
			// get the strings
			leftVSRightPercent[i] = ((leftVSRight > 0)? "+" : "") + leftVSRight;
			double rightVSLeft = -1 * leftVSRight;
			rightVSLeftPercent[i] = ((rightVSLeft > 0)? "+" : "") + rightVSLeft;
		}
		
		textView_HP_left.append(formatter.format(highestCommonLeft[0]) + " (" + leftVSRightPercent[0] + "%)");
		textView_ATK_left.append(formatter.format(highestCommonLeft[1]) + " (" + leftVSRightPercent[1] + "%)");
		textView_DEF_left.append(formatter.format(highestCommonLeft[2]) + " (" + leftVSRightPercent[2] + "%)");
		textView_WIS_left.append(formatter.format(highestCommonLeft[3]) + " (" + leftVSRightPercent[3] + "%)");
		textView_AGI_left.append(formatter.format(highestCommonLeft[4]) + " (" + leftVSRightPercent[4] + "%)");
		textView_total_left.append(formatter.format(highestCommonLeft[5]) + " (" + leftVSRightPercent[5] + "%)");
		
		textView_HP_right.append(formatter.format(highestCommonRight[0]) + " (" + rightVSLeftPercent[0] + "%)");
		textView_ATK_right.append(formatter.format(highestCommonRight[1]) + " (" + rightVSLeftPercent[1] + "%)");
		textView_DEF_right.append(formatter.format(highestCommonRight[2]) + " (" + rightVSLeftPercent[2] + "%)");
		textView_WIS_right.append(formatter.format(highestCommonRight[3]) + " (" + rightVSLeftPercent[3] + "%)");
		textView_AGI_right.append(formatter.format(highestCommonRight[4]) + " (" + rightVSLeftPercent[4] + "%)");
		textView_total_right.append(formatter.format(highestCommonRight[5]) + " (" + rightVSLeftPercent[5] + "%)");
		
		((TextView) activity.findViewById(R.id.statsLabel)).setText(label);
	}
}
