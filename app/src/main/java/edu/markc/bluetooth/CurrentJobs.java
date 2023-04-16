package edu.markc.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CurrentJobs#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CurrentJobs extends Fragment implements JobsActivity.FragmentListener {
    Gson g = new Gson();
    ArrayList<Note> notes;
    ArrayList<String> faults;
    ArrayList<Note> resolvedjobs;
    FirebaseFirestore db;
    @Override
    public void updateFragmentList(ArrayList<Note> newnotes) {
        notes = newnotes;
        updateArray(notes);
    }

    @Override
    public void faults(ArrayList<String> faults) {
        this.faults = faults;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((JobsActivity) getActivity()).setFragmentListener(this);

    }

    class noteArrayAdapter extends ArrayAdapter<Note> {
        private Context context;
        private List<Note> notes;

        //constructor, call on creation
        public noteArrayAdapter(Context context, int resource, ArrayList<Note> objects) {
            super(context, resource, objects);

            this.context = context;
            this.notes = objects;


        }

        public void remove(int i)
        {
            notes.remove(i);
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
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CurrentJobs() {

    }

    public static CurrentJobs newInstance() {
        CurrentJobs fragment = new CurrentJobs();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    ListView lvNotes;
    private  static CurrentJobs instance = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        instance = this;

    }

    public static CurrentJobs getInstance() {
        return instance;
    }

    ArrayAdapter<Note> adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_current_jobs, container, false);

        lvNotes = view.findViewById(R.id.lVNotes);

        notes = readActiveJobs();

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        faults = readFaults(sharedPref);
        resolvedjobs = new ArrayList<>();
        resolvedjobs = resolvedFirestore();
if (notes == null)
{
    notes = new ArrayList<Note>();
}
         adapter = new noteArrayAdapter(getActivity(), 0, notes);
        lvNotes.setAdapter(adapter);
        lvNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note e = (Note) parent.getAdapter().getItem(position);
                //  System.out.println(e.toString());
writeFaults(sharedPref, editor, faults);
                Intent i = new Intent(getActivity(), EditJobActivity.class);
                notes = getInstance().notes;
                i.putExtra("allNotes", notes);
                i.putExtra("editNoteIndex", notes.indexOf(e));
                i.putExtra("faults", readFaults(sharedPref));
                startActivity(i);

            }});

        lvNotes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                dialogDeleteJob(getActivity(), notes, position, adapter);

                return true;
            }});

        return view;
    }

    private ArrayList<Note> readActiveJobs() {
        db = FirebaseFirestore.getInstance();


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
                            notes.add(e);

                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println(e.toString());
                    }
                });

        System.out.println("STARTING");
        return notes;
    }


    public void writeFaults(SharedPreferences sharedPref, SharedPreferences.Editor editor, ArrayList<String> faults) {
        String jsonNotes = g.toJson(faults);
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

    private void dialogDeleteJob(Context context, ArrayList<Note> finalNotesArray2, int position,/*index of the deleted?*/ArrayAdapter<Note> adapter) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete job?")
                .setMessage("Tap Yes to delete the job or Resolve to view it later")
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        Toast.makeText(getActivity(), "Job deleted", Toast.LENGTH_SHORT).show();
                        adapter.remove(adapter.getItem(position));
                        adapter.notifyDataSetChanged();
                    }
                }).setNeutralButton("Resolve", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                       // finalNotesArray2 = adapter.getItem(position)
                        Note resolved = adapter.getItem(position);
                        resolved.content = "Resolved";
                        LocalDateTime myDateObj = LocalDateTime.now();
                        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

                        resolved.timestamp = myDateObj.format(myFormatObj);
                        resolved.type = "Resolved";

                        resolvedjobs.add(resolved);
                        adapter.remove(adapter.getItem(position));
                        notifyResolved(resolvedjobs);


                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void notifyResolved(ArrayList<Note> resolvedjobs) {
        resolvedjobs = resolvedFirestore();

        ResolvedJobs.getInstance().resolvedchanged(resolvedjobs);
    }

    private ArrayList<Note> resolvedFirestore() {
        db = FirebaseFirestore.getInstance();

        ArrayList<Note> thisresolvedjobs = new ArrayList<>();

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
                            e.type = type;
                            System.out.println("the note" + e);
                            thisresolvedjobs.add(e);

                        }
                        resolvedjobs = thisresolvedjobs;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("DIDNT WORK " +e.toString());
                    }
                });
        System.out.println(resolvedjobs);

        CollectionReference jobs = db.collection("data")
                .document("jobs")
                .collection("alljobs");

        for (Note e :
                resolvedjobs) {
            jobs.document(e.title).set(e);
        }

       return resolvedjobs;
    }


    public void updateArray(ArrayList<Note> newnotes)
    {

        adapter.clear();
adapter.addAll(newnotes);
adapter.notifyDataSetChanged();
    }
}