package edu.markc.bluetooth;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.common.reflect.TypeToken;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class JobsActivity extends AppCompatActivity implements Serializable {
   // ListView lV;
    FloatingActionButton add;
    Gson g = new Gson();
    ArrayList<Note> notesArray = new ArrayList<Note>();
    ArrayList<String> faults = new ArrayList<>();
    RadioButton importanceRB;
    RadioButton azRB;
    RadioGroup rgSort;
    RadioButton selected;
    TextView tvascdesc;
    CurrentJobs currentjobs;
    FirebaseFirestore db;
    boolean AZisDescending = false;
    boolean IisDescending = false;
    boolean az = false;
    boolean i = false;

    public interface FragmentListener {
        void updateFragmentList(ArrayList<Note> newnotes);
        void faults(ArrayList<String> faults);
    }
    FragmentListener fragmentListener;
    public void setFragmentListener(FragmentListener listener)
    {

        this.fragmentListener = listener;

    }

    public void sendDataToFragment(ArrayList<Note> newnotes) {
        this.fragmentListener.updateFragmentList(newnotes);
    }

    public void sendFaultsToFragment(ArrayList<String> faults) {
        this.fragmentListener.faults(faults);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);
getSupportActionBar().hide();

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(new TablayoutFragment(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
      // currentjobs = tabLayout.get
               //(CurrentJobs) getSupportFragmentManager().getFragment(savedInstanceState, "CurrentJobs");

       // currentjobs = (CurrentJobs) getSupportFragmentManager().findFragmentById(viewPager.getCurrentItem());

        rgSort = findViewById(R.id.radioGroup);
        tvascdesc = findViewById(R.id.ascdesc);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (readPrefs(sharedPref) != null) {
            notesArray = readJobs();//readPrefs(sharedPref);
            System.out.println("notesmain "+notesArray);
addJobsToFirestore(notesArray);
        }

        if (getIntent().getExtras() != null)
        {
            if (getIntent().getSerializableExtra("allNotes") != null) {
            notesArray = (ArrayList<Note>) getIntent().getSerializableExtra("allNotes");}
            addJobsToFirestore(notesArray);
          //  sendDataToFragment(notesArray);
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

writeFaults(sharedPref, editor, faults);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //                Note e = (Note) parent.getAdapter().getItem(position);
                    String fault = (String) parent.getAdapter().getItem(position);
                    alertDialog(JobsActivity.this, fault);

                }
            });

    //    lV = (ListView) findViewById(R.id.lVNotes);
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
                    TextView severity = (TextView) view.findViewById(R.id.tvSeverity);


                    title.setText(note.title);
                    date.setText(note.timestamp);
                    severity.setText(note.importance.toString());
                  //  note.importance = Importance.HIGH;
                    if (note != null) {
                        severity.setText(note.importance.toString());

                        if (severity.getText().toString() == "High")
                            severity.setBackgroundColor(Color.parseColor("#FF0000"));
                        else if (severity.getText().toString() == "Medium")
                            severity.setBackgroundColor(Color.parseColor("#FFA500"));
                        else if (severity.getText().toString() == "Low") {
                            severity.setBackgroundColor(Color.YELLOW);//.parseColor("#257317"));
                            severity.setTextColor(Color.BLACK);
                        }
                    }
                    return  view;
                }
        }
        System.out.println(notesArray);
       /* Note t = new Note("test" ,"fgeg");
        if (notesArray != null) {
            notesArray.add(t);
        }*/
            ArrayAdapter<Note> adapter = new noteArrayAdapter(this, 0, notesArray);
        //lV.setAdapter(adapter);

        //comparators for note
        Comparator<Note> azcomp = new Comparator<Note>() {
            @Override
            public int compare(Note o1, Note o2) {
                return o1.title.compareTo(o2.title);
            }
        };

        Comparator<Note> icomp = new Comparator<Note>() {
            @Override
            public int compare(Note o1, Note o2) {
                return Integer.compare(o1.importance.ordinal(),o2.importance.ordinal());
              /* if (o1.importance.ordinal() < o2.importance.ordinal())
                   return 1;
               else if (o1.importance.ordinal() > o2.importance.ordinal())
                    return 1;
               return 0;*/
            }
        };

        int selectedID = rgSort.getCheckedRadioButtonId();
        selected = (RadioButton) findViewById(selectedID);
        azRB = findViewById(R.id.azrB);
        importanceRB = findViewById(R.id.irB);

        /*   if (isDescending)
                {
                    Collections.reverse(notesArray);
                }*/

        tvascdesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collections.reverse(notesArray);
                if (tvascdesc.getText().toString().contains("↑"))
                    tvascdesc.setText("↓");
                else if (tvascdesc.getText().toString().contains("↓"))
                    tvascdesc.setText("↑");
                /*lV.invalidateViews();*/
                sendDataToFragment(notesArray);
                sendFaultsToFragment(faults);

            }
        });

        azRB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   int selectedID = rgSort.getCheckedRadioButtonId();
               // selected = (RadioButton) findViewById(selectedID);
                if (AZisDescending) {
                    tvascdesc.setText("↑");
                    Collections.reverse(notesArray);
                    sendDataToFragment(notesArray);
                    AZisDescending = false;
                    IisDescending = false;}
                else {
                    tvascdesc.setText("↓");

                   /* az = true;
                    i = false;*/
                    Collections.sort(notesArray, azcomp);
                    sendDataToFragment(notesArray);
                    AZisDescending = true;
                }
             //   lV.invalidateViews();
            }
        });

        importanceRB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (IisDescending) {
                    tvascdesc.setText("↑");

                    Collections.reverse(notesArray);
                IisDescending = false;
                AZisDescending = false;
                    sendDataToFragment(notesArray);

                }
                else {
/*
                    az = false;
                i = true;*/
                    tvascdesc.setText("↓");

                    Collections.sort(notesArray, icomp);
                    IisDescending = true;
                    sendDataToFragment(notesArray);
                }
                //also do desc, asc
   //             lV.invalidateViews();
            }
        });

        rgSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }

        });
    }

    private ArrayList<Note> readJobs() {
        db = FirebaseFirestore.getInstance();
        System.out.println("STARTING");

        CollectionReference jobs = db.collection("data")
                .document("jobs")
                .collection("alljobs");
        ArrayList<Note> firestorenotes = new ArrayList<>();
        System.out.println("STARTING");
        jobs.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                System.out.println("DOCS");
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    System.out.println(doc.getData());
                    Importance thisI = Importance.valueOf(String.valueOf(doc.getData().get("importance")));
                    String t = String.valueOf(doc.getData().get("title"));
                    String c = String.valueOf(doc.getData().get("content"));
                    String time = String.valueOf(doc.getData().get("timestamp"));
                    Note e = new Note(t, c, thisI, time);
                    System.out.println("the note"+e);
                    firestorenotes.add(e);

                }
                notesArray = firestorenotes;
                sendDataToFragment(notesArray);

            }
        });

        System.out.println("STARTING");
        return firestorenotes;
    }

    private void addJobsToFirestore(ArrayList<Note> jobsforfirestore)
    {
        db=FirebaseFirestore.getInstance();

        CollectionReference jobs = db.collection("data")
                .document("jobs")
                .collection("alljobs");

        for (Note e:
             jobsforfirestore) {
            jobs.document(e.title).set(e);
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

    private void dialogDeleteJob(Context context, ArrayList<Note> finalNotesArray2, int position,/*index of the deleted?*/ArrayAdapter<Note> adapter) {
        //find the code where it deletes
        //put in here
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete job?")
                .setMessage("Are you sure you want to delete this job")
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        Toast.makeText(JobsActivity.this, "Job deleted", Toast.LENGTH_SHORT).show();
                        finalNotesArray2.remove(position);
                        adapter.notifyDataSetChanged();
                        writePrefs(sharedPref, editor, finalNotesArray2);

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
       // sendDataToFragment(notes);
        editor.putString("notes", jsonNotes);
        editor.apply();
    }

    public void writeFaults(SharedPreferences sharedPref, SharedPreferences.Editor editor, ArrayList<String> faults) {
        String jsonNotes = g.toJson(faults);
        // sendDataToFragment(notes);
        editor.putString("faults", jsonNotes);
        editor.apply();
    }

    public ArrayList<String> readFaults(SharedPreferences sharedPref) {
        String json = sharedPref.getString("faults", null);
        if (json == null) {
            return  new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<String>>(){}.getType();

        return g.fromJson(json, type);
    }




}