package com.chin.bbdb;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

/**
 * A class for making queries to our sqlite database
 * @author Chin
 *
 */
public class DatabaseQuerier {
    private static SQLiteDatabase db;
    Context context;

    public DatabaseQuerier(Context context) {
        this.context = context;
    }

    /**
     * Execute a search query and return the result
     * @param whereClause The where clause that represents the criteria of the search
     * @return List of familiars that satisfy the criteria
     */
    public ArrayList<String> executeQuery(String whereClause) {
        ArrayList<String> resultSet = new ArrayList<String>();
        try {
            SQLiteDatabase db = getDatabase();
            Cursor cursor = db.rawQuery("Select name from Familiar where " + whereClause, null);
            if (cursor.moveToFirst()) {
                while (cursor.isAfterLast() == false) {
                    String name = cursor.getString(cursor.getColumnIndex("name"));

                    // verify that the fam really exists in out database
                    if (FamStore.famList.contains(name)) {
                        resultSet.add(name);
                    }
                    else {
                        Log.i("Search", "Not found: " + name);
                    }

                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(context, "An error occurred while searching.", Toast.LENGTH_SHORT);
            toast.show();
        }

        return resultSet;
    }

    private SQLiteDatabase getDatabase() {
        if (db == null) {
            BBSqliteDatabase dbHelper = new BBSqliteDatabase(context);
            db = dbHelper.getReadableDatabase();
        }
        return db;
    }
}
