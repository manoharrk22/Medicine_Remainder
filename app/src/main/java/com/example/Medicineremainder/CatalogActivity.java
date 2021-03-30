package com.example.Medicineremainder;
import com.example.Medicineremainder.data.MedicineContract;
import com.example.Medicineremainder.data.MedicineContract.MedicineEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the med data loader
     */
    private static final int MEDICINE_LOADER = 0;

    /**
     * Adapter for the ListView
     */
    MedicineCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the med data
        ListView medListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        medListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of med data in the Cursor.
        // There is no med data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new MedicineCursorAdapter(this, null);
        medListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        medListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific med that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link MedicineEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.meds/meds/2"
                // if the med with ID 2 was clicked on.
                Uri currentmedUri = ContentUris.withAppendedId(MedicineContract.MedicineEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentmedUri);

                // Launch the {@link EditorActivity} to display the data for the current med.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(MEDICINE_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded med data into the database. For debugging purposes only.
     */
    private void insertMed() {
        // Create a ContentValues object where column names are the keys,
        // and Toto's med attributes are the values.
        ContentValues values = new ContentValues();
        values.put(MedicineContract.MedicineEntry.COLUMN_MEDICINE_NAME, "Paracetamol");
        values.put(MedicineContract.MedicineEntry.COLUMN_MEDICINE_AFBF, "Before Food");
        values.put(MedicineContract.MedicineEntry.COLUMN_MEDICINE_TIMEPERIOD, MedicineContract.MedicineEntry.TIMEPERIOD_NOON);
        values.put(MedicineEntry.COLUMN_MEDICINE_DURATION, 7);

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link MedicineEntry#CONTENT_URI} to indicate that we want to insert
        // into the meds database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(MedicineEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all meds in the database.
     */
    private void deleteAllMedicine() {
        int rowsDeleted = getContentResolver().delete(MedicineContract.MedicineEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from med database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertMed();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllMedicine();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                MedicineContract.MedicineEntry._ID,
                MedicineContract.MedicineEntry.COLUMN_MEDICINE_NAME,
                MedicineContract.MedicineEntry.COLUMN_MEDICINE_AFBF,
                MedicineContract.MedicineEntry.COLUMN_MEDICINE_TIMEPERIOD};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                MedicineContract.MedicineEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link MedicineCursorAdapter} with this new cursor containing updated med data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
