package net.simplifiedcoding.navigationdrawerexample;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import java.util.ArrayList;

/**
 * Created by divyanshjain on 30/11/17.
 */

public class Database {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_NO = "number";

    private static final String DATABASE_NAME = "Numbers";
    private static final String DATABASE_TABLE = "BlockedApps";
    private static final int DATABASE_VERSION = 1;

    private DbHelper ourHelper;
    private final Context ourContext;
    private SQLiteDatabase ourDatabase;


    private static class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(
                    "CREATE TABLE " + DATABASE_TABLE + " (" +
                            KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            KEY_NO + " TEXT NOT NULL);"
            );

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);

        }
    }

    public Database(Context c) {
        ourContext = c;
    }

    public Database open() throws SQLException {
        ourHelper = new DbHelper(ourContext);
        ourDatabase = ourHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        ourHelper.close();
    }

    public long createEntry(String no) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_NO, no);
        return ourDatabase.insert(DATABASE_TABLE, null, cv);
    }

    public String getData() {

        String[] columns = new String[]{KEY_ROWID, KEY_NO};
        Cursor c = ourDatabase.query(DATABASE_TABLE, columns, null, null, null, null, null);
        String result = "";

        int iRow = c.getColumnIndex(KEY_ROWID);
        int No = c.getColumnIndex(KEY_NO);

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            result = result + c.getString(iRow) + "                  " + c.getString(No) + "\n" + "\n";
        }

        return result;
    }

    public String getNo(long l) throws SQLException {
        String[] columns = new String[]{KEY_ROWID, KEY_NO};
        Cursor c = ourDatabase.query(DATABASE_TABLE, columns, KEY_ROWID + "=" + l, null, null, null, null);
        if (c!= null) {
            c.moveToFirst();
            String No = c.getString(1);
            return No;
        }
        return null;
    }

    public void updateEntry(long lRow, String mNo) throws SQLException {

        ContentValues cvUpdate = new ContentValues();
        cvUpdate.put(KEY_NO, mNo);
        ourDatabase.update(DATABASE_TABLE, cvUpdate, KEY_ROWID + "=" + lRow, null);
    }

    public void deleteEntry(long lRow1) throws SQLException {
        ourDatabase.delete(DATABASE_TABLE, KEY_ROWID + "=" + lRow1, null);

    }

    public ArrayList<String> getValues() {

        ArrayList<String> values = new ArrayList<String>();
        String[] columns = new String[]{KEY_ROWID, KEY_NO};
        Cursor c = ourDatabase.query(DATABASE_TABLE, columns, null, null, null, null, null);
        String result = "";

        int iRow = c.getColumnIndex(KEY_ROWID);
        int No = c.getColumnIndex(KEY_NO);

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            result = c.getString(No);
            values.add(result);
        }

        return values;
    }

    public ArrayList<Pair<String, String>> getIdAndValues() {

        ArrayList<Pair<String, String>> values = new ArrayList<>();
        String[] columns = new String[]{KEY_ROWID, KEY_NO};
        Cursor c = ourDatabase.query(DATABASE_TABLE, columns, null, null, null, null, null);
        String result = "";

        int iRow = c.getColumnIndex(KEY_ROWID);
        int No = c.getColumnIndex(KEY_NO);

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            result = c.getString(No);
            values.add(Pair.create(c.getString(iRow), result));
        }

        return values;
    }
}
