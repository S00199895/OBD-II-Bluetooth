package edu.markc.bluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class JobsActivity extends AppCompatActivity implements Serializable {
   // ListView lV;
    FloatingActionButton add;
    Gson g = new Gson();
    public ArrayList<Note> notesArray = new ArrayList<Note>();
    ArrayList<String> faults = new ArrayList<>();
    RadioButton importanceRB;
    RadioButton azRB;
    RadioGroup rgSort;
    RadioButton selected;
    TextView tvascdesc;
    CurrentJobs currentjobs;
    ArrayList<Note> resolvedjobs;
    FirebaseFirestore db;
    boolean AZisDescending = false;
    boolean IisDescending = false;
    boolean az = false;
    boolean i = false;

    LinkedBlockingQueue<SFC> Gsfcs;

    ArrayAdapter<Note> currentadapter = null;

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
        rgSort = findViewById(R.id.radioGroup);
        tvascdesc = findViewById(R.id.ascdesc);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (notesArray == null) {
            notesArray = readJobs();
            System.out.println("notesmain " + notesArray);
            addJobsToFirestore(notesArray);
        }
resolvedjobs = readResolvedJobs();
        if (getIntent().getExtras() != null)
        {
            if (getIntent().getSerializableExtra("allNotes") != null) {
            notesArray = (ArrayList<Note>) getIntent().getSerializableExtra("allNotes");}
            addJobsToFirestore(notesArray);
          //  sendDataToFragment(notesArray);
            faults = (ArrayList<String>) getIntent().getSerializableExtra("faults");

            Gsfcs = (LinkedBlockingQueue<SFC>) getIntent().getSerializableExtra("Gsfcs");
            addJobsToFirestore(notesArray);
        }

        if (faults == null) {
            faults = new ArrayList<>();
            faults.add("No faults found");
        }

            ArrayAdapter<String> faultsadapter = new ArrayAdapter<>(this, R.layout.faultstyle, faults);
            ListView listView = (ListView) findViewById(R.id.lVFaults);

            listView.setAdapter(faultsadapter);

writeFaults(sharedPref, editor, faults);

        Toolbar toolbar = findViewById(R.id.navbar);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                System.out.println(item.getTitle().toString());
                item.getTitle().toString();
                Intent i;
                switch (item.getTitle().toString()) {
                    case "home":
                        i = new Intent(JobsActivity.this, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.putExtra("Gsfcs", Gsfcs);
                        i.putExtra("faults", faults);
                        startActivity(i);
                        return true;
                    case "stats":
                        i = new Intent(JobsActivity.this, StatsActivity.class);
                        //putextra the faults
                        i.putExtra("faults", faults);
                        i.putExtra("Gsfcs", Gsfcs);
                        i.putExtra("faults", faults);
                        startActivity(i);
                        return true;
                    case "fuel":
                        i = new Intent(JobsActivity.this, FuelActivity.class);

                        i.putExtra("Gsfcs", Gsfcs);
                        i.putExtra("faults", faults);
                        startActivity(i);
                        return true;
                    case "jobs":
                        return true;
                    default:
                        return false;
                }
            }
        });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String fault = (String) parent.getAdapter().getItem(position);
                    alertDialog(JobsActivity.this, fault);

                }
            });

        add = (FloatingActionButton) findViewById(R.id.addNoteBtn);
        ArrayList<Note> finalNotesArray = notesArray;

        ArrayList<Note> finalNotesArray1 = notesArray;
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addJobsToFirestore(finalNotesArray);

                Intent i = new Intent(JobsActivity.this, EditJobActivity.class);
                i.putExtra("allNotes", finalNotesArray1);
                i.putExtra("faults", faults);
                startActivity(i);

            }
        });

            class noteArrayAdapter extends ArrayAdapter<Note> {
            private Context context;
            private List<Note> notes;

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
            ArrayAdapter<Note> adapter = new noteArrayAdapter(this, 0, notesArray);
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

            }
        };

        int selectedID = rgSort.getCheckedRadioButtonId();
        selected = (RadioButton) findViewById(selectedID);
        azRB = findViewById(R.id.azrB);
        importanceRB = findViewById(R.id.irB);

        tvascdesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvascdesc.getText().toString().contains("↑"))
                    tvascdesc.setText("↓");
                else if (tvascdesc.getText().toString().contains("↓"))
                    tvascdesc.setText("↑");
                if (notesArray.size() == 0){
                    currentadapter = addToAdapter();
                    if (currentadapter == null)
                        currentadapter = addToAdapter();}
                Collections.reverse(notesArray);
                sendDataToFragment(notesArray);
                sendFaultsToFragment(faults);
            }
        });

        azRB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (AZisDescending) {
                    tvascdesc.setText("↑");
                    Collections.reverse(notesArray);
                    AZisDescending = false;
                    IisDescending = false;
                    sendDataToFragment(notesArray);
                   }
                else {
                    if (notesArray.size() == 0){
                        currentadapter = addToAdapter();
                        if (currentadapter == null)
                            currentadapter = addToAdapter();}
                    else{
                        tvascdesc.setText("↓");

                        Collections.sort(notesArray, azcomp);
                        AZisDescending = true;
                        sendDataToFragment(notesArray);
                    }
                }
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
                    if (notesArray.size() == 0){
                    currentadapter = addToAdapter();
                    if (currentadapter == null)
                        currentadapter = addToAdapter();}
                    else{
                        tvascdesc.setText("↓");

                        Collections.sort(notesArray, icomp);
                    IisDescending = true;
                    sendDataToFragment(notesArray);
                    }
                }
            }
        });

        rgSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }

        });
    }

    private ArrayAdapter<Note> addToAdapter()
    {
        ArrayAdapter<Note> currentadapter = CurrentJobs.getInstance().adapter;
        for (int j = 0; j < currentadapter.getCount(); j++) {
            notesArray.add(currentadapter.getItem(j));
        }
        return currentadapter;
    }

    public ArrayList<Note> readJobs() {
        db = FirebaseFirestore.getInstance();
        System.out.println("STARTING");


        ArrayList<Note> firestorenotes = new ArrayList<>();
        System.out.println("STARTING");
        db.collection("data")
                .document("jobs")
                .collection("alljobs").whereEqualTo("type", "Active")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                System.out.println("DOCS");
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    System.out.println(doc.getData());
                    Importance thisI = Importance.valueOf(String.valueOf(doc.getData().get("importance")));
                    String t = String.valueOf(doc.getData().get("title"));
                    String c = String.valueOf(doc.getData().get("content"));
                    String time = String.valueOf(doc.getData().get("timestamp"));
                    String type = String.valueOf(doc.getData().get("type"));
                    Note e = new Note(t, c, thisI, time);
                    e.type=type;
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

    public ArrayList<Note> readResolvedJobs() {
        db = FirebaseFirestore.getInstance();
        System.out.println("STARTING");


        ArrayList<Note> firestorenotes = new ArrayList<>();
        System.out.println("STARTING");
        db.collection("data")
                .document("jobs")
                .collection("alljobs").whereEqualTo("type", "Resolved")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        System.out.println("DOCS");
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            System.out.println(doc.getData());
                            Importance thisI = Importance.valueOf(String.valueOf(doc.getData().get("importance")));
                            String t = String.valueOf(doc.getData().get("title"));
                            String c = String.valueOf(doc.getData().get("content"));
                            String time = String.valueOf(doc.getData().get("timestamp"));
                            String type = String.valueOf(doc.getData().get("type"));
                            Note e = new Note(t, c, thisI, time);
                            e.type=type;
                            System.out.println("the note"+e);
                            firestorenotes.add(e);

                        }
                        resolvedjobs = firestorenotes;

                        ResolvedJobs.getInstance().resolvedjobs = resolvedjobs;
                        ResolvedJobs.getInstance().resolvedchanged(resolvedjobs);


                    }
                });

        System.out.println("STARTING");
        return firestorenotes;
    }

    private void alertDialog(Context context, String fault) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Fault Code")
                .setMessage("Create a job associated with this fault?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(JobsActivity.this, EditJobActivity.class);
                        i.putExtra("thisFault", fault);
                        i.putExtra("allNotes", notesArray);
                        i.putExtra("faults", faults);
                        i.putExtra("Gsfcs", Gsfcs);
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




    public void writeFaults(SharedPreferences sharedPref, SharedPreferences.Editor editor, ArrayList<String> faults) {
        String jsonNotes = g.toJson(faults);
        editor.putString("faults", jsonNotes);
        editor.apply();
    }

}