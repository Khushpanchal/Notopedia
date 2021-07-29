package com.example.notopedia;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.notopedia.data.Contract.NoteEntry;
import com.example.notopedia.data.Contract;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {


    private static final int NOTE_LOADER = 0;
    private NoteAdapter madapter;
    private Button maddNoteButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        maddNoteButton = findViewById(R.id.addNoteButtonId);
        maddNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView listView = findViewById(R.id.list);
        madapter = new NoteAdapter(this, null);
        listView.setAdapter(madapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                Uri currentNoteUri = ContentUris.withAppendedId(Contract.NoteEntry.CONTENT_URI, id);
                intent.setData(currentNoteUri);
                startActivity(intent);
            }
        });
        getLoaderManager().initLoader(NOTE_LOADER, null, this);
    }
    
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                NoteEntry._ID,
                NoteEntry.COLUMN_NOTE_TITLE};

        return new CursorLoader(this,   // Parent activity context
                NoteEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }
    

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        madapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        madapter.swapCursor(null);
    }
}
