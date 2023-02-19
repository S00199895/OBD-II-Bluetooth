package edu.markc.bluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JobsActivity extends AppCompatActivity implements Serializable {
    ListView lV;
    FloatingActionButton add;
    Gson g = new Gson();
    ArrayList<Note> notesArray = new ArrayList<Note>();
    ArrayList<String> faults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);
    //    alertDialog(JobsActivity.this);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (readPrefs(sharedPref) != null) {
            notesArray = readPrefs(sharedPref);

        }

        if (getIntent().getExtras() != null)
        {
            if (getIntent().getSerializableExtra("allNotes") != null) {
            notesArray = (ArrayList<Note>) getIntent().getSerializableExtra("allNotes");}
            faults = (ArrayList<String>) getIntent().getSerializableExtra("faults");
            writePrefs(sharedPref, editor, notesArray);
        }

        if (faults == null) {
            faults = new ArrayList<>();
            faults.add("No faults found");
        }

            ArrayAdapter<String> faultsadapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, faults);
            ListView listView = (ListView) findViewById(R.id.lVFaults);
            listView.setAdapter(faultsadapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //                Note e = (Note) parent.getAdapter().getItem(position);
                    String fault = (String) parent.getAdapter().getItem(position);
                    alertDialog(JobsActivity.this, fault);

                }
            });

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
                i.putExtra("faults", faults);
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

       // notesArray = duplicatejobs(notesArray);

        checkFaultLinks(notesArray, faults);


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



    private void checkFaultLinks(ArrayList<Note> notesArray, ArrayList<String> faults) {
        //get the last/newest note title
        //loop through faults and alert dialog the one that matches
        //yes = put fault in desc
        //no = ---
        String newest = "";
if  (notesArray.size() > 0) {
        newest = notesArray.get(notesArray.size() - 1).title;
    }
            String[] newestW = newest.split(" ");
            String fw = "";
            //convert to array
            ArrayList<String> faultsW = new ArrayList<>();
            for (String f : faults
            ) {
                for (String f1 : f.split(" ")
                ) {
                    faultsW.add(f1);
                }

            }
            //end of faults loop

        boolean hasCommonWords = false;

        for (String word1 : newestW) {
            for (String word2 : faultsW) {
                if (word1.equalsIgnoreCase(word2)) {
                    hasCommonWords = true;
                    fw = word1;
                    break;
                }
            }
            if (hasCommonWords) {
                //show dialog for adding desc
                linkJob(fw, faults, newest);
                break;
            }
        }
    }

    private void linkJob(String fw, ArrayList<String> faults, String jobName)
    {
        String thisFault = "";
        for (String f: faults)
        {
            if(f.toLowerCase().contains(fw.toLowerCase()))
            {
                thisFault =f;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(JobsActivity.this);

        String finalThisFault = thisFault;
        builder.setTitle("Job link found")
                .setMessage("The new job could be associated with your car's fault codes. Would you like to add the description to this job?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent i = new Intent(JobsActivity.this, EditJobActivity.class);
                        i.putExtra("thisFault", finalThisFault);
                        i.putExtra("thisJob", jobName);
                        i.putExtra("allNotes", notesArray);
                        i.putExtra("faults", faults);
                        startActivity(i);

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked no button
                    }
                });
        if (thisFault != "")
        {
        AlertDialog dialog = builder.create();
        dialog.show();
        }
    }

    private void alertDialog(Context context, String fault) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Fault Code")
                .setMessage("Create a job associated with this fault?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked yes button
                        //make intent
                        //make note to pass - description is the fault code string
                        Intent i = new Intent(JobsActivity.this, EditJobActivity.class);
                        i.putExtra("thisFault", fault);
                        i.putExtra("allNotes", notesArray);
                        i.putExtra("faults", faults);
                        startActivity(i);

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked no button
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public ArrayList<String> readFaults(SharedPreferences sharedPref) {
        String json = sharedPref.getString("faults", null);
        if (json == null) {
            return  new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<String>>(){}.getType();

        return g.fromJson(json, type);
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