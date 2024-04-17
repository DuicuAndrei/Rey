package com.example.rey;

import android.Manifest;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import io.reactivex.annotations.NonNull;

import java.util.HashMap;

public class CustomerActivityMap extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Button btnRequestTaxi, mlogout;
    private FirebaseAuth mAuth;
    private FirebaseFunctions mFunctions;
    private  LatLng userLocation;
    private DatabaseReference customerLocationRef;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        customerLocationRef = database.getReference("customer_locations").child(mAuth.getCurrentUser().getUid());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        mAuth = FirebaseAuth.getInstance();

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Define location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        userLocation = new LatLng(location.getLatitude(),location.getLongitude());
                        updateLocationOnMap(location);
                        saveCustomerLocationToFirebase(location.getLatitude(), location.getLongitude());
                    }
                }
            }
        };


        mFunctions = FirebaseFunctions.getInstance ();

        // Initialize button
        btnRequestTaxi = (Button) findViewById(R.id.btnRequestTaxi);


        btnRequestTaxi.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(userLocation != null ) {
                    sendTaxiRequest(userLocation);
                }else {
                    Toast.makeText(CustomerActivityMap.this, "User location not available.1", Toast.LENGTH_SHORT).show();
                }
            }

        });

        mlogout = (Button) findViewById(R.id.mlogout);

        if(mlogout != null) {
            mlogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    logoutUser();
                }
            });
        }else{
            Toast.makeText(CustomerActivityMap.this, "Button is null",Toast.LENGTH_SHORT).show();
        }
    }


    private void saveCustomerLocationToFirebase(double latitude, double longitude) {
        customerLocationRef.child("latitude").setValue(latitude);
        customerLocationRef.child("longitude").setValue(longitude);
    }

    private void logoutUser(){

        mAuth.signOut();
        Toast.makeText(CustomerActivityMap.this, "Logout succesfull ",Toast.LENGTH_SHORT).show();
        redirectToMainActivity();
    }

    private void redirectToMainActivity() {
        Intent intent = new Intent(CustomerActivityMap.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // Metoda pentru a trimite cererea de taxi către serviciul Cloud Function
    private void sendTaxiRequest(LatLng userLocation) {
        if (userLocation != null) {
            // Obțineți o referință către baza de date Firebase
            DatabaseReference taxiRequestsRef = FirebaseDatabase.getInstance().getReference("taxi_requests");

            // Creează un obiect pentru locația de preluare a utilizatorului
            HashMap<String, Object> pickupLocation = new HashMap<>();
            pickupLocation.put("latitude", userLocation.latitude);
            pickupLocation.put("longitude", userLocation.longitude);

            // Adaugă locația de preluare a utilizatorului la baza de date Firebase
            taxiRequestsRef.push().setValue(pickupLocation)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Notifică utilizatorul că cererea de taxi a fost trimisă cu succes
                            Toast.makeText(CustomerActivityMap.this, "Taxi request sent.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Notifică utilizatorul dacă a apărut o eroare în timpul trimiterii cererii de taxi
                            Toast.makeText(CustomerActivityMap.this, "Failed to send taxi request.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Dacă locația utilizatorului nu este disponibilă, afișați un mesaj corespunzător
            Toast.makeText(this, "User location not available.", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
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
            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Your Location"));
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

}
