package edu.markc.bluetooth;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.AvailablePidsCommand_21_40;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import br.ufrn.imd.obd.commands.ObdCommandGroup;
import br.ufrn.imd.obd.commands.control.TroubleCodesCommand;
import br.ufrn.imd.obd.utils.TroubleCodeDescription;

public class MainActivity extends AppCompatActivity implements Serializable {
    private final static int REQUEST_ENABLE_BT = 1;
    private static final UUID CONNUUID = UUID.fromString("ea3836df-b860-4f33-b338-4e032c124870");
    TextView tVRPM;
    TextView tVSpeed;
    FirebaseFirestore db;
    LineChart mChart;
    RadioGroup rG;
    RadioButton selected;
    Spinner selectSpinner;

    RadioButton day;
    RadioButton week;
    RadioButton month;
    Gson g = new Gson();

    Button btn;
    Button btnStats;

    ArrayList<String> faults = new ArrayList<>();

    private  static  LocalDate localdate;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };
    private static String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkPermissions();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirestoreService.read("RPM", "Month");
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        tVRPM = findViewById(R.id.tVRPM);
        tVSpeed = findViewById(R.id.tVSpeed);
        rG = (RadioGroup) findViewById(R.id.radioGroup);

        btn = (Button) findViewById(R.id.buttonre);
        btnStats = (Button) findViewById(R.id.btnStats);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, JobsActivity.class);
                //putextra the faults
                i.putExtra("faults", faults);
                startActivity(i);
            }
        });

        btnStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, StatsActivity.class);
                //good idea to pass the current spinner value in future
                startActivity(i);
            }
        });
        selectSpinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.readings_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        selectSpinner.setAdapter(adapter);

        day = (RadioButton) findViewById(R.id.dayRB);
        week = (RadioButton) findViewById(R.id.weekRB);
        month = (RadioButton) findViewById(R.id.MonthRB);

        day.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                read(selectSpinner.getSelectedItem().toString());
            }
        });
        week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                read(selectSpinner.getSelectedItem().toString());
            }
        });
        month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                read(selectSpinner.getSelectedItem().toString());
            }
        });
        ArrayList<Map<String, Object>> reads = read(selectSpinner.getSelectedItem().toString());
        BluetoothSocket btSocket = connectToOBD();
//        ArrayList<Integer> testrpm = new ArrayList<>();
//        testrpm.add(20);
//        testrpm.add(30);
//        testrpm.add(40);
//        testrpm.add(60);
//        testrpm.add(20);testrpm.add(20);
//
//       ArrayList<Map<String, Object>> objsToPush = formatResults(testrpm);
//
//        addToFirestore(objsToPush);

       // ArrayList<Map<String, Object>> wantedReadings = read();
         if (btSocket.isConnected()) {
             Toast.makeText(MainActivity.this,
                     "Connected to " + btSocket, Toast.LENGTH_LONG).show();

             OutputStream outputStream = null;
             InputStream inputStream = null;

             try{
              outputStream = btSocket.getOutputStream();
         } catch (IOException e) {
                 e.printStackTrace();
             }

             try{
                  inputStream = btSocket.getInputStream();
             } catch (IOException e) {
                 e.printStackTrace();
             }
             try {
                // final Handler handler = new Handler();

               //  final int delay = 5000;

                 InputStream finalInputStream = inputStream;
                 OutputStream finalOutputStream = outputStream;

                 faults= getfaults(finalInputStream, finalOutputStream);
                 writePrefs(sharedPref, editor, faults);
                 ArrayList<Map<String, Object>> rpmList = new ArrayList<>();

                 ArrayList<Map<String, Object>> speedList = new ArrayList<>();

                Executor executor = Executors.newFixedThreadPool(2);
                 //handler.postDelayed
                 //rpm thread
                 executor.execute(new Runnable() {
                     @Override
                     public void run() {
                        // String s = selectSpinner.getSelectedItem().toString();

                       //  if (s.contains("RPM")) {
                             //work

                             try {
                                 while (btSocket.isConnected()) {
                                     Map<String, Object> thisDoc = new HashMap<>();

                                     localdate = LocalDate.now();
                                     //here
                                     //make it check that RPM is the one seelcted from the dropdown - new method

                                     int rpm = getLiveRPM(finalInputStream, finalOutputStream);

                                     //this line needs to be general for variables
                                     thisDoc.put("type", "RPM");
                                     thisDoc.put("value", rpm);
                                     thisDoc.put("datetime", Timestamp.now());
                                     rpmList.add(thisDoc);
                                     tVRPM.setText(String.valueOf(rpm));

                                     try {
                                         Thread.sleep(5000);
                                     } catch (InterruptedException e) {
                                         e.printStackTrace();
                                     }
                                     //work
                                     // handler.postDelayed(this, delay);
                                     }
                                 } catch (NullPointerException e) {
                                      addToFirestore(rpmList);
                                      read(selectSpinner.getSelectedItem().toString());
                                 }
                                 //try catch for delay

                             /*else {*/
                            // addToFirestore(rpmList);
                           //  read(selectSpinner.getSelectedItem().toString());
                         }
                   //  }
                     //}


                 });
                 executor.execute(new Runnable() {
                     //speed thread
                     @Override
                     public void run() {
                         //String s = selectSpinner.getSelectedItem().toString();

                         /*if (s.contains("Speed"))
                         {*/
                             //realistically all will be running at once
                             //multi-threading
                             //change to speed

                             //speed add firestore emthod
                             //work

                                 try {
                                     while (btSocket.isConnected()) {
                                         Map<String, Object> thisDoc = new HashMap<>();

                                         localdate = LocalDate.now();
                                         //here
                                         //make it check that RPM is the one seelcted from the dropdown - new method

                                         int speed = getLiveSpeed(finalInputStream, finalOutputStream);//getLiveRPM(finalInputStream, finalOutputStream);

                                         //this line needs to be general for variables
                                         thisDoc.put("type", "Speed");
                                         thisDoc.put("value", speed);
                                         thisDoc.put("datetime", Timestamp.now());
                                         speedList.add(thisDoc);
                                         tVSpeed.setText(String.valueOf(speed));
                                         try {
                                             Thread.sleep(5000);
                                         } catch (InterruptedException e) {
                                             e.printStackTrace();
                                         }
                                         //work
                                         //  handler.postDelayed(this, delay);
                                     }
                                 } catch (NullPointerException e) {
                                     //different method?
                                     //check within method extract type variable
                                     addToFirestore(speedList);
                                     read(selectSpinner.getSelectedItem().toString());
                                 }
                            //try and modularise it
                         //make part using getLiveX a method use variables
                         //get each thread to run variatiosn of it

                         //amke more methods
                         //different classes?
                         //firestore class?
                         //speed
                         //rpm classes?

                                // addToFirestore(rpmList);
                              //   read(selectSpinner.getSelectedItem().toString());

                         }
                     //}
                 });

               /* ObdRawCommand custom = new ObdRawCommand("00"); //command can be any ELM command like "atz" or "0130"
                custom.run(inputStream, outputStream); //inputs - input stream, outputs - output stream
                String result = custom.getResult(); // your variable with result*/
            }
            catch(Exception e){
e.printStackTrace();
            } }}

    private ArrayList<String> getfaults(InputStream finalInputStream, OutputStream finalOutputStream) {
        ObdCommandGroup commands = new ObdCommandGroup();

        commands.add(new TroubleCodesCommand());
        //commands.add(new TroubleCode);
        try {
            commands.run(finalInputStream, finalOutputStream);
            String r = commands.toString();


            ArrayList<String> dtcs = getDTCs(r);
            return  dtcs;

        }
        catch (IOException | InterruptedException e)
        {

        }
        return null;
    }

    private ArrayList<String> getDTCs(String r) {
        ArrayList<String> dtcs = new ArrayList<>();
        TroubleCodeDescription troubleCodeDescription = TroubleCodeDescription.getInstance(MainActivity.this);

        String[] parts = r.split("\\[|,|\\]");
        //get index 1 up to Length - 1

        parts = Arrays.copyOfRange(parts, 1, parts.length-1);

        for (String d:parts) {
            //d is the dtc
            String dtc = d /*+ ": "*/;
            String t = troubleCodeDescription.getTroubleCodeDescription(d);

            dtc += ": "+ t;
            dtcs.add(dtc);
        }

        return dtcs;
    }

    private int getLiveSpeed(InputStream finalInputStream, OutputStream finalOutputStream) {
        SpeedCommand spd = new SpeedCommand();
        try {
            spd.run(finalInputStream, finalOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String result = spd.getResult();
        String sub = result.substring(8);
        return Integer.parseInt(sub, 16);
    }

    public ArrayList<Note> readPrefs(SharedPreferences sharedPref) {
        String jsonNotes = sharedPref.getString("notes", null);
        if (jsonNotes == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Note>>(){}.getType();
        return g.fromJson(jsonNotes, type);
    }

    public void writePrefs(SharedPreferences sharedPref, SharedPreferences.Editor editor, ArrayList<String> notes) {
        String jsonNotes = g.toJson(notes);
        editor.putString("faults", jsonNotes);
        editor.apply();
    }

    //mecessary for refreshing graph
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



    private void checkPermissions(){
        int permission1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
        int permission2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    1
            );
        } else if (permission2 != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_LOCATION,
                    1
            );
        }
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
                                System.out.println(doc.getData());
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
    private ArrayList<Map<String, Object>> formatResults(ArrayList<Integer> rpmList) {
        //foreach
        //put into a json object array
        //include date
        //type
        //new interface?
        ArrayList<Map<String, Object>> arrayOfValues = new ArrayList<>();
        for (int value: rpmList)
        {
            //for firestore
            Map<String, Object> obj = new HashMap<>();

                obj.put("type", "RPM");
                obj.put("value", value);
               // obj.put("date", date.toString());

            arrayOfValues.add(obj);
        }
        return arrayOfValues;
        //addToFirestore(arrayOfValues, 1, date, "rpm");
    }

 private void addToFirestore(ArrayList<Map<String, Object>> objsToPush) {
        db = FirebaseFirestore.getInstance();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        for (Map<String, Object> oneDoc : objsToPush) {

            db.collection("data")
                    .document(String.valueOf(LocalDate.now()))
                    .collection(String.valueOf(oneDoc.get("type")))
                    .add(oneDoc);
        }
        Date dt = new Date();
        Map<String, Object> updates = new HashMap<>();
        updates.put("datedoc", dt);

        db.collection("data")
                .document(String.valueOf(LocalDate.now())).set(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Document update successful!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }


    private void makeLineChart(ArrayList<Map<String, Object>> chartData) {

        //check button selected
        checkRadio();
        
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
        d.setText(selectSpinner.getSelectedItem().toString() + " of this " + checkRadio());
        mChart = (LineChart) findViewById(R.id.lineChart);
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
        simulateTap(mChart);
    }

    private String checkRadio() {
        int selectedID = rG.getCheckedRadioButtonId();
        selected = (RadioButton) findViewById(selectedID);

        if (selectedID == -1) {
            Toast.makeText(MainActivity.this, "Please select a time period", Toast.LENGTH_SHORT).show();
        }
        if (selected != null)
            return selected.getText().toString();
        return "";
    }

        private BluetoothSocket connectToOBD()
    {
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        BluetoothDevice obd = bluetoothAdapter.getRemoteDevice("10:21:3E:48:A4:5B");
        String obdName = obd.getName();

        ParcelUuid[] uuidsR = null;

        try{
            Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);

            ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(bluetoothAdapter, null);
            uuidsR = uuids;
        }
        catch (Exception e)
        {
        }
        for (ParcelUuid uuid: uuidsR) {
            Log.d(TAG, "UUID: " + uuid.getUuid().toString());
        }
        BluetoothSocket btSocket = null;

        try {
            btSocket = obd.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            btSocket.connect();

        } catch (IOException e) {

            e.printStackTrace();
        }

        return btSocket;
    }

    private int getLiveRPM(InputStream finalInputStream, OutputStream finalOutputStream) {
        RPMCommand spd = new RPMCommand();
        try {
            spd.run(finalInputStream, finalOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String result = spd.getResult();
        String sub = result.substring(8);
        int rpm = Integer.parseInt(sub, 16)/4;

        return  rpm;
    }

    private void getRunTime(InputStream finalInputStream, OutputStream finalOutputStream) {
        AvailablePidsCommand_21_40 rTime = new AvailablePidsCommand_21_40();

        try {
            rTime.run(finalInputStream, finalOutputStream);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String result = rTime.getFormattedResult();
        Log.d(TAG, "getRunTime: " + result);
    }
}