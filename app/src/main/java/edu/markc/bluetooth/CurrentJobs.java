package edu.markc.bluetooth;

import static edu.markc.bluetooth.EmuService.editor;
import static edu.markc.bluetooth.EmuService.sharedPref;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.reflect.TypeToken;
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

        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CurrentJobs.
     */
    // TODO: Rename and change types and number of parameters
    public static CurrentJobs newInstance() {
        CurrentJobs fragment = new CurrentJobs();
        Bundle args = new Bundle();
      /*  args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);*/
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_current_jobs, container, false);

        lvNotes = view.findViewById(R.id.lVNotes);


        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        faults = readFaults(sharedPref);
        resolvedjobs = new ArrayList<>();
        if (readResolved(sharedPref) != null)
        {
            resolvedjobs = readResolved(sharedPref);
        }
        ArrayList<Note> notes = readPrefs(sharedPref);

         adapter = new noteArrayAdapter(getActivity(), 0, notes);
        lvNotes.setAdapter(adapter);
        lvNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note e = (Note) parent.getAdapter().getItem(position);
                //  System.out.println(e.toString());
writeFaults(sharedPref, editor, faults);
                Intent i = new Intent(getActivity(), EditJobActivity.class);
                i.putExtra("allNotes", notes);
                i.putExtra("editNoteIndex", notes.indexOf(e));
                i.putExtra("faults", readFaults(sharedPref));
                startActivity(i);
//startActivity(i);
            }});

        lvNotes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // TextView tVTitle =  view.findViewById(R.id.noteTitle);
                dialogDeleteJob(getActivity(), notes, position, adapter);

               /*Toast.makeText(JobsActivity.this, "Job deleted", Toast.LENGTH_SHORT).show();
                finalNotesArray2.remove(position);
                adapter.notifyDataSetChanged();
                writePrefs(sharedPref, editor, finalNotesArray2);*/
                return true;
            }});

        return view;
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

    private void dialogDeleteJob(Context context, ArrayList<Note> finalNotesArray2, int position,/*index of the deleted?*/ArrayAdapter<Note> adapter) {
        //find the code where it deletes
        //put in here
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete job?")
                .setMessage("Tap Yes to delete the job or Resolve to view it later")
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        Toast.makeText(getActivity(), "Job deleted", Toast.LENGTH_SHORT).show();
                        finalNotesArray2.remove(position);
                        adapter.notifyDataSetChanged();
                        writePrefs(sharedPref, editor, finalNotesArray2);

                    }
                }).setNeutralButton("Resolve", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        Note resolved = finalNotesArray2.get(position);
                        resolved.content = "Resolved";
                        LocalDateTime myDateObj = LocalDateTime.now();
                        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

                        resolved.timestamp = myDateObj.format(myFormatObj);
                        resolvedjobs.add(resolved);
                        finalNotesArray2.remove(position);
                        notifyResolved(resolvedjobs);
                        writePrefs(sharedPref, editor, finalNotesArray2);
                        writeResolved(sharedPref, editor, resolvedjobs);


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

    private void notifyResolved(ArrayList<Note> resolvedjobs) {
        ResolvedJobs.getInstance().resolvedchanged(resolvedjobs);
    }

    public void writePrefs(SharedPreferences sharedPref, SharedPreferences.Editor editor, ArrayList<Note> notes) {
        String jsonNotes = g.toJson(notes);
        // sendDataToFragment(notes);
        editor.putString("notes", jsonNotes);
        editor.apply();
    }

    public void writeResolved(SharedPreferences sharedPref, SharedPreferences.Editor editor, ArrayList<Note> notes) {
        String jsonNotes = g.toJson(notes);
        // sendDataToFragment(notes);
        editor.putString("Rjobs", jsonNotes);
        editor.apply();
        adapter.notifyDataSetChanged();
        //other adapter changed
    }

    public ArrayList<Note> readPrefs(SharedPreferences sharedPref) {
        String jsonNotes = sharedPref.getString("notes", null);
        if (jsonNotes == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Note>>(){}.getType();
        return g.fromJson(jsonNotes, type);
    }

    public ArrayList<Note> readResolved(SharedPreferences sharedPref) {
        String jsonNotes = sharedPref.getString("Rjobs", null);
        if (jsonNotes == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Note>>(){}.getType();
        return g.fromJson(jsonNotes, type);
    }

    public void updateArray(ArrayList<Note> newnotes)
    {
      //  adapter = new noteArrayAdapter(getActivity(), 0, newnotes);

        adapter.clear();
adapter.addAll(newnotes);
adapter.notifyDataSetChanged();
    }


    //TODO: put resolve logic in here too
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
                        Intent i = new Intent(getActivity(), EditJobActivity.class);
                        i.putExtra("thisFault", fault);
                        i.putExtra("allNotes", notes);
                   //     i.putExtra("faults", faults);
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
}