package com.chin.bbdb.asyncTask;

import java.io.InputStream;

import com.chin.bbdb.R;
import com.chin.bbdb.activity.FamDetailActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    FamDetailActivity activity;

    public DownloadImageTask(FamDetailActivity activity) {
        this.activity = activity;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        // remove the spinner
        ProgressBar pgrBar = (ProgressBar) activity.findViewById(R.id.progressBar1);
        LinearLayout layout = (LinearLayout) activity.findViewById(R.id.linearLayout1);
        layout.removeView(pgrBar);

        // set the image
        ImageView bmImage = (ImageView) activity.findViewById(R.id.imageView_leftFam);
        bmImage.setImageBitmap(result);
    }
}
