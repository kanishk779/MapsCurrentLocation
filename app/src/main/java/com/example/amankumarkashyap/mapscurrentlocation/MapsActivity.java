package com.example.amankumarkashyap.mapscurrentlocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.pm.PermissionInfoCompat;
import android.support.v4.net.ConnectivityManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private int locationRequestCode = 100;
    private double wayLatitude,wayLongitude;
    private MarkerOptions markerOptions;
    private LatLng latLng;
    private static final String TAG = MapsActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();
        markerOptions = new MarkerOptions();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if(!isNetworkAvailable(this))
        {
            Toast.makeText(this, "Open Your internet Connection", Toast.LENGTH_SHORT).show();
        }
        /**
         * calling the below method is not actually causing the apps.
         */
//        pingForGPS();
        locationCallback = new LocationCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        wayLatitude = location.getLatitude();
                        wayLongitude = location.getLongitude();
                        mMap.setBuildingsEnabled(true);
                        latLng = new LatLng(wayLatitude,wayLongitude);
                        /**
                         all markers, overlays, and polylines from the map are cleared when we use clear on google Maps
                         */
                        mMap.clear();
                        mMap.addMarker(markerOptions.position(latLng).title("Location in LocationResult"));
                        mMap.setMaxZoomPreference(20);
                        mMap.setMinZoomPreference(12);
                        mMap.setMyLocationEnabled(true);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                Intent intent = new Intent(getApplicationContext(),DetailActivity.class);
                                startActivity(intent);
                            }
                        });

                    }
                }
            }
        };
    }
    private void pingForGPS()
    {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5 * 1000);
        locationRequest.setFastestInterval(2 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,new LocationCallback(){
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                for (Location location : locationResult.getLocations()) {
                                    //Do what you want with location
                                    //like update camera
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16f));
                                }

                            }
                        },null);

                    }
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(MapsActivity.this, 2001);
                                break;
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.

                            break;
                    }
                }}


        });
    }
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return (activeNetwork != null && activeNetwork.isConnected());
        } else {
            return false;
        }
    }

    protected synchronized  void buildGoogleApiClient()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(30*1000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);

        }
        else
        {
            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, null);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG,"Connection Suspended "+ Integer.toString(i));

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode)
        {
            case 100:
            {
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if(location == null)
                            {
                                Toast.makeText(MapsActivity.this, "Please Check your Connection and turn on gps", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                wayLatitude = location.getLatitude();
                                wayLongitude = location.getLongitude();
                                mMap.setBuildingsEnabled(true);
                                latLng = new LatLng(wayLatitude,wayLongitude);
                                /**
                                 all markers, overlays, and polylines from the map are cleared when we use clear on google Maps
                                 */
                                mMap.clear();
                                mMap.addMarker(markerOptions.position(latLng).title("Your Last Location").snippet("YOur Last Location"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG , "Connection Failed ...."+connectionResult.getErrorMessage());
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG,"hitting the onLocationChanged");
        wayLatitude = location.getLatitude();
        wayLongitude = location.getLongitude();
        latLng = new LatLng(wayLatitude,wayLongitude);
        /**
         all markers, overlays, and polylines from the map are cleared when we use clear on google Maps
         */
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title("Your Location"));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if(status == LocationProvider.OUT_OF_SERVICE)
        {
            Toast.makeText(this, "OUT OF SERVICE check connection", Toast.LENGTH_SHORT).show();
        }
        else if(status == LocationProvider.TEMPORARILY_UNAVAILABLE)
        {
            Toast.makeText(this, "TEMPORARY UNAVAILABLE", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        /**
         * Do nothing as it is not something to which we would like to react towards
         */

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Provider "+provider+" has been disabled", Toast.LENGTH_SHORT).show();
    }
}
