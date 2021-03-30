package com.example.Medicineremainder.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.Medicineremainder.data.MedicineContract.MedicineEntry;


/**
 * Database helper for Medicine app. Manages database creation and version management.
 */
public class MedicineDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = MedicineDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "medicine.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link MedicineDbHelper}.
     *
     * @param context of the app
     */
    public MedicineDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the med table
        String SQL_CREATE_MEDICINE_TABLE =  "CREATE TABLE " + MedicineContract.MedicineEntry.TABLE_NAME + " ("
                + MedicineEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MedicineContract.MedicineEntry.COLUMN_MEDICINE_NAME + " TEXT NOT NULL, "
                + MedicineContract.MedicineEntry.COLUMN_MEDICINE_AFBF + " TEXT, "
                + MedicineContract.MedicineEntry.COLUMN_MEDICINE_TIMEPERIOD + " INTEGER NOT NULL, "
                + MedicineEntry.COLUMN_MEDICINE_DURATION + " INTEGER NOT NULL DEFAULT 0);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_MEDICINE_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}