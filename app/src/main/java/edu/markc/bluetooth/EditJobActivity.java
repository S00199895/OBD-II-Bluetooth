package edu.markc.bluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
    ListView lvfaults;
    ArrayList<String> gfaults;
    TextView tvselected;
    int jobsselected;
    Gson g;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_job);
        title =  (EditText) findViewById(R.id.eTTitle);
        content =  (EditText) findViewById(R.id.eTContent);
        getSupportActionBar().hide();
tvselected = findViewById(R.id.tvSelected);
tvselected.setText("0 faults selected");
jobsselected =0;
g = new Gson();
        save = (Button) findViewById(R.id.btnSave);
        Intent edit = getIntent();
        importanceSpinner = (Spinner) findViewById(R.id.importance);
        ArrayList<String> allFaults = new ArrayList<>();
        lvfaults = findViewById(R.id.lvfaultsedit);
        importanceSpinner.setAdapter(new ArrayAdapter<Importance>(this, android.R.layout.simple_spinner_item, Importance.values()));

        if (edit.getExtras() != null)
        {
            Note edited = new Note();
            allNotes = (ArrayList<Note>) edit.getSerializableExtra("allNotes");

            fault = edit.getStringExtra("thisFault");
            jobName = edit.getStringExtra("thisJob");
            allFaults = (ArrayList<String>) edit.getSerializableExtra("faults");
            gfaults = allFaults;

            if (Integer.valueOf(edit.getIntExtra("editNoteIndex", -5)) != -5)
            {
                edited = allNotes.get(edit.getIntExtra("editNoteIndex", -5));
                allNotes.remove(edit.getIntExtra("editNoteIndex", -5));
            }

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
        ArrayAdapter<String> adapter = new FaultAdapter(EditJobActivity.this, 0, allFaults);
        lvfaults.setAdapter(adapter);


        ArrayList<Note> finalAllNotes = allNotes;
        ArrayList<String> finalAllFaults = allFaults;
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note n = new Note();
                if (title.getText().toString() != null && content.getText().toString() != null)
                {
                    n.title = title.getText().toString();
                    n.content = content.getText().toString();
                    n.importance = Importance.valueOf( importanceSpinner.getSelectedItem().toString().toUpperCase(Locale.ROOT));
                    n.type = "Active";
                }

                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();

                finalAllNotes.add(n);
                allNotes = duplicatejobs(allNotes, fault);
                Toast.makeText(EditJobActivity.this, "Job saved", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(EditJobActivity.this, JobsActivity.class);
                i.putExtra("allNotes", finalAllNotes);
                i.putExtra("faults", finalAllFaults);
            writeFaults(sharedPref, editor, finalAllFaults);
                startActivity(i);
            }
        });
    }
    public void writeFaults(SharedPreferences sharedPref, SharedPreferences.Editor editor, ArrayList<String> faults) {
        String jsonNotes = g.toJson(faults);
        editor.putString("faults", jsonNotes);
        editor.apply();
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
        {
            String finalTitle = title;
            notesArray.removeIf(n -> n.title.contains(finalTitle) && !n.content.contains(fault));
                }


        return  notesArray;
    }

    public void backtojobs(View view) {
        Intent i = new Intent(EditJobActivity.this, JobsActivity.class);
        i.putExtra("allNotes", allNotes);
        i.putExtra("faults", gfaults);

        startActivity(i);
    }

    class FaultAdapter extends ArrayAdapter<String> {
        Context context;
        List<String> faults;
        String descOut = "";
        public FaultAdapter(Context context, int resource, ArrayList<String> objects) {


            super(context, resource, objects);

            this.context = context;
            this.faults = objects;


        }

        public String getDescOut()
        {
            return descOut;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            String f = faults.get(position);

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.job_link_layout, null);

            TextView title = (TextView) view.findViewById(R.id.tvFault);
            CheckBox cb = (CheckBox) view.findViewById(R.id.cb);


            cb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (cb.isChecked())
                    {
                        descOut += "\nLinked " + f;
                        content.setText(descOut);
                        jobsselected++;
                        tvselected.setText(String.valueOf(jobsselected) + " jobs selected");

                    }
                    else if (!cb.isChecked())
                    {
                        descOut = descOut.replace("\nLinked " + f, "");
                        content.setText(descOut);

                        jobsselected--;
                        tvselected.setText(String.valueOf(jobsselected) + " jobs selected");
                    }
                }
            });

            title.setText(f);
            return  view;
        }

    }
}