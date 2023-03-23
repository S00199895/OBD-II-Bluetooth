package edu.markc.bluetooth;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ResolvedJobs#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ResolvedJobs extends Fragment {
ArrayList<Note> resolvedjobs;
Gson g;
ListView lvResolved;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ResolvedJobs() {

        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ResolvedJobs.
     */
    // TODO: Rename and change types and number of parameters
    public static ResolvedJobs newInstance(String param1, String param2) {
        ResolvedJobs fragment = new ResolvedJobs();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    private  static ResolvedJobs instance = null;
    FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        instance = this;


    }

    public static ResolvedJobs getInstance() {
        return instance;
    }
    ArrayAdapter<Note> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        resolvedjobs = getResolvedJobs();
       /* Gson g = new Gson();

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        String jsonNotes = sharedPref.getString("Rjobs", null);
        if (jsonNotes != null) {
            Type type = new TypeToken<ArrayList<Note>>(){}.getType();
            resolvedjobs = g.fromJson(jsonNotes, type);
        }
        else
        {
            //empty array
            resolvedjobs = new ArrayList<Note>();
        }*/


        // Inflate the layout for this fragment
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
                date.setText("Resolved " + note.timestamp);
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
        View view = inflater.inflate(R.layout.fragment_resolved_jobs, container, false);

        lvResolved = view.findViewById(R.id.lVNotesResolved);
        if (resolvedjobs == null)
        {
            resolvedjobs = getResolvedJobs();
        }
        adapter = new noteArrayAdapter(getActivity(), 0, resolvedjobs);


        lvResolved.setAdapter(adapter);
        return  view;
    }

    private ArrayList<Note> getResolvedJobs() {
        db = FirebaseFirestore.getInstance();

        resolvedjobs = new ArrayList<>();

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
                            resolvedjobs.add(e);

                        }
                     //   resolvedjobs = thisresolvedjobs;
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

    public void resolvedchanged(ArrayList<Note> newresolved)
    {
        adapter.clear();
        adapter.addAll(newresolved);
        adapter.notifyDataSetChanged();
    }
}