package com.chin.bbdb;
import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Some utility functions to help with doing the layout
 * @author Chin
 *
 */
public class LayoutUtil {
	public static void addLineSeparator(Activity activity, ViewGroup view) {
		View tmpView = new View(activity);
		tmpView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
		tmpView.setBackgroundColor(0xff444444);//dark grey
		view.addView(tmpView);
	}
	
	/**
	 * Add a row with 2 TextView (e.g. Title/Description) to a table.
	 * @param table The table to add the row to
	 * @param textView1String The text of the first Textview
	 * @param textView2String The text of the second TextView
	 * @param showLineSeparator true if a line separator is added after the new row
	 */
	public static void addRowWithTwoTextView(Activity activity, 
			TableLayout table, String textView1String, String textView2String, boolean showLineSeparator) {
		TableRow tr = new TableRow(activity);
		TextView tv1 = new TextView(activity); tv1.setText(textView1String); tr.addView(tv1);
		TextView tv2 = new TextView(activity); tv2.setText(textView2String); tr.addView(tv2);
		table.addView(tr);
		if (showLineSeparator) addLineSeparator(activity, table);
	}
	
	
	/**
	 * Add a row with 2 TextView (e.g. Title/Description) to a table.
	 * @param table The table to add the row to
	 * @param textView1String The text of the first Textview
	 * @param textView2String The text of the second TextView
	 * @param typeface The typeface for the two TextView
	 * @param showLineSeparator true if a line separator is added after the new row
	 */
	public static void addRowWithTwoTextView(Activity activity, 
			TableLayout table, String textView1String, String textView2String, boolean showLineSeparator, Typeface typeface) {
		TableRow tr = new TableRow(activity);
		TextView tv1 = new TextView(activity); tv1.setText(textView1String); tr.addView(tv1);
		TextView tv2 = new TextView(activity); tv2.setText(textView2String); tr.addView(tv2);
		table.addView(tr);
		if (showLineSeparator) addLineSeparator(activity, table);
		tv1.setTypeface(typeface); tv2.setTypeface(typeface);
	}
}
