package edu.markc.bluetooth;
import static android.content.ContentValues.TAG;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.app.Dialog;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements Serializable {
    //emu var
    static boolean emu = true;
    public static boolean stop = false;
    static float fuelLevel = -1;

    private final static int REQUEST_ENABLE_BT = 1;
    private static final UUID CONNUUID = UUID.fromString("ea3836df-b860-4f33-b338-4e032c124870");
    TextView tVRPM;
    TextView tVSpeed;
    TextView tvfuel;
   // TextView uptime;
    Dialog tut;
    FirebaseFirestore db;
    double dist;
    ViewFlipper vF;
    double fuelConsumed;
    Gson g = new Gson();
    int Gspeed;
  //  Button btn;
   // Button btnFuel;
  //  Button btnStats;
    Button btnEmu;
    ArrayList<String> faults = new ArrayList<>();
    ImageView speedlimitImage;
    LinkedBlockingQueue<SFC> Gsfcs;

    private  static  LocalDate localdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        BluetoothService.checkPermissions(MainActivity.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        dist =0;

        getSupportActionBar().hide();
        tutorial();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

speedLimits();
        if (emu == true)
        {
            fuelLevel = EmuService.getFuel(MainActivity.this);


            EmuService.writeFuel(MainActivity.this, (int)fuelLevel);
            timer();
        }
        Toolbar toolbar = findViewById(R.id.navbar);




        tVRPM = findViewById(R.id.tVRPM);
        tVSpeed = findViewById(R.id.tVSpeed);
        tvfuel = findViewById(R.id.tvfuel);
  /*      btnFuel = findViewById(R.id.btnFuel);
      //  rG = (RadioGroup) findViewById(R.id.radioGroup);
uptime = findViewById(R.id.uptime);
        btn = (Button) findViewById(R.id.buttonre);
        btnStats = (Button) findViewById(R.id.btnStats);*/
        btnEmu = (Button) findViewById(R.id.btnEmuStopReading);

        btnEmu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop = true;
            }
        });
   /*     btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, JobsActivity.class);
                //putextra the faults
                i.putExtra("faults", faults);
                startActivity(i);
            }
        });*/
/*
        btnFuel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, FuelActivity.class);
                //putextra the sfcs
                if (Gsfcs == null)
                {
                    Gsfcs=  getsfcs();
                }
                i.putExtra("Gsfcs", Gsfcs);
                startActivity(i);
            }
        });

        btnStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, StatsActivity.class);
                startActivity(i);
            }
        });*/
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.readings_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                System.out.println(item.getTitle().toString());
                item.getTitle().toString();
                Intent i;
                switch (item.getTitle().toString()) {
                    case "home":
                        return true;
                    case "stats":
                        i = new Intent(MainActivity.this, StatsActivity.class);
                        if (Gsfcs == null)
                        {
                            Gsfcs=  getsfcs();
                        }
                        i.putExtra("Gsfcs", Gsfcs);
                        i.putExtra("faults", faults);
                        startActivity(i);
                        return true;
                    case "fuel":
                       i = new Intent(MainActivity.this, FuelActivity.class);
                        if (Gsfcs == null)
                        {
                            Gsfcs=  getsfcs();
                        }
                        i.putExtra("Gsfcs", Gsfcs);
                        i.putExtra("faults", faults);
                        startActivity(i);
                        return true;
                    case "jobs":
                         i = new Intent(MainActivity.this, JobsActivity.class);
                        i.putExtra("faults", faults);
                        if (Gsfcs == null)
                        {
                            Gsfcs=  getsfcs();
                        }
                        i.putExtra("Gsfcs", Gsfcs);
                        i.putExtra("faults", faults);
                        startActivity(i);
                        return true;
                    default:
                        return false;
                }
            }
        });
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
                                 }
                         }
                 });
                 executor.execute(new Runnable() {

                     @Override
                     public void run() {
                                 try {
                                     while (btSocket.isConnected() || emu == true) {
                                         Map<String, Object> thisDoc = new HashMap<>();

                                         localdate = LocalDate.now();

                                         Gspeed = getLiveSpeed(finalInputStream, finalOutputStream);
                                         if (stop == true)
                                         {throw new NullPointerException();}
                                         if (Gspeed != 0) {
                                             thisDoc.put("type", "Speed");
                                             thisDoc.put("value", Gspeed);
                                             thisDoc.put("datetime", Timestamp.now());
                                             speedList.add(thisDoc);
                                         }
                                         tVSpeed.setText(String.valueOf(Gspeed));
                                         try {
                                             Thread.sleep(5000);
                                         } catch (InterruptedException e) {
                                             e.printStackTrace();
                                         }
                                     }
                                 } catch (NullPointerException e) {
                                     addToFirestore(speedList);
                                 }
                         }
                 });
            }
            catch(Exception e){
e.printStackTrace();
            } }}

    private void tutorial() {

        tut = new Dialog(MainActivity.this);

  //  tut.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    tut.setTitle("Welcome to DrivePal");
        View modal = getLayoutInflater().inflate(R.layout.modal, null);
        vF = modal.findViewById(R.id.vFl);

        tut.setContentView(R.layout.modal);

        tut.show();



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navmenu,menu);


        return true;
    }

    private LinkedBlockingQueue<SFC> getsfcs() {

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String existingJSON = sharedPref.getString("sfcs", null);
        Type type = new TypeToken<LinkedBlockingQueue<SFC>>() {
        }.getType();

        return g.fromJson(existingJSON, type);
    }

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
                int speed = 0;
                speed = Integer.parseInt(tVSpeed.getText().toString());

                if (EmuService.over(imgs, speed))
                {
                    overTheLimit();
                }

                handler.postDelayed(this, 10000);
            }
        };

        Handler h = new Handler();
        handler.postDelayed(ru, 10000);

    }

    private void overTheLimit() {
            Vibrator v = (Vibrator) MainActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
            Toast.makeText(MainActivity.this, "SLOW DOWN!", Toast.LENGTH_LONG).show();
            v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));


    }

        private BluetoothSocket connectToOBD()
    {
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
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
            return EmuService.getRPM();
        }
    }
    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;
    Handler handler = new Handler();
    int Seconds, Minutes, MilliSeconds ;

    private void timer() {
        StartTime = SystemClock.uptimeMillis();
        handler.postDelayed(runnable, 0);
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
//uptime.setText(timer);
dist += distanceHandler();

if (stop == true)
{
    fuelConsumptionHandler(dist);
    return;
}
            if (Seconds % 5 == 0 && MilliSeconds < 5)
            {
                if (fuelLevel >= 1)
                fuelLevel -= 1;
                else
                    fuelLevel = 44;
                EmuService.writeFuel(MainActivity.this, (int)fuelLevel);
                tvfuel.setText("Fuel level: "+String.valueOf(fuelLevel) + " Litres");
                gauge(fuelLevel / 45f);
            }

            handler.postDelayed(this, 0);
        }

    };

    public void fuelConsumptionHandler(double dist) {
        fuelConsumed = dist /23800;
        SFCPrefHandler(fuelConsumed);
    }

    private double distanceHandler(/*int milliSeconds*/) {

        double speed = Gspeed;
        speed = speed * .2778;
        double distance = speed * 5;

        return distance;
    }

    private void SFCPrefHandler(double fuelConsumed)
    {
        LinkedBlockingQueue<SFC> sfcs;


        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String existingJSON = sharedPref.getString("sfcs", null);

        LocalDate today = LocalDate.now();

        String dayName = today.format(DateTimeFormatter.ofPattern("E"));
        if (existingJSON == null) {
            sfcs = new LinkedBlockingQueue<>(7);

            SFC newsfc = new SFC(fuelConsumed, dayName, dist);
            sfcs.add(newsfc);
            writeSFCs(sfcs);
            Gsfcs = sfcs;
        }
        else {
            //if it exists
            Type type = new TypeToken<LinkedBlockingQueue<SFC>>() {
            }.getType();

            sfcs = g.fromJson(existingJSON, type);
            sfcs = putNewSFC(sfcs, fuelConsumed, dayName);
            writeSFCs(sfcs);
            Gsfcs = sfcs;
        }
    }

    private LinkedBlockingQueue<SFC> putNewSFC(LinkedBlockingQueue<SFC> sfcs, double fuelConsumed, String dayName) {
        ArrayList<SFC> arrsfcs = new ArrayList<SFC>(sfcs);

        LocalDateTime myDateObj = LocalDateTime.now();//.plusDays(4);
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String todaysdate = myDateObj.format(myFormatObj);

        if (sfcs.size() == 0)
        {
            sfcs.add(new SFC(fuelConsumed, dayName, dist, todaysdate));
            return sfcs;
        }
        else if (sfcs.size() == 7)
        {
            //LOGIC IN HERE FOR SAME DAY

            if (arrsfcs.get(6).dateString.equals(todaysdate))
            {
                arrsfcs.get(6).distance += dist;
                arrsfcs.get(6).value += fuelConsumed;
                return sfcs;

            }
            sfcs.poll();
            sfcs.offer(new SFC(fuelConsumed, dayName, dist, todaysdate));

            return sfcs;
        }
        else
        {
            for (SFC e:arrsfcs) {
                if (e.dateString.equals(todaysdate))
                {
                    arrsfcs.get(arrsfcs.indexOf(e)).distance += dist;
                    arrsfcs.get(arrsfcs.indexOf(e)).value += fuelConsumed;

                    sfcs = new LinkedBlockingQueue<>(arrsfcs);
                    return sfcs;
                }

            }
            if (LocalDate.parse(arrsfcs.get(arrsfcs.size()-1).dateString, myFormatObj).plusDays(1).atStartOfDay().equals(LocalDate.now().atStartOfDay()))
            {
                sfcs.offer(new SFC(fuelConsumed,dayName,dist));
                return sfcs;
            }
            ArrayList<SFC> gapsfcs = new ArrayList<SFC>();
            //between 0 and 7
            SFC last = arrsfcs.get(arrsfcs.size()-1);
            gapsfcs.add(new SFC(fuelConsumed, dayName, dist, todaysdate));

            LocalDateTime currentDate = LocalDateTime.now();
            for (int i=0; i<7; i++)
            {
                currentDate = currentDate.minusDays(1);
                if (currentDate.format(myFormatObj).equals(last.dateString))
                    break;
                gapsfcs.add(new SFC(0, currentDate.format(DateTimeFormatter.ofPattern("E")),0));
            }

                gapsfcs.add(last);
            Collections.reverse(gapsfcs);
            sfcs = new LinkedBlockingQueue<>(gapsfcs);
            return sfcs;

        }
    }

    void writeSFCs(LinkedBlockingQueue<SFC> sfcs)
    {
        SharedPreferences sharedPref = getPreferences( Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
       editor.putString("sfcs",g.toJson(sfcs));
editor.apply();
    }

    public void nextModal(View view) {
        tut.setContentView(R.layout.fuel_tut);
        tut.setTitle("View reading insights");
        System.out.println("DID");
    }

    public void nextJobs(View view) {
        tut.setContentView(R.layout.jobs_tut);
        tut.setTitle("Jobs screen");

    }

    public void closeTut(View view)
    {
        tut.cancel();
    }

    public void skip(View view) {
        closeTut(view);
    }
}