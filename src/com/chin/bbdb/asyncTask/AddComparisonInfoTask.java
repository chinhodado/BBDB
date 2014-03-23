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
	
		TextView maxHP_textView_left    = (TextView) activity.findViewById(R.id.textView_HP_left);
		TextView maxATK_textView_left   = (TextView) activity.findViewById(R.id.textView_ATK_left);
		TextView maxDEF_textView_left   = (TextView) activity.findViewById(R.id.textView_DEF_left);
		TextView maxWIS_textView_left   = (TextView) activity.findViewById(R.id.textView_WIS_left);
		TextView maxAGI_textView_left   = (TextView) activity.findViewById(R.id.textView_AGI_left);
		//TextView maxTotal_textView = (TextView) activity.findViewById(R.id.maxTotal_textView);
		
		TextView maxHP_textView_right    = (TextView) activity.findViewById(R.id.textView_HP_right);
		TextView maxATK_textView_right   = (TextView) activity.findViewById(R.id.textView_ATK_right);
		TextView maxDEF_textView_right   = (TextView) activity.findViewById(R.id.textView_DEF_right);
		TextView maxWIS_textView_right   = (TextView) activity.findViewById(R.id.textView_WIS_right);
		TextView maxAGI_textView_right   = (TextView) activity.findViewById(R.id.textView_AGI_right);
	
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
		
		maxHP_textView_left.append(formatter.format(highestCommonLeft[0]));
		maxATK_textView_left.append(formatter.format(highestCommonLeft[1]));
		maxDEF_textView_left.append(formatter.format(highestCommonLeft[2]));
		maxWIS_textView_left.append(formatter.format(highestCommonLeft[3]));
		maxAGI_textView_left.append(formatter.format(highestCommonLeft[4]));
		//maxTotal_textView.setText(formatter.format(stats.maxStats[5]));
		
		maxHP_textView_right.append(formatter.format(highestCommonRight[0]));
		maxATK_textView_right.append(formatter.format(highestCommonRight[1]));
		maxDEF_textView_right.append(formatter.format(highestCommonRight[2]));
		maxWIS_textView_right.append(formatter.format(highestCommonRight[3]));
		maxAGI_textView_right.append(formatter.format(highestCommonRight[4]));
		
		((TextView) activity.findViewById(R.id.statsLabel)).setText(label);
	}
}
