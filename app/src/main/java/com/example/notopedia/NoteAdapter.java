package com.example.notopedia;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.example.notopedia.data.Contract.NoteEntry;


public class NoteAdapter extends CursorAdapter {

    public NoteAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView nameTextView = (TextView) view.findViewById(R.id.title_id);
        int nameColumnIndex = cursor.getColumnIndex(NoteEntry.COLUMN_NOTE_TITLE);
        String petName = cursor.getString(nameColumnIndex);
        nameTextView.setText(petName);
    }
}