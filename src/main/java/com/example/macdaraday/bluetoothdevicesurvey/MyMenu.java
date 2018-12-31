package com.example.macdaraday.bluetoothdevicesurvey;


import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.companion.BluetoothDeviceFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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

public class MyMenu extends AppCompatActivity implements android.location.LocationListener  {

    private GoogleMap mMap;
    private Marker markerLocation;
    private ProgressDialog LocationDialog;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private long timestamp;
    private LocationData locationdata;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_menu);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }


        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();

        setContentView(R.layout.activity_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkLocationPermission();
        startGettingLocations();
        getLocation();



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
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mDatabase.addValueEventListener(postListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

   // @androidx.annotation.Nullable

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

        locationdata = new LocationData(location.getLatitude(),location.getLongitude());

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
    @Override
    public void onPause() {
        super.onPause();
        searchForDevices(null);
    }
    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    public void button1OnClick(View v) {

        Intent myIntent = new Intent(v.getContext(), MapsActivity.class);
        startActivityForResult(myIntent, 0);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        timestamp = System.currentTimeMillis();
        String ts = String.valueOf(timestamp);
        DatabaseReference myRef = database.getReference(ts);

        myRef.child(ts).setValue("Button 1");
        bluetoothonCreate();
    }
    public void button2OnClick(View v) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        timestamp = System.currentTimeMillis();
        String ts = String.valueOf(timestamp);
        DatabaseReference myRef = database.getReference(ts);

        myRef.child(ts).setValue("Button 2");

    }
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Permission to access GPS")
                        .setMessage("Please allow the app to access you location.")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MyMenu.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        99);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        99);
            }
            return false;
        } else {
            return true;
        }
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
                        LocationDialog.dismiss();
                    }
                });
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

    protected void bluetoothonCreate() {
        // Register for broadcasts when a device is discovered.
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                mDatabase = FirebaseDatabase.getInstance().getReference();
                timestamp = System.currentTimeMillis();
                String ts = String.valueOf(timestamp);
                DatabaseReference myRef = database.getReference(ts);

                myRef.child(ts).setValue(deviceName + deviceHardwareAddress);
            }
        }
    };
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }


    public void searchForDevices(Context context) {

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);


        mDatabase = FirebaseDatabase.getInstance().getReference();
        timestamp = System.currentTimeMillis();
        String ts = String.valueOf(timestamp);
        DatabaseReference myRef = database.getReference(ts);
        myRef.child(ts).setValue("Worked");

        Intent i = new Intent(this, MyBroadcastReceiver.class);

        PendingIntent sender = PendingIntent.getBroadcast(this,0, i, 0);


        AlarmManager am = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 6000,
                6000, sender);//15min interval

    }
}
