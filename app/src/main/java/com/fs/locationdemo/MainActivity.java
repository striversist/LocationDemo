package com.fs.locationdemo;

import android.Manifest;
import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.RxPermissions;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "aaa";
    private TextView mInfoTextView;
    private LocationHelper mLocationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInfoTextView = findViewById(R.id.info_tv);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.get_perms_btn:
                requestPermissions();
                break;
            case R.id.open_settings_btn:
                Utils.openLocationSettings(this);
                break;
            case R.id.start_get_loc_btn:
                if (!Utils.isLocationPermissionGranted(this)) {
                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!Utils.isLocationProviderEnabled(this)) {
                    Toast.makeText(this, "Provider not enabled", Toast.LENGTH_LONG).show();
                    return;
                }
                initLocation();
                break;
            case R.id.stop_get_loc_btn:
                if (mLocationHelper != null) {
                    mLocationHelper.removeLocationUpdatesListener();
                }
                break;
        }
    }

    public void requestPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
        };
        new RxPermissions(this)
                .request(permissions)//访问手机状态权限
                .subscribe(aBoolean -> {
                    boolean fine = Utils.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
                    Log.d(TAG, String.format("aBoolean=%b, fine=%b", aBoolean, fine));
                    boolean isGpsOpen = Utils.isGpsOpen(this);
                    boolean isNetworkOpen = Utils.isNetworkOpen(this);
                    Toast.makeText(this, String.format("isGpsOpen=%b, isNetworkOpen=%b", isGpsOpen, isNetworkOpen), Toast.LENGTH_LONG).show();
                });
    }

    private void initLocation() {
        mLocationHelper = new LocationHelper(this);
        mLocationHelper.initLocation(new MyLocationListener() {
            @Override
            public void updateLastLocation(Location location) {
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                Toast.makeText(MainActivity.this, String.format("updateLastLocation: %f, %f", longitude, latitude), Toast.LENGTH_SHORT).show();
                mInfoTextView.setText(JsonUtils.getCommLocation(MainActivity.this).toJSONString());
            }

            @Override
            public void updateLocation(Location location) {
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                Toast.makeText(MainActivity.this, String.format("updateLocation: %f, %f", longitude, latitude), Toast.LENGTH_SHORT).show();
                mInfoTextView.setText(JsonUtils.getCommLocation(MainActivity.this).toJSONString());
            }

            @Override
            public void updateStatus(String provider, int status, Bundle extras) {
                Toast.makeText(MainActivity.this, String.format("updateStatus: provider=%s, status=%d", provider, status), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void updateGpsStatus(GpsStatus gpsStatus) {

            }
        });
    }

    private void getCurrentLocation(String provider) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!Utils.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(MainActivity.this, "request permission failed", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(MainActivity.this, "requestLocationUpdates", Toast.LENGTH_SHORT).show();
        locationManager.requestLocationUpdates(provider, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LogUtil.e("locationManager onLocationChanged");
                String currentPosition = "latitude is " + location.getLatitude() + "\n" + "longitude is " + location.getLongitude();
                LogUtil.e("currentPosition: " + currentPosition);
                Toast.makeText(MainActivity.this, "currentPosition: " + currentPosition, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                LogUtil.e("locationManager onStatusChanged");
                Toast.makeText(MainActivity.this, "status: " + status, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderEnabled(String provider) {
                LogUtil.e("locationManager onProviderEnabled");
                Toast.makeText(MainActivity.this, "onProviderEnabled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                LogUtil.e("locationManager onProviderDisabled");
                Toast.makeText(MainActivity.this, "onProviderDisabled", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
