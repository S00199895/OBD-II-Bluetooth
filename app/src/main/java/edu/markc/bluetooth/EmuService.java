package edu.markc.bluetooth;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.widget.Chronometer;


import com.google.common.base.Stopwatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class EmuService {
    //static int fuel;
    static SharedPreferences sharedPref;
    static SharedPreferences.Editor editor;
    static Stopwatch s;


    public EmuService(Activity main)
    {
        SharedPreferences sharedPref = main.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        s = Stopwatch.createStarted();
        if (s.isRunning())
        {
            stopStopwatch();
        }
//    else
           s.start();


    }
    public static long stopStopwatch()
    {
        s.stop();
        return s.elapsed(TimeUnit.MINUTES);
    }

    //faults
    public static ArrayList<String> getFaults()
    {
        return new ArrayList<String>(
                Arrays.asList(
                        "P0088: Fuel Rail/System Pressure - Too High",
                        "P0094: Fuel System Leak Detected - Small Leak",
                        "P0230: Fuel Pump Primary Circuit Malfunction"
                )
        );
    }
    //speed
    public static int getSpeed()
    {
        Random random = new Random();
        //logic to reduce fuel
        int speed = 30;
        return random.nextInt(90) + speed;

    }

    //rpm
    public static int getRPM()
    {
        int rpm = 900;

        Random random = new Random();

        return random.nextInt(1001) + rpm;
    }

    public static boolean over(TypedArray imgs, int speed)
    {
        //passed:
       // TypedArray imgs = getResources().obtainTypedArray(R.array.speedimgs);
        //Integer.parseInt(tVSpeed.getText().toString())
        int[] speeds = new int[] {50,60,80,100,120};
        Random r = new Random();
        int speedIndex = r.nextInt(5);
        //speedlimitImage.setImageResource(imgs.getResourceId(speedIndex,0));


        if (speeds[speedIndex] < speed)
        {
            return true;
        }
        return  false;
    }

    //fuel
    public static int getFuel(Activity main)
    {

        SharedPreferences sharedPref = main.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        int readfuel = sharedPref.getInt("fuelLevel", -1);
        if (readfuel == -1)
        {

            return 45;
        }
        else
        {
            return readfuel;
        }

    }

    public static void writeFuel(Activity main, int fuel)
    {
        SharedPreferences sharedPref = main.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt("fuelLevel", fuel);
        editor.commit();
    }



    public void updateFuel(Activity main)
    {
//        if (s.elapsed(TimeUnit.SECONDS) == TimeUnit.SECONDS.toSeconds(10L))
//        {
//            fuel -= 1;
//            writeFuel(main);
//        }
    }





}
//trip time