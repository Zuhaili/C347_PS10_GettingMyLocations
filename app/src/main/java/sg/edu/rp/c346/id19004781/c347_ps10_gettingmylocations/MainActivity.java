package sg.edu.rp.c346.id19004781.c347_ps10_gettingmylocations;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileWriter;

public class MainActivity extends AppCompatActivity {
    private GoogleMap map;
    Button btnStart, btnStop, btnRecords;
    TextView tvLat, tvLog;
    ToggleButton music;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    FusedLocationProviderClient client;
    LatLng poi_Marker;
    String folderLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnRecords = findViewById(R.id.btnCheckRecords);
        tvLat = findViewById(R.id.tvLat);
        tvLog = findViewById(R.id.tvLog);
        music = findViewById(R.id.toggleButtonMusic);

        String[] permission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(MainActivity.this, permission, 0);
        if(checkPermission()){
            client = LocationServices.getFusedLocationProviderClient(MainActivity.this);
            Task<Location> task = client.getLastLocation();
            task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Log.i("check",location.toString());
                    String msg = "";
                    if (location != null) {
                        poi_Marker = new LatLng(location.getLatitude(), location.getLongitude());
                        tvLat.setText("Latitude: " + location.getLatitude());
                        tvLog.setText("Longitude: " + location.getLongitude());
                        msg = "Marker Exists";
                    } else {
                        msg = "No last known location found";
                    }
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        }



        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;

                LatLng poi_SG = new LatLng(1.3348883, 103.9833633);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(poi_SG,
                        10));
                UiSettings ui = map.getUiSettings();
                ui.setCompassEnabled(true);
                ui.setZoomControlsEnabled(true);
                int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

                if (permissionCheck == PermissionChecker.PERMISSION_GRANTED) {
                    map.setMyLocationEnabled(true);
                } else {
                    Log.e("GMap - Permission", "GPS access has not been granted");
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                }

            }
        });

        mLocationRequest = new LocationRequest();
        mLocationCallback = new LocationCallback() {
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location data = locationResult.getLastLocation();
                    double lat = data.getLatitude();
                    double log = data.getLongitude();

                    LatLng poi = new LatLng(lat, log);
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(poi,
                            15));
                    Marker north = map.addMarker(new
                            MarkerOptions()
                            .position(poi)
                            .title("HQ Singapore")
                            .snippet("Block 551, Woodlands Ave 4, 730551\n Operating hours: 9am-9pm\n" +
                                    "Tel:68934653\n")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                    try {
                        String folderLocation =
                                Environment.getExternalStorageDirectory()
                                        .getAbsolutePath() + "/PS10";
                        File folder = new File(folderLocation);
                        if (folder.exists() == false) {
                            boolean result = folder.mkdir();
                            if (result == true) {
                                Log.d("File Read/Write", "Folder created");
                            }
                        }
                        try {
                            folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PS10";
                            File targetFile = new File(folderLocation, "data4.txt");
                            FileWriter writer = new FileWriter(targetFile, true);
                            writer.write(lat +"," + log+"\n");
                            writer.flush();
                            writer.close();
                        } catch (Exception e) {
                            Log.d("folder",folderLocation.toString());
                            Toast.makeText(MainActivity.this, "Failed to write!", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to create folder!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivity.this, "New location detected: " + lat + "\n" + log, Toast.LENGTH_SHORT).show();
                }
            }
        };
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkPermission()) {
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    mLocationRequest.setInterval(30000);
                    mLocationRequest.setFastestInterval(500);
                    mLocationRequest.setSmallestDisplacement(500);
                    client = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                    client.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission()) {
                    client.removeLocationUpdates(mLocationCallback);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
        });
        btnRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,CheckRecordsActivity.class);
                startActivity(i);
            }
        });

        music.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    startService(new Intent(MainActivity.this, MyService.class));
                }
                else{
                    stopService(new Intent(MainActivity.this, MyService.class));

                }
            }
        });

    }

    private boolean checkPermission() {
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (
                permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED
        ) {
            return true;
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkPermission()){
            client = LocationServices.getFusedLocationProviderClient(MainActivity.this);
            Task<Location> task = client.getLastLocation();
            task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {

                    String msg = "";
                    if (location != null) {
                        poi_Marker = new LatLng(location.getLatitude(), location.getLongitude());
                        tvLat.setText("Latitude: " + location.getLatitude());
                        tvLog.setText("Longitude: " + location.getLongitude());
                        //msg = "Marker Exists again";
                    } else {
                        msg = "No last known location found";
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }
}