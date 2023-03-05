package edu.markc.bluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

public class EditJobActivity extends AppCompatActivity implements Serializable {

    EditText title;
    EditText content;
    Button save;
    String fault;
    String jobName;
    ArrayList<Note> allNotes = new ArrayList<>();
    Spinner importanceSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_job);
        title =  (EditText) findViewById(R.id.eTTitle);
        content =  (EditText) findViewById(R.id.eTContent);
        save = (Button) findViewById(R.id.btnSave);
        Intent edit = getIntent();
        importanceSpinner = (Spinner) findViewById(R.id.importance);
        ArrayList<String> allFaults = new ArrayList<>();

        importanceSpinner.setAdapter(new ArrayAdapter<Importance>(this, android.R.layout.simple_spinner_item, Importance.values()));
     //   SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
     //   SharedPreferences.Editor editor = sharedPref.edit();

        if (edit.getExtras() != null)
        {
            Note edited = new Note();
            allNotes = (ArrayList<Note>) edit.getSerializableExtra("allNotes");
            fault = edit.getStringExtra("thisFault");
            jobName = edit.getStringExtra("thisJob");
            allFaults = (ArrayList<String>) edit.getSerializableExtra("faults");

            /*  i.putExtra("thisFault", fault);
                        i.putExtra("allNotes", notesArray);
                        i.putExtra("faults", faults);*/

            if (Integer.valueOf(edit.getIntExtra("editNoteIndex", -5)) != -5)
            {
                edited = allNotes.get(edit.getIntExtra("editNoteIndex", -5));
                allNotes.remove(edit.getIntExtra("editNoteIndex", -5));

            }
           // Note edited = (Note) edit.getSerializableExtra("editNote");

            title.setText(edited.title);
            content.setText(edited.content);
        }

        if (fault != null)
        {
            content.setText(fault);

            if (jobName != null)
            {
                title.setText(jobName);
            }
        }




        ArrayList<Note> finalAllNotes = allNotes;
        ArrayList<String> finalAllFaults = allFaults;
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                                /* Intent i = new Intent(MainActivity.this, JobsActivity.class);
                startActivity(i);*/
                Note n = new Note();
                if (title.getText().toString() != null && content.getText().toString() != null)
                {
                    n.title = title.getText().toString();
                    n.content = content.getText().toString();
                    n.importance = Importance.valueOf( importanceSpinner.getSelectedItem().toString().toUpperCase(Locale.ROOT));
                }
                finalAllNotes.add(n);
                allNotes = duplicatejobs(allNotes, fault);
                //finalAllNotes = duplicatejobs(finalAllNotes);
                Intent i = new Intent(EditJobActivity.this, JobsActivity.class);
                i.putExtra("allNotes", finalAllNotes);
                i.putExtra("faults", finalAllFaults);
                startActivity(i);
               // finishActivity(0);
            }
        });
    }

    private ArrayList<Note> duplicatejobs(ArrayList<Note> notesArray, String fault) {

        ArrayList<String> titles = new ArrayList<String>(notesArray.stream().map(p -> p.title).collect(Collectors.toList()));
        ArrayList<String> test = new ArrayList<>();
        boolean duplicate = false;
        String title = "";
        for (String t : titles)
        {
            if(test.contains(t))
            {
                duplicate = true;
                title = t;
                break;
            }
            else {
                test.add(t);
            }
        }

        if (duplicate == true)
        {/*
            for (Note n: notesArray
            ) {
                if (n.title.contains(title) && !n.content.contains(fault))
                {*/
            String finalTitle = title;
            notesArray.removeIf(n -> n.title.contains(finalTitle) && !n.content.contains(fault));
                }


        return  notesArray;
    }
}