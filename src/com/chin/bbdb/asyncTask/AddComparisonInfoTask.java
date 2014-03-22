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
	String famName;
	FamStore famStore;
    
    public AddComparisonInfoTask(FamCompareActivity activity) {
        this.activity = activity;
        this.famStore = FamStore.getInstance();
    }

    @Override
	protected Void doInBackground(String... params) {
    	this.famName = params[0];
    	famStore.getStats(this.famName);
    	famStore.getImage(this.famName);
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
        bmImage.setImageBitmap(famStore.getImage(famName));
	}
	
	public void addFamStat() {
	
		TextView maxHP_textView    = (TextView) activity.findViewById(R.id.textView_HP_left);
		TextView maxATK_textView   = (TextView) activity.findViewById(R.id.textView_ATK_left);
		TextView maxDEF_textView   = (TextView) activity.findViewById(R.id.textView_DEF_left);
		TextView maxWIS_textView   = (TextView) activity.findViewById(R.id.textView_WIS_left);
		TextView maxAGI_textView   = (TextView) activity.findViewById(R.id.textView_AGI_left);
		//TextView maxTotal_textView = (TextView) activity.findViewById(R.id.maxTotal_textView);
	
		boolean isWarlord = famStore.isWarlord(famName);
		FamStats stats = famStore.getStats(famName);
	
		DecimalFormat formatter = new DecimalFormat("#,###");
		
		maxHP_textView.setText(formatter.format(stats.maxStats[0]));
		maxATK_textView.setText(formatter.format(stats.maxStats[1]));
		maxDEF_textView.setText(formatter.format(stats.maxStats[2]));
		maxWIS_textView.setText(formatter.format(stats.maxStats[3]));
		maxAGI_textView.setText(formatter.format(stats.maxStats[4]));
		//maxTotal_textView.setText(formatter.format(stats.maxStats[5]));
	}
}
