package com.paddy.robocontrol.robocontrol;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

public class DeviceAdapter extends BaseAdapter {

    final List<BluetoothDevice> mDevices;
    private final LayoutInflater mLayoutInflater;

    public DeviceAdapter(final Context context, final List<BluetoothDevice> devices) {
        mDevices = devices;
        mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return mDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.device_adapter, parent, false);
        }

        final TextView textViewDeviceName = (TextView) view.findViewById(R.id.deviceName);
        final TextView textViewDeviceAddress = (TextView) view.findViewById(R.id.deviceAddress);

        textViewDeviceName.setText(mDevices.get(position).getName());
        textViewDeviceAddress.setText(mDevices.get(position).getAddress());

        return view;
    }
}
