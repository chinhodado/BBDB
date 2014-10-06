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
 * Some utility functions
 * @author Chin
 *
 */
public class Util {
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

    /**
     * Add a row with 1 TextView to a table.
     * @param table The table to add the row to
     * @param textViewString The text of the first Textview
     * @param showLineSeparator true if a line separator is added after the new row
     */
    public static void addRowWithOneTextView(Activity activity,
            TableLayout table, String textViewString, boolean showLineSeparator) {
        TableRow tr = new TableRow(activity);
        TextView tv1 = new TextView(activity); tv1.setText(textViewString); tr.addView(tv1);
        table.addView(tr);
        if (showLineSeparator) addLineSeparator(activity, table);
    }

    /**
     * Add a row with n TextView to a table.
     * @param table The table to add the row to
     * @param numTextView The number of TextView to add
     * @param textViewString The text of the Textviews
     * @param showLineSeparator true if a line separator is added after the new row
     */
    public static void addRowWithNTextView(Activity activity,
            TableLayout table, int numTextView, String[] textViewString, boolean showLineSeparator) {
        TableRow tr = new TableRow(activity);
        for (int i = 0; i<numTextView; i++) {
            TextView tv = new TextView(activity);
            tv.setText(textViewString[i]);
            tr.addView(tv);
        }
        table.addView(tr);
        if (showLineSeparator) addLineSeparator(activity, table);
    }

    /**
     * Given a link to a wikia image (scaled or not scaled), return a (new) scaled version with the specified width
     * @param link The link to the original scaled wikia image
     * @param newWidth The width of the new image
     * @return The link to the new scaled image
     */
    public static String getScaledWikiaImageLink (String link, int newWidth) {
        // TODO: maybe we should use regex...
        int lastSlash = link.lastIndexOf("/");
        String scaledName = link.substring(lastSlash + 1); // the original scaled image name

        // get the original image name
        int prefixIndex = scaledName.indexOf("px-");
        String originalName = scaledName;
        if (prefixIndex != -1) {
            // this is a scaled link
            int firstOriginalImageNamePosition = scaledName.indexOf("px-") + 3;
            originalName = scaledName.substring(firstOriginalImageNamePosition);
        }

        // the new scaled image
        String newScaledName = newWidth + "px-" + originalName;

        // original image link with the slash
        String originalLink = (prefixIndex == -1)? (link + "/") : link.substring(0, lastSlash + 1);

        // complete new link
        String newScaledLink = originalLink + newScaledName;
        if (prefixIndex == -1) {
            // some additional work to turn a normal image to a thumb/scaled one
            newScaledLink = newScaledLink.replace("/images/", "/images/thumb/");
        }
        return newScaledLink;
    }
}
