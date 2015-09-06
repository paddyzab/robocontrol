package com.paddy.robocontrol.robocontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class DiscoveryBroadcastMonitor extends BroadcastReceiver {

    public static final String DISCOVERY_STARTED = BluetoothAdapter.ACTION_DISCOVERY_STARTED;
    public static final String DISCOVERY_FINISHED = BluetoothAdapter.ACTION_DISCOVERY_FINISHED;
    public static final String DISCOVERY_SUCCESS = BluetoothDevice.ACTION_FOUND;

    NewDevicesFoundListener listener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DISCOVERY_STARTED.equals(intent.getAction())) {
            Toast.makeText(context, "looking for robots", Toast.LENGTH_LONG).show();
        } else if (DISCOVERY_FINISHED.equals(intent.getAction())) {
            Toast.makeText(context, "finished...", Toast.LENGTH_LONG).show();
        } else if (DISCOVERY_SUCCESS.equals(intent.getAction())) {
            final BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (context instanceof NewDevicesFoundListener) {
                listener = ((ControlActivity) context);
            }

            if (listener != null) {
                listener.addNewDevice(remoteDevice);
            }
        }
    }
}
