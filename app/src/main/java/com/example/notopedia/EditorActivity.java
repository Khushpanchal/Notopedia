package com.example.notopedia;

import android.app.AlertDialog;
import android.app.LoaderManager;
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
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import com.example.notopedia.data.Contract.NoteEntry;


public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_NOTE_LOADER = 0;
    private Uri mCurrentNoteUri;
    private EditText mTitleEditText;
    private EditText mDescriptionEditText;
    private boolean mNotehasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mNotehasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        Intent intent = getIntent();
        mCurrentNoteUri = intent.getData();

        if(mCurrentNoteUri == null){
            setTitle("Add a Note");
            invalidateOptionsMenu();
        }else {
            setTitle("Edit a Note");
            getLoaderManager().initLoader(EXISTING_NOTE_LOADER, null, this);
        }

        mTitleEditText = (EditText) findViewById(R.id.etTitleIdEdit);
        mDescriptionEditText = (EditText) findViewById(R.id.etDescriptionIdEdit);

        mTitleEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                NoteEntry._ID,
                NoteEntry.COLUMN_NOTE_TITLE,
                NoteEntry.COLUMN_NOTE_DESCRIPTION};

        return new CursorLoader(this, mCurrentNoteUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int titleCol = cursor.getColumnIndex(NoteEntry.COLUMN_NOTE_TITLE);
            int descriptionCol = cursor.getColumnIndex(NoteEntry.COLUMN_NOTE_DESCRIPTION);

            String title = cursor.getString(titleCol);
            String description = cursor.getString(descriptionCol);

            mTitleEditText.setText(title);
            mDescriptionEditText.setText(description);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentNoteUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                savePet();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mNotehasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mNotehasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePet() {
        
        if (mCurrentNoteUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentNoteUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, "Deletetion Failed",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Successfully Deleted",
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    private void savePet() {
        String title = mTitleEditText.getText().toString().trim();
        String description = mDescriptionEditText.getText().toString().trim();

        if (mCurrentNoteUri == null &&
                TextUtils.isEmpty(title) && TextUtils.isEmpty(description)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(NoteEntry.COLUMN_NOTE_TITLE, title);
        values.put(NoteEntry.COLUMN_NOTE_DESCRIPTION, description);


        if (mCurrentNoteUri == null) {
            Uri newUri = getContentResolver().insert(NoteEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, "Insertion Failed",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Successfully Inserted",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentNoteUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, "Update Failed",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Updated Successfully",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    
}
