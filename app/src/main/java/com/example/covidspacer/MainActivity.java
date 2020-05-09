package com.example.covidspacer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, BeaconConsumer {
    Switch broadcastSwitch = null;

    private BeaconManager beaconManager;

    protected static final String TAG = "MonitoringActivity";
    private String beacon_UUID = "DDDD98FF-2900-441A-802F-9C398FC1DDDD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        broadcastSwitch = (Switch) findViewById(R.id.broadcastSwitch);
        broadcastSwitch.setOnCheckedChangeListener(this);

        beaconManager = BeaconManager.getInstanceForApplication(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton _switch, boolean isEnabled){
        Context context = getApplicationContext();
        CharSequence text = "Toggled";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.removeAllRangeNotifiers();
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Log.i(TAG, "The first beacon I see is about "+beacons.iterator().next().getDistance()+" meters away.");
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region(beacon_UUID, null, null, null));
        } catch (RemoteException e) {    }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }
}
