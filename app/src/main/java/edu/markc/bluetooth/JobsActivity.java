package edu.markc.bluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JobsActivity extends AppCompatActivity implements Serializable {
    ListView lV;
    FloatingActionButton add;
    Gson g = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);
        ArrayList<Note> notesArray = new ArrayList<Note>();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (readPrefs(sharedPref) != null) {
            notesArray = readPrefs(sharedPref);
        }


        if (getIntent().getExtras() != null)
        {
            notesArray = (ArrayList<Note>) getIntent().getSerializableExtra("allNotes");

            writePrefs(sharedPref, editor, notesArray);
        }


        lV = (ListView) findViewById(R.id.lVNotes);
        add = (FloatingActionButton) findViewById(R.id.addNoteBtn);

        ArrayList<Note> finalNotesArray = notesArray;

        ArrayList<Note> finalNotesArray1 = notesArray;
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writePrefs(sharedPref, editor, finalNotesArray);

                Intent i = new Intent(JobsActivity.this, EditJobActivity.class);
                i.putExtra("allNotes", finalNotesArray1);
                startActivity(i);

            }
        });

            class noteArrayAdapter extends ArrayAdapter<Note> {
            private Context context;
            private List<Note> notes;

            //constructor, call on creation
            public noteArrayAdapter(Context context, int resource, ArrayList<Note> objects) {
                super(context, resource, objects);

                this.context = context;
                this.notes = objects;


            }

                public View getView(int position, View convertView, ViewGroup parent){
                    Note note = notes.get(position);

                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    View view = inflater.inflate(R.layout.job_layout, null);

                    TextView title = (TextView) view.findViewById(R.id.noteTitle);
                    TextView date = (TextView) view.findViewById(R.id.noteDate);

                    title.setText(note.title);
                    date.setText(note.timestamp);

                    return  view;
                }
        }
        System.out.println(notesArray);
       /* Note t = new Note("test" ,"fgeg");
        if (notesArray != null) {
            notesArray.add(t);
        }*/
            ArrayAdapter<Note> adapter = new noteArrayAdapter(this, 0, notesArray);
        lV.setAdapter(adapter);


        ArrayList<Note> finalNotesArray2 = notesArray;
        lV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
              // TextView tVTitle =  view.findViewById(R.id.noteTitle);
               Toast.makeText(JobsActivity.this, "Job deleted", Toast.LENGTH_SHORT).show();
                finalNotesArray2.remove(position);
                adapter.notifyDataSetChanged();
                writePrefs(sharedPref, editor, finalNotesArray2);
               return true;
            }});

        ArrayList<Note> finalNotesArray3 = notesArray;
        lV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note e = (Note) parent.getAdapter().getItem(position);
              //  System.out.println(e.toString());

                Intent i = new Intent(JobsActivity.this, EditJobActivity.class);
                i.putExtra("allNotes", finalNotesArray3);
                i.putExtra("editNoteIndex", finalNotesArray3.indexOf(e));

                //send the array with the intent
                //get and write prefs inside the edit activity
                //try that
            //    notesArray.remove(position);
               // writePrefs(sharedPref, editor, notesArray);
startActivity(i);
            }});
    }

    public ArrayList<Note> readPrefs(SharedPreferences sharedPref) {
        String jsonNotes = sharedPref.getString("notes", null);
        if (jsonNotes == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Note>>(){}.getType();
        return g.fromJson(jsonNotes, type);
    }

    public void writePrefs(SharedPreferences sharedPref, SharedPreferences.Editor editor, ArrayList<Note> notes) {
        String jsonNotes = g.toJson(notes);
        editor.putString("notes", jsonNotes);
        editor.apply();
    }


}