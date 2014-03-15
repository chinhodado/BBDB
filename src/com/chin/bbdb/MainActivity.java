package com.chin.bbdb;

import java.util.ArrayList;
import java.util.Hashtable;
import org.json.JSONArray;
import org.json.JSONObject;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends Activity {
	
	public final Activity activity = this;
	public final static String FAM_LINK = "com.chin.BBDB.LINK";
	public final static String FAM_NAME = "com.chin.BBDB.NAME";
    private EditText famEditText;
    private ListView famListView;
    public static String jsonString = null;
    public static ArrayList<String> famList = null;
    public static Hashtable<String, String> famLinkTable = null;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
			
		// get the familiar list and their wiki url
		if (famList == null) {
			try {
				String url = "http://bloodbrothersgame.wikia.com/api/v1/Articles/List?category=Familiars&limit=5000&namespaces=0";
				jsonString = new NetworkTask().execute(url).get();
				JSONObject myJSON = new JSONObject(jsonString);
				
				famList = new ArrayList<String>();
				famLinkTable = new Hashtable<String, String>();				
				
				JSONArray myArray = myJSON.getJSONArray("items");
				for (int i = 0; i < myArray.length(); i++) {
					String famName = myArray.getJSONObject(i).getString("title");
					famList.add(famName);
					famLinkTable.put(famName, myArray.getJSONObject(i).getString("url"));
				}	
			} catch (Exception e) {
				if (android.os.Build.VERSION.SDK_INT >= 11)
				{
					DialogFragment newFragment = new NetworkDialogFragment();
					newFragment.setCancelable(false);
				    newFragment.show(getFragmentManager(), "no net");
					e.printStackTrace();					
				}
				return;
			}
		}
		
		try {
			final RegexFilterArrayAdapter<String> adapter = new RegexFilterArrayAdapter<String>(this, android.R.layout.simple_list_item_1, famList);
			
			famEditText = (EditText) findViewById(R.id.famEditText);	
			
			famEditText.addTextChangedListener(new TextWatcher() {
			    public void onTextChanged(CharSequence s, int start, int before, int count) {
			        //adapter.getFilter().filter(s);
			    }

			    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			    public void afterTextChanged(Editable s) {
			    	adapter.getFilter().filter(s);
			    }
			});
			
			famListView = (ListView) findViewById(R.id.famListView);
			famListView.setAdapter(adapter);
			famListView.setOnItemClickListener(new OnItemClickListener(){
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) 
			      {
			            String famName = (String)arg0.getItemAtPosition(position); 
			            Intent intent = new Intent(activity, FamDetailActivity.class);
			            intent.putExtra(FAM_NAME, famName);
			            intent.putExtra(FAM_LINK, MainActivity.famLinkTable.get(famName));
			            startActivity(intent);
			      }
			});
		} catch (Exception e) {
			Log.e("MainActivity", "Error setting up the fam list");
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
