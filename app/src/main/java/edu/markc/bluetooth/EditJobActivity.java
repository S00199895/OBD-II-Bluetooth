package edu.markc.bluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.Serializable;

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

        if (edit.getExtras() != null)
        {
            Note edited = (Note) edit.getSerializableExtra("editNote");

            title.setText(edited.title);
            content.setText(edited.content);
        }


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

                Intent i = new Intent(EditJobActivity.this, JobsActivity.class);
                i.putExtra("thisNote", n);
                startActivity(i);
            }
        });
    }
}