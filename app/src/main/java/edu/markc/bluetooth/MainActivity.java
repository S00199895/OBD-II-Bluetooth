package edu.markc.bluetooth;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import com.github.mikephil.charting.charts.BarChart;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdRawCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity {
    private final static int REQUEST_ENABLE_BT = 1;
    private static final UUID CONNUUID = UUID.fromString("ea3836df-b860-4f33-b338-4e032c124870");
    TextView tv1;
    FirebaseFirestore db;
    BarChart bc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv1 = findViewById(R.id.tV1);
        db = FirebaseFirestore.getInstance();
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }

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

                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);


        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        BluetoothDevice bose = bluetoothAdapter.getRemoteDevice("10:21:3E:48:A4:5B");
        String boseName = bose.getName();

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
            btSocket = bose.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            btSocket.connect();

        } catch (IOException e) {

            e.printStackTrace();
        }

        System.out.print(btSocket);

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
                 handler.postDelayed(new Runnable() {
                     @Override
                     public void run() {

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
                         //work
                         //int rn = ThreadLocalRandom.current().nextInt(0, 10);

                         tv1.setText(String.valueOf(rpm));
                         //work
                         handler.postDelayed(this, delay);
                     }
                 }, delay);
                 //DO COMMANDS HERE

                // System.out.print(result);
               /* ObdRawCommand custom = new ObdRawCommand("00"); //command can be any ELM command like "atz" or "0130"
                custom.run(inputStream, outputStream); //inputs - input stream, outputs - output stream
                String result = custom.getResult(); // your variable with result*/
            }
            catch(Exception e){

            }




        }
}}