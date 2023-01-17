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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.AvailablePidsCommand_21_40;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private final static int REQUEST_ENABLE_BT = 1;
    private static final UUID CONNUUID = UUID.fromString("ea3836df-b860-4f33-b338-4e032c124870");
    TextView tv1;
    FirebaseFirestore db;
    LineChart mChart;
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
        tv1 = findViewById(R.id.tV1);
        ArrayList<Map<String, Object>> reads = read();
        BluetoothSocket btSocket = connectToOBD();
       // ArrayList<Integer> testrpm = new ArrayList<>();
//        testrpm.add(20);
//        testrpm.add(30);
//        testrpm.add(40);
//        testrpm.add(60);
//        testrpm.add(20);testrpm.add(20);

//       ArrayList<Map<String, Object>> objsToPush = formatResults(testrpm);
//
//        addToFirestore(objsToPush);

      //  ArrayList<Map<String, Object>> wantedReadings = read();


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
                 final Handler handler = new Handler();

                 final int delay = 5000;

                 InputStream finalInputStream = inputStream;
                 OutputStream finalOutputStream = outputStream;

                 ArrayList<Map<String, Object>> rpmList = new ArrayList<>();

                 handler.postDelayed(new Runnable() {
                     @Override
                     public void run() {
                         //need to check if obd connected
                         //else

                         //work
                         if (btSocket.isConnected()) {
                           //  getRunTime(finalInputStream, finalOutputStream);
                             try {
                                 Map<String, Object> thisDoc = new HashMap<>();
                                 LocalDateTime date = LocalDateTime.now();
                                 localdate = LocalDate.now();
                                 int rpm = getLiveRPM(finalInputStream, finalOutputStream);

                                 thisDoc.put("type", "RPM");
                                 thisDoc.put("value", rpm);
                                 thisDoc.put("datetime", date.toString());


                                 rpmList.add(thisDoc);
                                 tv1.setText(String.valueOf(rpm));
                                 //work
                                 handler.postDelayed(this, delay);
                             }
                             catch (NullPointerException e)
                             {
                                 addToFirestore(rpmList);
                                 read();
                             }
                         }
                         else {
                        //     ArrayList<Map<String, Object>> formattedValues = formatResults(rpmList);
                             addToFirestore(rpmList);
                             read();
                         }
                     }
                 }, delay);

               /* ObdRawCommand custom = new ObdRawCommand("00"); //command can be any ELM command like "atz" or "0130"
                custom.run(inputStream, outputStream); //inputs - input stream, outputs - output stream
                String result = custom.getResult(); // your variable with result*/
            }
            catch(Exception e){

            }




        }
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

    private ArrayList<Map<String, Object>> read() {
        //read and query where type is rpm
        ArrayList<Map<String, Object>> readMaps = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        /*
        *  db.collection("data")
                    .document(String.valueOf(LocalDate.now()))
                    .collection(String.valueOf(oneDoc.get("type")oneDoc.get("type")))
                    .add(oneDoc)/*.add(oneDoc)
        * */
       db.collection("data").document(String.valueOf(LocalDate.now())).collection("RPM")
               .whereEqualTo("type", "RPM").orderBy("datetime", Query.Direction.ASCENDING)
               .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                   @Override
                   public void onComplete(@NonNull Task<QuerySnapshot> task) {
                       if (task.isSuccessful())
                       {
                           for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                               System.out.println(doc.getData());
                               readMaps.add(doc.getData());

                           }
                           makeLineChart(readMaps);

                       } else {
                           Log.d(TAG, "get failed with ", task.getException());
                       }
                       }

               }).addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       e.printStackTrace();
                   }
               });



        return readMaps;
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
                    .collection(String.valueOf(oneDoc.get("type")/*oneDoc.get("type")*/))
                    .add(oneDoc)/*.add(oneDoc)*/;

        }

    }

    private void addToFirestore() {
        db = FirebaseFirestore.getInstance();

        Map<String, Object> user = new HashMap<>();
        user.put("first", "Ada");
        user.put("last", "Lovelace");
        user.put("born", 1815);

        // Add a new document with a generated ID
        db.collection("data")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private void makeLineChart(ArrayList<Map<String, Object>> chartData) {

        System.out.println("chart valeus");
        System.out.println(chartData);
        //give this chart times for order and dates
        //plot against values
        //display chart
        ArrayList<Entry> yValues = new ArrayList<>();


        int i = 0;
        for(Map<String, Object> oneDoc : chartData)
        {
            yValues.add(new Entry(i, Float.parseFloat(String.valueOf(oneDoc.get("value")))));
            i++;

        }
        System.out.println("yvalues");
        System.out.println(yValues);


        mChart = (LineChart) findViewById(R.id.lineChart);
        mChart.setBackgroundColor(Color.WHITE);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);


        LineDataSet set1 = new LineDataSet(yValues, "Data");
        set1.setFillAlpha(110);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        LineData data = new LineData(dataSets);
        mChart.setData(data);
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
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

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