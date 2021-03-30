package com.example.Medicineremainder;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import com.example.Medicineremainder.data.MedicineContract;
import com.example.Medicineremainder.data.MedicineContract.MedicineEntry;

/**
 * Allows user to create a new med or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {


    private static final int EXISTING_MEDICINE_LOADER = 0;


    private Uri mCurrentMedUri;
    private EditText mNameEditText;
    private EditText mTextTimeEditText;
    private EditText mDurationEditText;
    private Spinner mDurSpinner;

    TimePickerDialog picker;
    EditText eText;
    Button btnGet;
    TextView tvw;

    private int mDur = MedicineEntry.TIMEPERIOD_MORNING;

    /** Boolean flag that keeps track of whether the med has been edited (true) or not (false) */
    private boolean mMedHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mMedHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mMedHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new med or editing an existing one.
        Intent intent = getIntent();
        mCurrentMedUri = intent.getData();

        if (mCurrentMedUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_med));

            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing med, so change app bar to say "Edit med"
            setTitle(getString(R.string.editor_activity_title_edit_med));

            getLoaderManager().initLoader(EXISTING_MEDICINE_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_med_name);
        mTextTimeEditText = (EditText) findViewById(R.id.edit_med_afbf);
        mDurationEditText = (EditText) findViewById(R.id.edit_med_dur);
        mDurSpinner = (Spinner) findViewById(R.id.spinner_timeperiod);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mTextTimeEditText.setOnTouchListener(mTouchListener);
        mDurationEditText.setOnTouchListener(mTouchListener);
        mDurSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the time period of the med.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter durSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_timezone_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        durSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mDurSpinner.setAdapter(durSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mDurSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.Afternoon))) {
                        mDur = MedicineContract.MedicineEntry.TIMEPERIOD_NOON;
                    } else if (selection.equals(getString(R.string.Night))) {
                        mDur = MedicineEntry.TIMEPERIOD_NIGHT;
                    } else {
                        mDur = MedicineContract.MedicineEntry.TIMEPERIOD_MORNING;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mDur = MedicineContract.MedicineEntry.TIMEPERIOD_MORNING;
            }
        });
    }

    /**
     * Get user input from editor and save med into database.
     */
    private void saveMed() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String afbfString = mTextTimeEditText.getText().toString().trim();
        String durString = mDurationEditText.getText().toString().trim();

        // Check if this is supposed to be a new med
        // and check if all the fields in the editor are blank
        if (mCurrentMedUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(afbfString) &&
                TextUtils.isEmpty(durString) && mDur == MedicineContract.MedicineEntry.TIMEPERIOD_MORNING) {
            // Since no fields were modified, we can return early without creating a new med.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and med attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(MedicineEntry.COLUMN_MEDICINE_NAME, nameString);
        values.put(MedicineEntry.COLUMN_MEDICINE_AFBF, afbfString);
        values.put(MedicineContract.MedicineEntry.COLUMN_MEDICINE_TIMEPERIOD, mDur);
        // If the dur is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int dur = 0;
        if (!TextUtils.isEmpty(durString)) {
            dur = Integer.parseInt(durString);
        }
        values.put(MedicineEntry.COLUMN_MEDICINE_DURATION, dur);

        // Determine if this is a new or existing med by checking if mCurrentMedUri is null or not
        if (mCurrentMedUri == null) {
            Uri newUri = getContentResolver().insert(MedicineContract.MedicineEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_med_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_med_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING med, so update the med with content URI: mCurrentMedUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentMedUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentMedUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_med_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_med_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new med, hide the "Delete" menu item.
        if (mCurrentMedUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save med to database
                saveMed();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the med hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mMedHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        if (!mMedHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all med attributes, define a projection that contains
        // all columns from the med table
        String[] projection = {
                MedicineEntry._ID,
                MedicineEntry.COLUMN_MEDICINE_NAME,
                MedicineEntry.COLUMN_MEDICINE_AFBF,
                MedicineEntry.COLUMN_MEDICINE_TIMEPERIOD,
                MedicineEntry.COLUMN_MEDICINE_DURATION };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentMedUri,         // Query the content URI for the current med
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of med attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(MedicineEntry.COLUMN_MEDICINE_NAME);
            int AfBfColumnIndex = cursor.getColumnIndex(MedicineEntry.COLUMN_MEDICINE_AFBF);
            int tpColumnIndex = cursor.getColumnIndex(MedicineEntry.COLUMN_MEDICINE_TIMEPERIOD);
            int durColumnIndex = cursor.getColumnIndex(MedicineEntry.COLUMN_MEDICINE_DURATION);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String afbf = cursor.getString(AfBfColumnIndex);
            int timeperiod = cursor.getInt(tpColumnIndex);
            int duration = cursor.getInt(durColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mTextTimeEditText.setText(afbf);
            mDurationEditText.setText(Integer.toString(duration));

            switch (timeperiod) {
                case MedicineContract.MedicineEntry.TIMEPERIOD_NOON:
                    mDurSpinner.setSelection(1);
                    break;
                case MedicineEntry.TIMEPERIOD_NIGHT:
                    mDurSpinner.setSelection(2);
                    break;

                case 0:
                    mDurSpinner.setSelection(0);
                    break;

            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mTextTimeEditText.setText("");
        mDurationEditText.setText("");
        mDurSpinner.setSelection(0); // Select "Unknown" 0
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the med.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this med.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the med.
                deleteMed();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the med.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the med in the database.
     */
    private void deleteMed() {
        // Only perform the delete if this is an existing med.
        if (mCurrentMedUri != null) {
            // Call the ContentResolver to delete the med at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentMedUri
            // content URI already identifies the med that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentMedUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_med_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_med_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}