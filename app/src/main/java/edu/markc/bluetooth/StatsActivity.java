package edu.markc.bluetooth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.protobuf.DescriptorProtos;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

public class StatsActivity extends AppCompatActivity {

    Spinner spinner;
    LineChart mChart;
    RadioButton day;
    RadioButton week;
    RadioButton month;
    RadioButton selected;
    RadioGroup rG;
    FirebaseFirestore db;
    TextView avg;
    TextView timetv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        avg = findViewById(R.id.avgtv);
        timetv = findViewById(R.id.totaltimetv);
        spinner = (Spinner) findViewById(R.id.spinnerReading);
        day = (RadioButton) findViewById(R.id.dayRB);
        week = (RadioButton) findViewById(R.id.weekRB);
        month = (RadioButton) findViewById(R.id.MonthRB);

        rG = (RadioGroup) findViewById(R.id.radioGroup);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.readings_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);


        day.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                read(spinner.getSelectedItem().toString());
            }
        });
        week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                read(spinner.getSelectedItem().toString());
            }
        });
        month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                read(spinner.getSelectedItem().toString());
            }
        });
    }

    private void makeLineChart(ArrayList<Map<String, Object>> chartData) {

        //check button selected
        String interval = checkRadio();

        System.out.println("chart valeus");
        System.out.println(chartData);
        //give this chart times for order and dates
        //plot against values
        //display chart

        ArrayList<Entry> yValues = new ArrayList<>();

        //sort
        chartData.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                Timestamp t1 = (Timestamp) o1.get("datetime");
                Timestamp t2 = (Timestamp) o2.get("datetime");

                Date d1 = t1.toDate();
                Date d2 = t2.toDate();

                return d1.compareTo(d2);
            }
        });

        int i = 0;
        for(Map<String, Object> oneDoc : chartData)
        {
            yValues.add(new Entry(i, Float.parseFloat(String.valueOf(oneDoc.get("value")))));
            i++;

        }
        System.out.println("yvalues");
        System.out.println(yValues);

        Description d = new Description();
        d.setText(spinner.getSelectedItem().toString() + " of this " + interval);
        mChart = (LineChart) findViewById(R.id.lineChartStats);
mChart.setExtraOffsets(0,0,20,0);


        XAxis x = mChart.getXAxis();
       String giosdroni =  x.getFormattedLabel(1000);
                mChart.setDescription(d);

        mChart.setBackgroundColor(Color.WHITE);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);


        LineDataSet set1 = new LineDataSet(yValues, "Data");
        set1.setFillAlpha(110);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        LineData data = new LineData(dataSets);

 mChart.setData(data);
 ArrayList<String> labels = new ArrayList<>();

        String thisLabel = "first";
        int k=0;
        while (thisLabel != "")
        {
            thisLabel = x.getFormattedLabel(k);
            if (thisLabel != "")
                labels.add(thisLabel);
            k++;
        }

    /*    Timestamp t2 = (Timestamp) o2.get("datetime");

        Date d1 = t1.toDate();*/
        SimpleDateFormat time = new SimpleDateFormat("HH:mm");
        SimpleDateFormat date = new SimpleDateFormat("dd-MM-yyyy");
        Timestamp first = (Timestamp)  chartData.get(0).get("datetime");
        Timestamp last = (Timestamp) chartData.get(chartData.size()-1).get("datetime");
        String Firstdate = time.format(first.toDate());
        String Lastdate = time.format(last.toDate());
        ArrayList<LabelIndex> labelIndexArr = new ArrayList<>();
        String current = date.format( ((Timestamp) chartData.get(0).get("datetime")).toDate());
        labelIndexArr.add(new LabelIndex(0, current));
        for (int j=1;j<chartData.size(); j++ )
        {
            String thisTimestamp = date.format( ((Timestamp) chartData.get(j).get("datetime")).toDate());

            if (!thisTimestamp.equals(current))
            {
                LabelIndex li = new LabelIndex(j, thisTimestamp);
                if (li.index % 2 != 0 || li.index - Math.floor(li.index) == .5)
                li.index = Math.round(li.index) + 1;
                labelIndexArr.add(li);
                current = thisTimestamp;
            }

        }
        System.out.println("done" + labelIndexArr);

        x.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                //day case WORKS
                //make returns the start and end date
                //week/month case
                //use dates(???)
                String val = String.valueOf(value);
                switch (interval) {
                    case "Day":
                    if (val.equals(labels.get(0)))
                        return Firstdate;
                    else if (val.equals(labels.get(labels.size() - 1)))
                        return Lastdate;
                    case "Week":
                        for (LabelIndex li:labelIndexArr
                             ) {
                            if (val.equals(String.valueOf(li.index)))
                            {
                                return li.Label;
                            }
                        }

                    case "Month":
                        for (LabelIndex li:labelIndexArr
                        ) {
                            if (val.equals(String.valueOf(li.index)))
                            {
                                return li.Label;
                            }
                        }


                }
                return "";
            }
        });

        System.out.println("LABLESARE"+labels);
//yValues.get
        //invalidate
        simulateTap(mChart);

        getStats(chartData);
    }

    private void getStats(ArrayList<Map<String, Object>> chartData) {
        //average
        //sum up all the values in a loop divide by size

        float average = 0;
        String type = checkRadio();
        for (Map<String, Object> obj : chartData)
        {
            average += Integer.valueOf(obj.get("value").toString());
        }

        average = average / chartData.size();

        avg.setText(String.valueOf(average) + " " + spinner.getSelectedItem().toString());

        //total time
        //get the first and the last and subtract them to get hours and minutes
        Date first = ((Timestamp)chartData.get(0).get("datetime")).toDate();
        Date last = ((Timestamp)chartData.get(chartData.size()-1).get("datetime")).toDate();

        long diff = last.getTime() -  first.getTime();

        long hours = diff / (60 * 60 * 1000);
        long minutes = (diff / (60 * 1000)) % 60;
//JUST FOR DAY??
        timetv.setText(String.valueOf(hours) +" hour(s) and "+String.valueOf(minutes) + " minutes");
    }

    private void CheckWeekChart(float value, ArrayList<Map<String, Object>> chartData) {
        //
    }

    private String checkRadio() {
        int selectedID = rG.getCheckedRadioButtonId();
        selected = (RadioButton) findViewById(selectedID);

        if (selectedID == -1) {
            Toast.makeText(StatsActivity.this, "Please select a time period", Toast.LENGTH_SHORT).show();
        }
        if (selected != null)
            return selected.getText().toString();
        return "";
    }

    public void simulateTap(View view) {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;

        float x = view.getWidth() / 2.0f;
        float y = view.getHeight() / 2.0f;

        int metaState = 0;
        MotionEvent motionEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_UP,
                x,
                y,
                metaState
        );

        view.dispatchTouchEvent(motionEvent);
    }

    private ArrayList<Map<String, Object>> read(String type) {
        //read and query where type is rpm
        ArrayList<Map<String, Object>>[] readMaps = new ArrayList[]{new ArrayList<>()};

        db = FirebaseFirestore.getInstance();

        String interval = checkRadio();
        //interval = "Week";
        if (interval.contains("Day")) {

            //change these RPMs to a variable
            //has to be a parameter
            db.collection("data").document(String.valueOf(LocalDate.now())).collection(type)
                    .whereEqualTo("type", type).orderBy("datetime", Query.Direction.ASCENDING)
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                System.out.println("STATS WORKLS "+doc.getData());
                                readMaps[0].add(doc.getData());
                            }
                            makeLineChart(readMaps[0]);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println("printing "+e.toString());
                        }
                    });
        }
        else if (interval.contains("Week") )
        {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
            ZoneId zid = ZoneId.of("UTC");
            ZonedDateTime zonedDateTime = lastWeek.atZone(zid);
            Instant i = zonedDateTime.toInstant();
            date = Date.from(i);
            db.collection("data").whereGreaterThanOrEqualTo("datedoc", date)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    System.out.println("printing the " + document);
                                }
                            }
                        }
                    }).addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            ArrayList<Map<String, Object>> weekMaps = new ArrayList<Map<String, Object>>();
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                //gets the 2023-01-20 document
                                System.out.println("printing the " + document);

                                //then get rpm colelction within
                                document.getReference().collection(type).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        System.out.println(queryDocumentSnapshots);
                                        //queryDocumentSnapshots.getDocuments();
                                        for (DocumentSnapshot d : queryDocumentSnapshots.getDocuments())
                                        {
                                            String test = d.getData().toString();
                                            weekMaps.add(d.getData());
                                        }
                                        readMaps[0] = weekMaps;
                                        makeLineChart(readMaps[0]);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        String et = e.toString();
                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String t = e.toString();
                        }
                    });
        }
        else if (interval.contains("Month"))
        {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            LocalDateTime lastWeek = LocalDateTime.now().minusMonths(1);
            ZoneId zid = ZoneId.of("UTC");
            ZonedDateTime zonedDateTime = lastWeek.atZone(zid);
            Instant i = zonedDateTime.toInstant();
            date = Date.from(i);
//change to ascending
            //19012023
            db.collection("data").whereGreaterThanOrEqualTo("datedoc", date)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    System.out.println("printing the " + document);
                                    //apparently this works
                                }
                            }
                        }
                    }).addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            ArrayList<Map<String, Object>> weekMaps = new ArrayList<Map<String, Object>>();
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                //gets the 2023-01-20 document
                                System.out.println("printing the " + document);

                                //then get rpm colelction within
                                document.getReference().collection(type).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        System.out.println(queryDocumentSnapshots);
                                        //queryDocumentSnapshots.getDocuments();
                                        for (DocumentSnapshot d : queryDocumentSnapshots.getDocuments())
                                        {
                                            String test = d.getData().toString();
                                            weekMaps.add(d.getData());
                                        }
                                        readMaps[0] = weekMaps;
                                        makeLineChart(readMaps[0]);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        String et = e.toString();
                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String t = e.toString();
                        }
                    });
        }



        return readMaps[0];
    }
}