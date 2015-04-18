package com.paddy.robocontrol.robocontrol;

import android.bluetooth.BluetoothDevice;

public interface NewDevicesFoundListener {

    void addNewDevice(final BluetoothDevice remoteDevice);
}
