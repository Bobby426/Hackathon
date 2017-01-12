package com.example.bobby.hackathon;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


/**
 * Created by Bobby on 04.01.2017.
 */

public class DataSource {
    private static final String LOG_TAG = DataSource.class.getSimpleName();


    private SQLiteDatabase database;
    private DbHelper dbHelper;

    private String[] columns = {
            DbHelper.COLUMN_MACADRESS,
            DbHelper.COLUMN_DESCRIPTION,

    };


    public DataSource(Context context) {
        Log.d(LOG_TAG, "Unsere DataSource erzeugt jetzt den dbHelper.");
        dbHelper = new DbHelper(context);
    }

    public void open() {
        Log.d(LOG_TAG, "Eine Referenz auf die Datenbank wird jetzt angefragt.");
        database = dbHelper.getWritableDatabase();
        Log.d(LOG_TAG, "Datenbank-Referenz erhalten. Pfad zur Datenbank: " + database.getPath());
    }

    public void close() {
        dbHelper.close();
        Log.d(LOG_TAG, "Datenbank mit Hilfe des DbHelpers geschlossen.");
    }

    public void delete(){
        database.execSQL("DROP TABLE IF EXISTS " + DbHelper.TABLE_BEACON_LIST);
    }

    public void createBeacon(String macAdress, String description) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_MACADRESS, macAdress);
        values.put(DbHelper.COLUMN_DESCRIPTION, description);

        long insertId = database.insert(DbHelper.TABLE_BEACON_LIST, null, values);



    }

    private Beacon cursorToBeacon(Cursor cursor) {
        int idMacAdress = cursor.getColumnIndex(DbHelper.COLUMN_MACADRESS);
        int idDescription = cursor.getColumnIndex(DbHelper.COLUMN_DESCRIPTION);
        String macAdress, description;

        try {
            macAdress= cursor.getString(idMacAdress);
            description = cursor.getString(idDescription);
        }catch (CursorIndexOutOfBoundsException e){
            macAdress = "";
            description = "";
        }

        Beacon beacon = new Beacon(macAdress, description);

        return beacon;
    }

    public Beacon findBeacon(String macAdresse){
        Cursor cursor = database.query(DbHelper.TABLE_BEACON_LIST,
                columns, "macAdress=?", new String[] { macAdresse }, null, null, null);

        cursor.moveToFirst();

        return cursorToBeacon(cursor);

    }
// DbHelper.COLUMN_MACADRESS + "=" + macAdresse

}
