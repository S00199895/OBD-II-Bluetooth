package edu.markc.bluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class FuelActivity extends AppCompatActivity {
BarChart fuelBC;
double totalDistance;
double avgFE;
    TextView tvfe ;
            TextView tvdist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel);

        tvfe = findViewById(R.id.avgFE);
         tvdist = findViewById(R.id.totaldist);
        getSupportActionBar().hide();
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        ArrayList<SFC> fuelData;

        if (getIntent().getExtras() != null)
        {
            fuelData = (ArrayList<SFC>) getIntent().getSerializableExtra("Gsfcs");
        }
        else {
            fuelData = new ArrayList<SFC>();
        }
        getStats(fuelData);
        fuelBC = findViewById(R.id.fuel_barchart);
        //sharedprefs
        makeBarChart(fuelData);
    }

    private void getStats(ArrayList<SFC> fuelData) {
        totalDistance = 0;
        avgFE = 0;
        double totalFuel = 0;
        for (SFC e:fuelData)
        {
            totalDistance += e.distance;
            totalFuel += e.value;
        }
totalDistance = totalDistance / 1000;
        avgFE = totalDistance / totalFuel;
DecimalFormat df = new DecimalFormat("0.00");
/*String formattedDouble = df.format(myDouble);*/
        tvfe.setText("Average Fuel Efficiency: " + df.format(avgFE) + " km/L");
        tvdist.setText("Total Distance Travelled: " + df.format(totalDistance) + " km");
    }


    void makeBarChart(ArrayList<SFC> fuelData)
    {
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < fuelData.size(); i++) {
            entries.add(new BarEntry( i, (float) fuelData.get(i).value));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Values");

        /*for (SFC e:fuelData
             ) {
            entries.add(new Entry())
        }*/
        BarData barData = new BarData(dataSet);
        fuelBC.setData(barData);
        XAxis xAxis = fuelBC.getXAxis();
        xAxis.setLabelCount(fuelData.size(), true);
        YAxis yAxis = fuelBC.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        barData.setBarWidth(.45f);
        yAxis.setTextColor(Color.WHITE);
        xAxis.setTextColor(Color.WHITE);
        Description desc= new Description();
        desc.setText("");
        fuelBC.setDescription(desc);
xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
xAxis.setXOffset(barData.getBarWidth() / 2);

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {

                return fuelData.get((int)value).dayName;

               // return super.getAxisLabel(value, axis);
            }
        });

fuelBC.invalidate();
    }
}