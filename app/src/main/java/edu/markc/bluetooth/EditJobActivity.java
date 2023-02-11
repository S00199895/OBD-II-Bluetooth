package edu.markc.bluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.Serializable;
import java.util.ArrayList;

public class EditJobActivity extends AppCompatActivity implements Serializable {

    EditText title;
    EditText content;
    Button save;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_job);
        title =  (EditText) findViewById(R.id.eTTitle);
        content =  (EditText) findViewById(R.id.eTContent);
        save = (Button) findViewById(R.id.btnSave);
        Intent edit = getIntent();
        ArrayList<Note> allNotes = new ArrayList<>();

     //   SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
     //   SharedPreferences.Editor editor = sharedPref.edit();

        if (edit.getExtras() != null)
        {
            Note edited = new Note();
            allNotes = (ArrayList<Note>) edit.getSerializableExtra("allNotes");

            if (Integer.valueOf(edit.getIntExtra("editNoteIndex", -5)) != -5)
            {
                edited = allNotes.get(edit.getIntExtra("editNoteIndex", -5));
                allNotes.remove(edit.getIntExtra("editNoteIndex", -5));

            }
           // Note edited = (Note) edit.getSerializableExtra("editNote");

            title.setText(edited.title);
            content.setText(edited.content);
        }


        ArrayList<Note> finalAllNotes = allNotes;
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
                }
                finalAllNotes.add(n);
                Intent i = new Intent(EditJobActivity.this, JobsActivity.class);
                i.putExtra("allNotes", finalAllNotes);
                startActivity(i);
               // finishActivity(0);
            }
        });
    }
}