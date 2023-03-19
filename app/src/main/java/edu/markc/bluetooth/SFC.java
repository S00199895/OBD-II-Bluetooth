package edu.markc.bluetooth;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SFC implements Serializable {
    public double value;
    public String dateString;
  //  public LocalDateTime datetime;
    public String dayName;
    public double distance;


    //store this with every one?
    public  String[] days = new String[] {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    public SFC() {
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        this.dateString = myDateObj.format(myFormatObj);
      //  this.datetime = myDateObj;
    }

    public SFC(double v, String day, double d)
    {
        this.value = v;
        this.dayName =day;
        this.distance = d;

        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        this.dateString = myDateObj.format(myFormatObj);
     //   this.datetime = LocalDateTime.now();

    }


}

