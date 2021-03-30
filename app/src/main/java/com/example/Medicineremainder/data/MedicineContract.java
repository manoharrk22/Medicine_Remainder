package com.example.Medicineremainder.data;

import android.net.Uri;
import android.content.ContentResolver;
import android.provider.BaseColumns;

/**
 * API Contract for the Medicine app.
 */
public final class MedicineContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private MedicineContract() {}

    public static final String CONTENT_AUTHORITY = "com.example.Medicineremainder";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    public static final String PATH_MEDICINE = "medicine";


    public static final class MedicineEntry implements BaseColumns {


        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MEDICINE);


        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MEDICINE;


        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MEDICINE;


        public final static String TABLE_NAME = "medicine";


        public final static String _ID = BaseColumns._ID;


        public final static String COLUMN_MEDICINE_NAME ="name";


        public final static String COLUMN_MEDICINE_AFBF = "afbf";


        public final static String COLUMN_MEDICINE_TIMEPERIOD = "timeperiod";


        public final static String COLUMN_MEDICINE_DURATION = "duration";


        public static final int TIMEPERIOD_MORNING = 0;
        public static final int TIMEPERIOD_NOON = 1;
        public static final int TIMEPERIOD_NIGHT = 2;

        public static boolean isValidTimePeriod(int dur) {
            if (dur == TIMEPERIOD_MORNING || dur == TIMEPERIOD_NOON || dur == TIMEPERIOD_NIGHT) {
                return true;
            }
            return false;
        }
    }

}

