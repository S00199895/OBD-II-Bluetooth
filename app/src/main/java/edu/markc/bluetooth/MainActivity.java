package edu.markc.bluetooth;
import static android.content.ContentValues.TAG;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
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
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements Serializable {
    //emu var
    static boolean emu = true;
    static boolean stop = false;
    static float fuelLevel = -1;

    private final static int REQUEST_ENABLE_BT = 1;
    private static final UUID CONNUUID = UUID.fromString("ea3836df-b860-4f33-b338-4e032c124870");
    TextView tVRPM;
    TextView tVSpeed;
    TextView tvfuel;
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
    Button btnEmu;
    ArrayList<String> faults = new ArrayList<>();
    ImageView speedlimitImage;
    //EmuService emuService;

    private  static  LocalDate localdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BluetoothService.checkPermissions(MainActivity.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

speedLimits();
        if (emu == true)
        {
            fuelLevel = EmuService.getFuel(MainActivity.this);
            EmuService.writeFuel(MainActivity.this, (int)fuelLevel);
            timer();
        }

        tVRPM = findViewById(R.id.tVRPM);
        tVSpeed = findViewById(R.id.tVSpeed);
        tvfuel = findViewById(R.id.tvfuel);
        rG = (RadioGroup) findViewById(R.id.radioGroup);

        btn = (Button) findViewById(R.id.buttonre);
        btnStats = (Button) findViewById(R.id.btnStats);
        btnEmu = (Button) findViewById(R.id.btnEmuStopReading);

        btnEmu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop = true;
            }
        });
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

         if (btSocket.isConnected() || emu == true) {
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

                 InputStream finalInputStream = inputStream;
                 OutputStream finalOutputStream = outputStream;

                 faults= getfaults(finalInputStream, finalOutputStream);
                 writePrefs(sharedPref, editor, faults);
                 ArrayList<Map<String, Object>> rpmList = new ArrayList<>();

                 ArrayList<Map<String, Object>> speedList = new ArrayList<>();

                Executor executor = Executors.newFixedThreadPool(2);
                 //rpm thread
                 executor.execute(new Runnable() {
                     @Override
                     public void run() {
                             //work
                        //may try mock btsocket
                         //how?

                             try {
                                 while (btSocket.isConnected() || emu == true) {
                                     Map<String, Object> thisDoc = new HashMap<>();

                                     localdate = LocalDate.now();

                                     int rpm = getLiveRPM(finalInputStream, finalOutputStream);
                                     if (stop == true)
                                     {throw new NullPointerException();}

                                     //this line needs to be general for variables
                                     Timestamp now = Timestamp.now();

                                         thisDoc.put("type", "RPM");
                                         thisDoc.put("value", rpm);
                                         thisDoc.put("datetime", now);
                                         rpmList.add(thisDoc);

                                     tVRPM.setText(String.valueOf(rpm));

                                     try {
                                         Thread.sleep(5000);
                                     } catch (InterruptedException e) {
                                         e.printStackTrace();
                                     }
                                     }
                                 } catch (NullPointerException e) {
                                      addToFirestore(rpmList);
                                      read(selectSpinner.getSelectedItem().toString());
                                 }
                         }
                 });
                 executor.execute(new Runnable() {
                     //speed thread
                     @Override
                     public void run() {
                                 try {
                                     while (btSocket.isConnected() || emu == true) {
                                         Map<String, Object> thisDoc = new HashMap<>();

                                         localdate = LocalDate.now();

                                         int speed = getLiveSpeed(finalInputStream, finalOutputStream);
                                         if (stop == true)
                                         {throw new NullPointerException();}
                                         //this line needs to be general for variables
                                         if (speed != 0) {
                                             thisDoc.put("type", "Speed");
                                             thisDoc.put("value", speed);
                                             thisDoc.put("datetime", Timestamp.now());
                                             speedList.add(thisDoc);
                                         }
                                         tVSpeed.setText(String.valueOf(speed));
                                         try {
                                             Thread.sleep(5000);
                                         } catch (InterruptedException e) {
                                             e.printStackTrace();
                                         }
                                     }
                                 } catch (NullPointerException e) {
                                     addToFirestore(speedList);
                                     read(selectSpinner.getSelectedItem().toString());
                                 }
                         }
                 });
            }
            catch(Exception e){
e.printStackTrace();
            } }}

    private void gauge(float fuelpc) {
        if (fuelpc < 0)
            fuelpc=0;
        ConstraintSet set = new ConstraintSet();
        TextView needle = (TextView)findViewById(R.id.needle);
        ConstraintLayout constraintLayout = (ConstraintLayout)findViewById(R.id.clayout);
        set.clone(constraintLayout);
        set.setHorizontalBias(R.id.needle,fuelpc);
        set.applyTo(constraintLayout);
    }

    private ArrayList<String> getfaults(InputStream finalInputStream, OutputStream finalOutputStream) {
        if (emu == false)
        {
            return OBDService.getfaults(finalInputStream, finalOutputStream, MainActivity.this);
        }
        else
        {
            //EMU
            return EmuService.getFaults();
        }

    }

    private int getLiveSpeed(InputStream finalInputStream, OutputStream finalOutputStream) {
        if (emu == false)
        {
            return OBDService.getLiveSpeed(finalInputStream, finalOutputStream);
        }
        else
        {
            //EMU
            return EmuService.getSpeed();
        }
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
    private ArrayList<Map<String, Object>> read(String type) {
        //read and query where type is rpm
         ArrayList<Map<String, Object>>[] readMaps = new ArrayList[]{new ArrayList<>()};

        db = FirebaseFirestore.getInstance();

        String interval = checkRadio();
        //interval = "Week";
        if (interval.contains("Day")) {
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

 private void addToFirestore(ArrayList<Map<String, Object>> objsToPush) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
     StackTraceElement callingMethod = stackTrace[2]; // Index 0 is getStackTrace, index 1 is myMethod, index 2 is the caller
     int lineNumber = callingMethod.getLineNumber();
     System.out.println("myMethod was called from line " + lineNumber);

     //ArrayList<Map<String, Object>> unique = removeDuplicates(objsToPush);
   //  objsToPush = unique;
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


    private void speedLimits() {

        speedlimitImage = findViewById(R.id.imageView);
        TypedArray imgs = getResources().obtainTypedArray(R.array.speedimgs);
        int[] speeds = new int[] {50,60,80,100,120};
         Runnable ru = new Runnable() {
            @Override
            public void run() {
               Random r = new Random();
                int speedIndex = r.nextInt(5);

                speedlimitImage.setImageResource(imgs.getResourceId(speedIndex,0));

                int speed = Integer.parseInt(tVSpeed.getText().toString());

                if (EmuService.over(imgs, speed))
                {
                    overTheLimit();
                }

                handler.postDelayed(this, 10000);
            }
        };

        Handler h = new Handler();
        handler.postDelayed(ru, 10000);


        /*
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        },1000);*/

    }

    private void overTheLimit() {
            Vibrator v = (Vibrator) MainActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
            Toast.makeText(MainActivity.this, "SLOW DOWN!", Toast.LENGTH_LONG).show();
            v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));


    }
//
//    private ArrayList<Map<String, Object>> removeDuplicates(ArrayList<Map<String, Object>> objsToPush) {
//        ArrayList<Map<String, Object>> unique = new ArrayList<>();
//        for (Map<String, Object> doc:objsToPush) {
//            Timestamp thisTimestamp = (Timestamp) doc.get("datetime");
//
//            if (unique.stream().anyMatch(o -> o.get("datetime") == thisTimestamp))
//            {
//
//            }
//            else
//            {
//                unique.add(doc);
//            }
//        }
//        return unique;
//    }

    private void makeLineChart(ArrayList<Map<String, Object>> chartData) {

        //check button selected
        checkRadio();
        
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

            MainActivity.super.runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Please select a time period", Toast.LENGTH_SHORT).show();

            });
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

        if (emu == false)
        {
            return OBDService.getLiveRPM(finalInputStream, finalOutputStream);
        }
        else
        {
            //EMU
          //  emuService.updateFuel(MainActivity.this);
           // tvfuel.setText(String.valueOf(EmuService.getFuel(MainActivity.this)));
            return EmuService.getRPM();
        }
    }
    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;
    Handler handler = new Handler();
    int Seconds, Minutes, MilliSeconds ;

    private void timer() {
        //global variable fuel
        //return a stopwatch object from another helper class?
        //check under the reading if its been x minutes
        //-= the global var
        StartTime = SystemClock.uptimeMillis();
        handler.postDelayed(runnable, 0);

        //reset.setEnabled(false);


    }

    public Runnable runnable = new Runnable() {

        public void run() {

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);

            Minutes = Seconds / 60;

            Seconds = Seconds % 60;

            MilliSeconds = (int) (UpdateTime % 1000);

            String timer = ("" + Minutes + ":"
                    + String.format("%02d", Seconds));

            if (Seconds % 5 == 0 && MilliSeconds < 5)
            {
                fuelLevel -= 1;
                EmuService.writeFuel(MainActivity.this, (int)fuelLevel);
                tvfuel.setText(String.valueOf(fuelLevel));
                gauge(fuelLevel / 45f);
            }

            handler.postDelayed(this, 0);
        }

    };


}