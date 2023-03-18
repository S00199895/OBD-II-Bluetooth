package edu.markc.bluetooth;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SFC {
    public double value;
    public String dateString;
    public LocalDateTime datetime;
    public String dayName;

    //store this with every one?
    public  String[] days = new String[] {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    public SFC() {
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        this.dateString = myDateObj.format(myFormatObj);
        this.datetime = myDateObj;
    }

    public SFC(double v, String day)
    {
        this.value = v;
        this.dayName =day;

        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        this.dateString = myDateObj.format(myFormatObj);
        this.datetime = myDateObj;

    }


}

