package com.example.Medicineremainder.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import com.example.Medicineremainder.data.MedicineContract.MedicineEntry;

/**
 * {@link ContentProvider} for medicine app.
 */
public class MedicineProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = MedicineProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the medicine table */
    private static final int MEDICINE = 100;

    /** URI matcher code for the content URI for a single medicine in the medicine table */
    private static final int MEDICINE_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(MedicineContract.CONTENT_AUTHORITY, MedicineContract.PATH_MEDICINE, MEDICINE);
        sUriMatcher.addURI(MedicineContract.CONTENT_AUTHORITY, MedicineContract.PATH_MEDICINE + "/#", MEDICINE_ID);
    }

    /** Database helper object */
    private MedicineDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new MedicineDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case MEDICINE:
                // For the MEDICINE code, query the medicine table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the medicine table.
                cursor = database.query(MedicineContract.MedicineEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case MEDICINE_ID:
                // For the MEDICINE_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.medicine/medicine/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = MedicineEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the medicine table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(MedicineEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MEDICINE:
                return insertMedicine(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a medicine into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertMedicine(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(MedicineEntry.COLUMN_MEDICINE_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Medicine requires a name");
        }


        Integer tp = values.getAsInteger(MedicineContract.MedicineEntry.COLUMN_MEDICINE_TIMEPERIOD);
        if (tp == null || !MedicineEntry.isValidTimePeriod(tp)) {
            throw new IllegalArgumentException("Medicine requires valid time period");
        }


        Integer dur = values.getAsInteger(MedicineEntry.COLUMN_MEDICINE_DURATION);
        if (dur != null && dur < 0) {
            throw new IllegalArgumentException("Medicine requires valid dur");
        }



        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new medicine with the given values
        long id = database.insert(MedicineEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the medicine content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MEDICINE:
                return updatemedicine(uri, contentValues, selection, selectionArgs);
            case MEDICINE_ID:
                // For the MEDICINE_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = MedicineContract.MedicineEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatemedicine(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update medicine in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more medicine).
     * Return the number of rows that were successfully updated.
     */
    private int updatemedicine(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link MedicineEntry#COLUMN_MEDICINE_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(MedicineEntry.COLUMN_MEDICINE_NAME)) {
            String name = values.getAsString(MedicineContract.MedicineEntry.COLUMN_MEDICINE_NAME);
            if (name == null) {
                throw new IllegalArgumentException("medicine requires a name");
            }
        }

        // If the {@link MedicineEntry#COLUMN_MEDICINE_TIMEPERIOD} key is present,
        // check that the tp value is valid.
        if (values.containsKey(MedicineEntry.COLUMN_MEDICINE_TIMEPERIOD)) {
            Integer tp = values.getAsInteger(MedicineContract.MedicineEntry.COLUMN_MEDICINE_TIMEPERIOD);
            if (tp == null || !MedicineEntry.isValidTimePeriod(tp)) {
                throw new IllegalArgumentException("medicine requires valid tp");
            }
        }


        if (values.containsKey(MedicineEntry.COLUMN_MEDICINE_DURATION)) {

            Integer dur = values.getAsInteger(MedicineEntry.COLUMN_MEDICINE_DURATION);
            if (dur != null && dur < 0) {
                throw new IllegalArgumentException("medicine requires valid duration");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(MedicineContract.MedicineEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MEDICINE:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(MedicineEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MEDICINE_ID:
                // Delete a single row given by the ID in the URI
                selection = MedicineEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(MedicineEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MEDICINE:
                return MedicineContract.MedicineEntry.CONTENT_LIST_TYPE;
            case MEDICINE_ID:
                return MedicineEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
