package com.paddy.robocontrol.robocontrol;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.widget.Toast;

public class BluetoothManager {

    final BluetoothAdapter adapter;

    public BluetoothManager() {
        adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void prepareAdapter(Activity context) {
        if (adapter.isEnabled()) {
            adapter.startDiscovery();
        } else {
            if (adapter.enable()) {
                Toast.makeText(context, "BT enabled.", Toast.LENGTH_LONG).show();
            }
            adapter.startDiscovery();
        }
    }

    public boolean isAdapterEnabled() {
        return adapter.isEnabled();
    }

    public boolean isNotDiscovering() {
        return !adapter.isDiscovering();
    }

    public void startDiscovery() {
        adapter.startDiscovery();
    }
}
