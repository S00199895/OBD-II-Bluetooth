package edu.markc.bluetooth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
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
import java.util.concurrent.LinkedBlockingQueue;

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
    TextView warning;
    TextView title;
    LinkedBlockingQueue<SFC> Gsfcs;
    ArrayList<String> faults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        getSupportActionBar().hide();

        Gsfcs = new LinkedBlockingQueue<>(7);
        faults = new ArrayList<String>();
        avg = findViewById(R.id.avgtv);
        title = findViewById(R.id.graphtitle);
        timetv = findViewById(R.id.totaltimetv);
        warning = findViewById(R.id.warning);
        spinner = (Spinner) findViewById(R.id.spinnerReading);
        day = (RadioButton) findViewById(R.id.dayRB);
        week = (RadioButton) findViewById(R.id.weekRB);
        month = (RadioButton) findViewById(R.id.MonthRB);

        rG = (RadioGroup) findViewById(R.id.radioGroup);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.readings_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);
        Toolbar toolbar = findViewById(R.id.navbar);
        if (getIntent().getExtras() != null)
        {
            Gsfcs = (LinkedBlockingQueue<SFC>) getIntent().getSerializableExtra("Gsfcs");

            faults = (ArrayList<String>) getIntent().getSerializableExtra("faults");
        }


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

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                System.out.println(item.getTitle().toString());
                item.getTitle().toString();
                Intent i;
                switch (item.getTitle().toString()) {
                    case "HOME":
                        i = new Intent(StatsActivity.this, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        i.putExtra("Gsfcs", Gsfcs);
                        i.putExtra("faults", faults);
                        startActivity(i);
                        return true;
                    case "stats":

                        return true;
                    case "fuel":
                        i = new Intent(StatsActivity.this, FuelActivity.class);

                        i.putExtra("Gsfcs", Gsfcs);
                        i.putExtra("faults", faults);
                        startActivity(i);
                        return true;
                    case "jobs":
                        i = new Intent(StatsActivity.this, JobsActivity.class);

                        i.putExtra("faults", faults);

                        i.putExtra("Gsfcs", Gsfcs);
                        i.putExtra("faults", faults);
                        startActivity(i);
                        return true;
                    default:
                        return false;

                }
            }
        });
    }

    private void highReadings( ArrayList<Entry> yValues) {


        ArrayList<Float> values = new ArrayList<>();

        for (Entry e:
             yValues)
        {
            if (spinner.getSelectedItem().toString().equals("Speed"))
            {
                if (e.getY() > 120)
                    values.add(e.getY());
            }
            else if (spinner.getSelectedItem().toString().equals("RPM"))
            {
                if (e.getY() > 1500)
                    values.add(e.getY());
            }}
        if (values.size() >= (int) yValues.size() / 2)
        {
            //text
            if (spinner.getSelectedItem().toString().equals("RPM"))
            {
                warning.setText("WARNING! Your RPM recorded this " + checkRadio() + " is very high. Lower RPM can mean lower fuel costs and engine health." );

            }
            else if (spinner.getSelectedItem().toString().equals("Speed"))
            {
                warning.setText("WARNING! Your Speed recorded this " + checkRadio() + " is very high. Reduce your speed to be more aware of your surroundings and to save on fuel");

            }
        }
        else
        {
            if (spinner.getSelectedItem().toString().equals("RPM"))
            {
                warning.setText("Your RPM recorded this " + checkRadio() + " is healthy. Good job!" );

            }
            else if (spinner.getSelectedItem().toString().equals("Speed"))
            {
                warning.setText("WARNING! Your Speed recorded this " + checkRadio() + " is safe.");

            }
        }
    }

    private void makeLineChart(ArrayList<Map<String, Object>> chartData) {

        String spinnerValue = spinner.getSelectedItem().toString();
        //check button selected
        String interval = checkRadio();
    //do spinner
        LimitLine ll = new LimitLine(1f);
        if (spinnerValue.contains("RPM"))
        {
            ll = new LimitLine(1700f, "High RPM");
        }
        else if (spinnerValue.contains("Speed"))
        {
            ll = new LimitLine(120f, "High Speed");
        }

        ll.setLineWidth(2f);
        ll.setLineColor(Color.RED);



        System.out.println("chart valeus");
        System.out.println(chartData);
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
YAxis y = mChart.getAxis(YAxis.AxisDependency.LEFT);

y.addLimitLine(ll);
        x.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                String val = String.valueOf(value);
                if (labels.size()>0){
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


                }}
                return "";
            }
        });

        System.out.println("LABLESARE"+labels);
        simulateTap(mChart);

        title.setText(spinner.getSelectedItem().toString() +" readings for this " + checkRadio());
        getStats(chartData);
highReadings(yValues);
    }
    private void getStats(ArrayList<Map<String, Object>> chartData) {
        float average = 0;
        String type = checkRadio();
        for (Map<String, Object> obj : chartData)
        {
            average += Integer.valueOf(obj.get("value").toString());
        }

        average = average / chartData.size();

        avg.setText("Average:\n " + String.valueOf(average) + " " + spinner.getSelectedItem().toString());

        //total time
        //get the first and the last and subtract them to get hours and minutes
        Date first = ((Timestamp)chartData.get(0).get("datetime")).toDate();
        Date last = ((Timestamp)chartData.get(chartData.size()-1).get("datetime")).toDate();

        long diff = last.getTime() -  first.getTime();

        long hours = diff / (60 * 60 * 1000);
        long minutes = (diff / (60 * 1000)) % 60;
        timetv.setText("Total time:\n "+String.valueOf(hours) +" hour(s) and "+String.valueOf(minutes) + " minutes");
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
        ArrayList<Map<String, Object>>[] readMaps = new ArrayList[]{new ArrayList<>()};

        db = FirebaseFirestore.getInstance();

        String interval = checkRadio();
        if (interval.contains("Day")) {
            db.collection("data").document(String.valueOf(LocalDate.now())).collection(type)
                    .whereEqualTo("type", type).orderBy("datetime", Query.Direction.ASCENDING)
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                System.out.println("STATS WORKLS "+doc.getData());
                                readMaps[0].add(doc.getData());
                            }
                            if (readMaps[0].size() > 0 )
                                makeLineChart(readMaps[0]);
                            else
                                Toast.makeText(StatsActivity.this, "No data for today to show", Toast.LENGTH_SHORT);
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