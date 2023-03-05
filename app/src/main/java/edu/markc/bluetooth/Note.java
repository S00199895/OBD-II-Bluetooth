package edu.markc.bluetooth;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Note implements Serializable {
    public String title;
    public String content;
    public String timestamp;
    public Importance importance;

    public Note() {
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        timestamp = myDateObj.format(myFormatObj);
    }

    public Note(String t, String c, Importance i) {
        this.title = t;
        this.content = c;
        this.importance = i;
    }

    @Override
    public String toString() {
        return "Note{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}

