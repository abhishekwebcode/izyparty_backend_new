package com.izyparty.invitation.templates;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.izyparty.invitation.R;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivitySelectPrebuilt extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    public LatLng point;
    public LocationManager locationManager;
    CameraUpdate temPcameraUpdate;
    LatLng latLngExisting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_select_prebuilt);
        Boolean ahead=true;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (getIntent().hasExtra("latitude")) {
            if (!("".equals(getIntent().getStringExtra("latitude")))) {
                ahead=false;
                Log.d("LATITUDE", "onCreate: "+getIntent().getStringExtra("latitude"));
                Log.d("LONGITUDE", "onCreate: "+getIntent().getStringExtra("longitude"));
                latLngExisting = new LatLng(Double.parseDouble(getIntent().getStringExtra("latitude")), Double.parseDouble(getIntent().getStringExtra("longitude")));
                 temPcameraUpdate = CameraUpdateFactory.newLatLngZoom(latLngExisting, 15);
                //mMap.animateCamera(cameraUpdate);
            }
        }
        if (!ahead) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            String[] k = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this,k,22);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 1L, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                mMap.animateCamera(cameraUpdate);
                locationManager.removeUpdates(this);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });
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
        if (temPcameraUpdate!=null) {
            mMap.animateCamera(temPcameraUpdate);
            temPcameraUpdate=null;
            LatLng point = latLngExisting;
            MapsActivitySelectPrebuilt.this.point=point;
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(point));
        }
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                MapsActivitySelectPrebuilt.this.point=point;
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(point));
            }
        });
    }

    public void confirm(View v) {
        if (point==null) {
            Toast.makeText(this, getString(R.string.no_point_seletced), Toast.LENGTH_SHORT).show();
            return;
        }
        setResult(RESULT_OK,new Intent().putExtra("latitude", String.valueOf(point.latitude)).putExtra("longitude", String.valueOf(point.longitude)));
        finish();
    }

    public void cancel(View v) {setResult(RESULT_CANCELED);finish();}


}
