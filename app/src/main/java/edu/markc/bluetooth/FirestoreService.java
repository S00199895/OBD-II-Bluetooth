package edu.markc.bluetooth;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.math.Stats;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class FirestoreService {
    static FirebaseFirestore db;

    public static ArrayList<Map<String, Object>> read(String type, String interval) {
        //read and query where type is rpm
        ArrayList<Map<String, Object>>[] readMaps = new ArrayList[]{new ArrayList<>()};

        db = FirebaseFirestore.getInstance();

        //interval = "Week";
        if (interval.contains("Day")) {

            //change these RPMs to a variable
            //has to be a parameter
            db.collection("data").document(String.valueOf(LocalDate.now())).collection(type)
                    .whereEqualTo("type", type).orderBy("datetime", Query.Direction.ASCENDING)
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                System.out.println(doc.getData());
                                readMaps[0].add(doc.getData());
                            }
                           // makeLineChart(readMaps[0]);
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
                                      //  makeLineChart(readMaps[0]);

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
//change to ascending
            //19012023
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
                                        //StatsActivity.makeLineChart(readMaps[0]);

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

}
