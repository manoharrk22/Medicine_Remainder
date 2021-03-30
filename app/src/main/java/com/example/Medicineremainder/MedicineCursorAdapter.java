package com.example.Medicineremainder;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.example.Medicineremainder.data.MedicineContract;
import com.example.Medicineremainder.data.MedicineContract.MedicineEntry;

public class MedicineCursorAdapter extends CursorAdapter {

    public MedicineCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView afbfTextView = (TextView) view.findViewById(R.id.afbf);
        TextView timeperiodTextView = (TextView) view.findViewById(R.id.timeperiod);

        // Find the columns of med attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(MedicineEntry.COLUMN_MEDICINE_NAME);
        int afbfColumnIndex = cursor.getColumnIndex(MedicineContract.MedicineEntry.COLUMN_MEDICINE_AFBF);
        int durColumnIndex = cursor.getColumnIndex(MedicineContract.MedicineEntry.COLUMN_MEDICINE_TIMEPERIOD);

        // Read the med attributes from the Cursor for the current med
        String medName = cursor.getString(nameColumnIndex);
        String medAfBf = cursor.getString(afbfColumnIndex);
        String  medDur = String.valueOf(cursor.getInt(durColumnIndex));

        if (TextUtils.isEmpty(medAfBf)) {
            medAfBf = context.getString(R.string.After_Food);
        }

        // Update the TextViews with the attributes for the current med
        nameTextView.setText(medName);
        afbfTextView.setText(medAfBf);

        switch(medDur){
            case "0": timeperiodTextView.setText("Morning");
                break;
            case "1": timeperiodTextView.setText("Afternoon");
                break;
            case "2": timeperiodTextView.setText("Night");
                break;
        }
    }
}
