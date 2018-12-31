package com.example.macdaraday.bluetoothdevicesurvey;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.EventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,LocationListener,EventListener {

    private GoogleMap mMap;
    private ProgressDialog LocationDialog;
    private Marker markerLocation;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private long timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        startGettingLocations();
        getLocation();


        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();

        ValueEventListener postListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> oldLocations = dataSnapshot.child("Locations").getChildren();

                for(DataSnapshot locationSnapshot : oldLocations){
                    LocationData ld = locationSnapshot.getValue(LocationData.class);
                    LatLng lastLocation = new LatLng(ld.latitude,ld.longitude);
                    //System.out.println(lastLocation);

                    Long label = Long.parseLong(locationSnapshot.getKey());
                    Timestamp ts = new Timestamp(label);
                    String newlabel = ts.toString();

                    mMap.addMarker(new MarkerOptions().position(lastLocation).title(newlabel));
                    //ArrayList<Double> arr = new ArrayList<>();
                    //arr.add(oldLocation.getLatitude());
                    //arr.add(oldLocation.getLongitude());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mDatabase.addValueEventListener(postListener);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
    private void startGettingLocations() {

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPS = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetwork = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean canGetLocation = true;
        int ALL_PERMISSIONS_RESULT = 101;
        long MIN_DISTANCE_CHANGE_FOR_UPDATES = 100;// Distance in meters
        long MIN_TIME_BW_UPDATES = 1000 * 60;// Time in milliseconds

        ArrayList<String> permissions = new ArrayList<>();
        ArrayList<String> permissionsToRequest;

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsToRequest = findUnAskedPermissions(permissions);


        //Check if GPS and Network are on, if not asks the user to turn on
        if (!isGPS && !isNetwork) {
            showSettingsAlert();
        } else {
            // check permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (permissionsToRequest.size() > 0) {
                    requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                            ALL_PERMISSIONS_RESULT);
                    canGetLocation = false;
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Permission not Granted", Toast.LENGTH_SHORT).show();


            return;
        }

        //Starts requesting location updates
        if (canGetLocation) {
            if (isGPS) {
                lm.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

            } else if (isNetwork) {
                // from Network Provider

                lm.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

            }
        }
        else{
            Toast.makeText(this, "Can't get location", Toast.LENGTH_SHORT).show();
        }
    }
    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList result = new ArrayList();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }
    private boolean hasPermission(String permission) {
        if (canAskPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }
    private boolean canAskPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS is not Enabled!");
        alertDialog.setMessage("Do you want to turn on GPS?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        addMarker(latLng, "New Location");

        LocationData locationdata = new LocationData(location.getLatitude(),location.getLongitude());

        mDatabase = FirebaseDatabase.getInstance().getReference();
        timestamp = System.currentTimeMillis();
        String ts = String.valueOf(timestamp);
        DatabaseReference myRef = database.getReference("Locations");

        myRef.child(ts).setValue(locationdata);

    }

    // ...

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void addMarker(LatLng latLng, String title) {
        if (latLng == null) {
            return;
        }
        if (markerLocation != null) {
            markerLocation.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("New Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        if (mMap != null)
            markerLocation = mMap.addMarker(markerOptions);


        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latLng.latitude, latLng.longitude))
                .zoom(16)
                .build();

        if (mMap != null)
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
    private void getLocation() {

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Permission to access GPS denied", Toast.LENGTH_SHORT).show();

            LocationDialog.dismiss();
            return;
        }

        SingleShotLocationProvider.requestSingleUpdate(this,
                new SingleShotLocationProvider.LocationCallback() {
                    @Override
                    public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                        LatLng latLng = new LatLng(location.latitude, location.longitude);
                        //addMarker(latLng, "New Location");
                        LocationDialog.dismiss();
                    }
                });
    }


}