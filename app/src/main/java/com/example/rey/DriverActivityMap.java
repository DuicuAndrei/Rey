package com.example.rey;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import io.reactivex.annotations.NonNull;
import org.jetbrains.annotations.NotNull;


public class DriverActivityMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Button mLogout, mRequest;
    private FirebaseAuth mAuth;
    private DatabaseReference driverLocationRef;



    private static final String TAG = "DriverActivityMap";



    public DriverActivityMap() {
    }
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        driverLocationRef = database.getReference("driver_locations").child(mAuth.getCurrentUser().getUid());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Define location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NotNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    updateLocationOnMap(location);
                    saveDriverLocationToFirebase(location.getLatitude(), location.getLongitude());
                }
            }
        };


        mAuth = FirebaseAuth.getInstance();

        mRequest = (Button) findViewById(R.id.PickUpLocationBtn);

        if (mRequest != null) {
            mRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listenForTaxiRequests();

                }
            });
        } else {
            Toast.makeText(DriverActivityMap.this, "Button is null", Toast.LENGTH_SHORT).show();
        }

        mLogout = (Button) findViewById(R.id.logoutBtn);

        if (mLogout != null) {
            mLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logoutUser();
                }
            });

        } else {

            Toast.makeText(DriverActivityMap.this, "Button is null", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveDriverLocationToFirebase(double latitude, double longitude) {
        driverLocationRef.child("latitude").setValue(latitude);
        driverLocationRef.child("longitude").setValue(longitude);
    }

    private void logoutUser() {

        mAuth.signOut();

        Toast.makeText(DriverActivityMap.this, "Logout successful!", Toast.LENGTH_SHORT).show();
        redirectToMainActivity();


    }

    private void redirectToMainActivity() {
        Intent intent = new Intent(DriverActivityMap.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onMapReady(@NotNull GoogleMap googleMap) {
        mMap = googleMap;

        // Check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Enable My Location button and show current location
        mMap.setMyLocationEnabled(true);

        // Start location updates
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        // Create location request
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000) // Update location every 5 seconds
                .setFastestInterval(2000); // Set the fastest interval for location updates

        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void updateLocationOnMap(Location location) {
        if (mMap != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.clear(); // Clear previous markers

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15)); // Zoom to current location
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates when activity is paused
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    String name;

    public DriverActivityMap(String name) {
        this.name = name;
    }
    private void listenForTaxiRequests() {
        DatabaseReference taxiRequestsRef = FirebaseDatabase.getInstance().getReference("taxi_requests");
        taxiRequestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Obțineți locația clientului din cerere
                    double latitude = snapshot.child("latitude").getValue(Double.class);
                    double longitude = snapshot.child("longitude").getValue(Double.class);
                    LatLng customerLocation = new LatLng(latitude, longitude);

                    // Afișați locația clientului pe hartă
                    showCustomerLocationOnMap(customerLocation);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Gestionați cazurile în care nu se poate citi din baza de date
                Toast.makeText(DriverActivityMap.this, "Failed to read taxi requests.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showCustomerLocationOnMap(LatLng customerLocation) {
        if (mMap != null) {
            // Ștergeți marker-ul anterior al clientului, dacă există
            mMap.clear();
            // Adăugați un marker pentru locația clientului
            mMap.addMarker(new MarkerOptions().position(customerLocation).title("Customer Location"));
            // Mișcați camera pe locația clientului cu un zoom adecvat
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(customerLocation, 15));
        }
    }

}
