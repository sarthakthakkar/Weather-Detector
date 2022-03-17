package com.example.weatherdetector;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashboardActivity extends AppCompatActivity {

    TextView tvTemperature;
    TextView tvHumidity;
    TextView tvPressure;
    TextView tvAirQuality;
    Button btnTemperature;
    Button btnHumidity;
    Button btnPressure;
    Button btnAirQuality;
    Button btnAdd;
    Button btnAddDevice;
    EditText etDeviceName;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initialize();
        getDeviceIdReference();
        setOnclickListeners();
    }

    public void initialize(){
        this.tvTemperature = findViewById(R.id.tvTemperature);
        this.tvHumidity = findViewById(R.id.tvHumidity);
        this.tvPressure = findViewById(R.id.tvPressure);
        this.tvAirQuality = findViewById(R.id.tvAirQuality);
        this.btnTemperature = findViewById(R.id.btnTemperature);
        this.btnHumidity = findViewById(R.id.btnHumidity);
        this.btnPressure = findViewById(R.id.btnPressure);
        this.btnAirQuality = findViewById(R.id.btnAirQuality);
        this.btnAdd = findViewById(R.id.btnAdd);
        this.btnAddDevice = findViewById(R.id.btnAddDevice);
        this.etDeviceName = findViewById(R.id.etDeviceName);
        this.database = FirebaseDatabase.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.user = auth.getCurrentUser();
        this.reference = database.getReference("Users").child(user.getUid()).child("DeviceId");
    }

    public void setOnclickListeners(){
        btnTemperature.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AnalysisActivity.class);
            intent.putExtra("condition","temperature_reading");
            intent.putExtra("label","Temperature");
            startActivity(intent);
        });

        btnHumidity.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AnalysisActivity.class);
            intent.putExtra("condition", "humidity_reading");
            intent.putExtra("label", "Humidity");
            startActivity(intent);
        });

        btnPressure.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AnalysisActivity.class);
            intent.putExtra("condition","pressure_reading");
            intent.putExtra("label", "Pressure");
            startActivity(intent);
        });

        btnAirQuality.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AnalysisActivity.class);
            intent.putExtra("condition","CO2_reading");
            intent.putExtra("label", "Air Quality (CO2)");
            startActivity(intent);
        });

        btnAddDevice.setOnClickListener(v -> {
            btnAddDevice.setVisibility(View.INVISIBLE);
            etDeviceName.setVisibility(View.VISIBLE);
            btnAdd.setVisibility(View.VISIBLE);
        });

        btnAdd.setOnClickListener(v -> {
            reference.setValue(etDeviceName.getText().toString());
            Toast.makeText(getApplicationContext(),"Device Added", Toast.LENGTH_SHORT).show();
            btnAddDevice.setVisibility(View.VISIBLE);
            etDeviceName.setVisibility(View.INVISIBLE);
            btnAdd.setVisibility(View.INVISIBLE);
        });
    }

    public void getDeviceIdReference(){

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               if(snapshot.getValue() != null)
                   getCurrentReadings(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void getCurrentReadings(String str){
        DatabaseReference ref = database.getReference().child("Weather Station Reading/" + str);
        ref.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    for(DataSnapshot snap : snapshot.getChildren()){
                        if(snap.getKey().equals("current_temperature"))
                            tvTemperature.setText(snap.getValue().toString());
                        if(snap.getKey().equals("current_humidity"))
                            tvHumidity.setText(snap.getValue().toString());
                        if(snap.getKey().equals("current_pressure"))
                            tvPressure.setText(snap.getValue().toString());
                        if(snap.getKey().equals("current_CO2"))
                            tvAirQuality.setText(snap.getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }

        });

    }
}
