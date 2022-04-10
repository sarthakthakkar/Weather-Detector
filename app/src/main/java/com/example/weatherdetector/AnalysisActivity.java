package com.example.weatherdetector;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AnalysisActivity extends AppCompatActivity {

    private TextView tvMaxReading;
    private TextView tvMinReading;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initialize();
        getDeviceIdReference();
    }

    private void initialize(){
        this.tvMaxReading = findViewById(R.id.tvMaxReading);
        this.tvMinReading = findViewById(R.id.tvMinReading);
        this.database = FirebaseDatabase.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.user = auth.getCurrentUser();
        this.reference = database.getReference("Users").child(user.getUid()).child("DeviceId");
    }

    public void getDeviceIdReference(){

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Intent intent = getIntent();
                String condition = intent.getStringExtra("condition");
                String label = intent.getStringExtra("label");
                String deviceId = snapshot.getValue().toString();
                initializeGraph(condition, label, deviceId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void initializeGraph(String condition, String label, String deviceId){
        ArrayList<Entry> list = new ArrayList<>();
        LineChart chart = findViewById(R.id.chartLine);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference().child("Weather Station Reading/" + deviceId + "/historical_readings");

        ref.addValueEventListener(new ValueEventListener() {
            int i = 0;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                float max = -999_999;
                float min = 999_999;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    for(DataSnapshot snap : snapshot.getChildren()){
                        if(snap.getKey().equals(condition)) {
                            float value = Float.parseFloat(snap.getValue().toString());
                            list.add(new Entry(i, value));
                            i++;

                            if(value > max) max = value;
                            if(value < min) min = value;
                        }
                    }

                    setText(max, min);
                }

                LineDataSet dataSet = new LineDataSet(list, label);
                LineData lineData = new LineData(dataSet);
                chart.setData(lineData);
                chart.invalidate();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    private void setText(float max, float min){
        this.tvMaxReading.setText(Float.toString(max));
        this.tvMinReading.setText(Float.toString(min));
    }

}
