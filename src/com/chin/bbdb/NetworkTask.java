package com.chin.bbdb;

import org.jsoup.Jsoup;

import android.os.AsyncTask;

public class NetworkTask extends AsyncTask<String, Void, String> {
	
	protected String doInBackground(String... params) {
		
		String json = null;
		try {
			json = Jsoup.connect(params[0]).ignoreContentType(true).execute().body();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
    }
}
