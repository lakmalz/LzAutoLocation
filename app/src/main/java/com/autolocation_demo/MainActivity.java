package com.autolocation_demo;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int ACCESS_FINE_LOCATION_INTENT_ID = 3;
    private TextView gps_status;
    private TextView location;
    private LzGEOAPIClient mGeoapiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gps_status = (TextView) findViewById(R.id.gps_status);
        location = (TextView) findViewById(R.id.location);
        initGPSClient();
    }

    private void initGPSClient() {
        mGeoapiClient = new LzGEOAPIClient(this, MainActivity.this, new LzGEOAPIClient.GEOAPIClientCallBack() {
            @Override
            public void onStartResolutionForResult(Status status) {
                try {
                    status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onUpdateGPSStatus(boolean gpsEnable) {
                updateGPSStatus(gpsEnable);
            }

            @Override
            public void onLocationUpdate(Location _location) {
                location.setText(_location.getLatitude()+"/"+_location.getLongitude());
            }
        });
        mGeoapiClient.initGoogleAPIClient();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK:
                        mGeoapiClient.updateLocation(LocationManager.GPS_PROVIDER);
                        updateGPSStatus(true);
                        //startLocationUpdates();
                        break;
                    case RESULT_CANCELED:
                        updateGPSStatus(false);
                        break;
                }
                break;
        }
    }

    private void updateGPSStatus(boolean gpsEnable) {
        String status = "Location Permission denied.";
        if (gpsEnable) {
            status = "GPS is Enabled in your device";
        } else {
            status = "GPS is Disabled in your device";
        }
        gps_status.setText(status);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ACCESS_FINE_LOCATION_INTENT_ID: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //If permission granted show location dialog if APIClient is not null
                    if (mGeoapiClient == null) {
                        mGeoapiClient.initGoogleAPIClient();
                        mGeoapiClient.showSettingDialog();
                    } else
                        mGeoapiClient.showSettingDialog();

                } else {
                    updateGPSStatus(false);
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGeoapiClient.registerGPSBroadCastReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGeoapiClient.unregisterGPSBroadCastReceiver();
    }
}


