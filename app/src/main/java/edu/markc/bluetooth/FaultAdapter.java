package edu.markc.bluetooth;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FaultAdapter extends ArrayAdapter<String> {
    Context context;
    List<String> faults;
    static String descOut;
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

                }
            }
        });

        title.setText(f);
/*
        title.setText(f.title);
        date.setText(f.timestamp);
        severity.setText(note.importance.toString());
        //  note.importance = Importance.HIGH;
        if (note != null) {
            severity.setText(note.importance.toString());

            if (severity.getText().toString() == "High")
                severity.setBackgroundColor(Color.parseColor("#FF0000"));
            else if (severity.getText().toString() == "Medium")
                severity.setBackgroundColor(Color.parseColor("#FFA500"));
            else if (severity.getText().toString() == "Low")
                severity.setBackgroundColor(Color.parseColor("#257317"));
        }*/
        return  view;
    }

}
