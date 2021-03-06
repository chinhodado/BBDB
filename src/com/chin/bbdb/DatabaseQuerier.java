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
    public ArrayList<String> executeQuery(String famCriteria, String skillCriteria) {
        ArrayList<String> resultSet = new ArrayList<String>();
        try {
            SQLiteDatabase db = getDatabase();

            String joinClause = " and (f.skillId1 = s.id or f.skillId2 = s.id or f.skillId3 = s.id)";
            String finalEvoClause = " and popeAtk != \"N/A\" ";
            String tables;
            String whereClause;
            if (!famCriteria.equals("") && !skillCriteria.equals("")) {
                tables = "familiar f, skill s";
                whereClause = famCriteria + " and " + skillCriteria + joinClause + finalEvoClause;
            }
            else if (!skillCriteria.equals("")){ // skill but no fam
                tables = "familiar f, skill s";
                whereClause = skillCriteria + joinClause;
            }
            else { // fam but no skill
                tables = "familiar f";
                whereClause = famCriteria + finalEvoClause;
            }
            Cursor cursor = db.rawQuery("Select f.name from " + tables + " where " + whereClause, null);
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

    public SQLiteDatabase getDatabase() {
        if (db == null) {
            BBSqliteDatabase dbHelper = new BBSqliteDatabase(context);
            db = dbHelper.getReadableDatabase();
        }
        return db;
    }
}
