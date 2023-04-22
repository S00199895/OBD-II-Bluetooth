package edu.markc.bluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
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
import java.util.concurrent.LinkedBlockingQueue;

public class FuelActivity extends AppCompatActivity {
BarChart fuelBC;
double totalDistance;
double avgFE;
    LinkedBlockingQueue<SFC> Gsfcs;
    ArrayList<String> faults;
    TextView tvfe ;
            TextView tvdist;
            TextView tveff;
            TextView details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel);

        tvfe = findViewById(R.id.avgFE);
         tvdist = findViewById(R.id.totaldist);
         tveff = findViewById(R.id.tvEfficiency);
details = findViewById(R.id.tvDetails);
        getSupportActionBar().hide();
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        LinkedBlockingQueue<SFC> fuelData;

        if (getIntent().getExtras() != null)
        {
            fuelData = (LinkedBlockingQueue<SFC>) getIntent().getSerializableExtra("Gsfcs");
            faults = (ArrayList<String>) getIntent().getSerializableExtra("faults");
        }

        else {
            fuelData = new LinkedBlockingQueue<>();
        }


        Toolbar toolbar = findViewById(R.id.navbar);



        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                System.out.println(item.getTitle().toString());
                item.getTitle().toString();
                Intent i;
                final String HOME = getResources().getString(R.string.homeu);
                switch (item.getTitle().toString()) {
                    case "HOME":
                        i = new Intent(FuelActivity.this, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);


                        i.putExtra("Gsfcs", Gsfcs);
                        i.putExtra("faults", faults);
                        startActivity(i);
                        return true;
                    case "stats":
                        i = new Intent(FuelActivity.this, StatsActivity.class);
                        //putextra the faults
                        i.putExtra("faults", faults);
                        i.putExtra("Gsfcs", Gsfcs);
                        i.putExtra("faults", faults);
                        startActivity(i);
                        return true;
                    case "fuel":
                        return true;
                    case "jobs":
                        i = new Intent(FuelActivity.this, JobsActivity.class);
                        //putextra the faults
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


        ArrayList<SFC> arrFuelData = new ArrayList<>(fuelData);
        getStats(arrFuelData);
        fuelBC = findViewById(R.id.fuel_barchart);
        //sharedprefs
        makeBarChart(arrFuelData);
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
        avgFE =(totalFuel * 100) / totalDistance;
DecimalFormat df = new DecimalFormat("0.00");
/*String formattedDouble = df.format(myDouble);*/

        tvfe.setText("Average Fuel Efficiency: " + df.format(avgFE) + " L/100km");
        tvdist.setText("Total Distance Travelled: " + df.format(totalDistance) + " km");

        efficiencyDetails(avgFE);
    }

    private void efficiencyDetails(double avgFE) {

        if (avgFE <= 4.5) {

            tveff.setTextColor(Color.parseColor("#0e910c"));
            tveff.setText("Good");
            details.setText("Keep it up!");
        } else if (avgFE > 4.5 && avgFE < 6)
        {
            tveff.setTextColor(Color.BLUE);
            tveff.setText("Fine");

            details.setText("Lower your revs to improve your fuel efficiency.");

        }
        else if (avgFE >= 6)
        {
            tveff.setTextColor(Color.RED);
            tveff.setText("Bad");

            details.setText("Make sure you're in the right gear to improve your fuel efficiency.");

        }
    }


    void makeBarChart(ArrayList<SFC> fuelData)
    {
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < fuelData.size(); i++) {
            entries.add(new BarEntry( i, (float) fuelData.get(i).value));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Values");

        BarData barData = new BarData(dataSet);
        fuelBC.setData(barData);


        XAxis xAxis = fuelBC.getXAxis();
        xAxis.setLabelCount(fuelData.size(), true);
        YAxis yAxis = fuelBC.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        barData.setBarWidth(.45f);
        yAxis.setTextColor(Color.BLACK);
        xAxis.setTextColor(Color.BLACK);
        Description desc= new Description();
        desc.setText("");
        fuelBC.setDescription(desc);
xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
xAxis.setXOffset(barData.getBarWidth() / 2);

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {

                return fuelData.get((int)value).dayName;

            }
        });

fuelBC.invalidate();
    }
}