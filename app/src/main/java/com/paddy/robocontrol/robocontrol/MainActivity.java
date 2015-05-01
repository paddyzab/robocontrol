package com.paddy.robocontrol.robocontrol;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends ActionBarActivity implements NewDevicesFoundListener, View.OnClickListener {

    private ImageButton imageButtonUp;
    private ImageButton imageButtonLeft;
    private ImageButton imageButtonRight;
    private ImageButton imageButtonDown;
    private Button buttonStop;
    private Button connectToDevice;
    private ToggleButton lightsButton;
    private TextView textViewConnectionStatus;

    private BluetoothManager bluetoothManager;
    private DiscoveryBroadcastMonitor discoveryBroadcastMonitor;

    private List<BluetoothDevice> deviceList = new ArrayList<>();

    private BluetoothDevice controlledDevice;
    private DeviceAdapter deviceAdapter;
    private BluetoothSocket transferSocket;

    private final static String UP = "1";
    private final static String LEFT = "4";
    private final static String RIGHT = "2";
    private final static String DOWN = "3";
    private final static String STOP = "0";

    private final static String LIGHTS_ON = "9";
    private final static String LIGHTS_OFF = "8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageButtonUp = (ImageButton) findViewById(R.id.imageButtonUp);
        imageButtonLeft = (ImageButton) findViewById(R.id.imageButtonLeft);
        imageButtonRight = (ImageButton) findViewById(R.id.imageButtonRight);
        imageButtonDown = (ImageButton) findViewById(R.id.imageButtonDown);
        connectToDevice = (Button) findViewById(R.id.buttonConnectToDevice);
        buttonStop = (Button) findViewById(R.id.buttonStop);
        textViewConnectionStatus = (TextView) findViewById(R.id.textViewConnectionStatus);

        imageButtonUp.setOnClickListener(this);
        imageButtonLeft.setOnClickListener(this);
        imageButtonRight.setOnClickListener(this);
        imageButtonDown.setOnClickListener(this);
        buttonStop.setOnClickListener(this);

        lightsButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sendMessage(transferSocket, LIGHTS_OFF);
                } else {
                    sendMessage(transferSocket, LIGHTS_ON);
                }
            }
        });

        connectToDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDevicesDialog();
            }
        });

        deviceAdapter = new DeviceAdapter(MainActivity.this, deviceList);

        bluetoothManager = new BluetoothManager();
        discoveryBroadcastMonitor = new DiscoveryBroadcastMonitor();
    }

    @Override
    protected void onResume() {
        super.onResume();

        bluetoothManager.prepareAdapter(MainActivity.this);
        registerReceiver(discoveryBroadcastMonitor, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(discoveryBroadcastMonitor, new IntentFilter(DiscoveryBroadcastMonitor.DISCOVERY_STARTED));
        registerReceiver(discoveryBroadcastMonitor, new IntentFilter(DiscoveryBroadcastMonitor.DISCOVERY_FINISHED));
        startDiscovery();

        connectToDevice.setEnabled(!deviceList.isEmpty());

        if (bluetoothManager.isBonded(controlledDevice)) {
            connectToSocket(controlledDevice);
        }
    }

    private void startDiscovery() {
        if (bluetoothManager.isAdapterEnabled() && bluetoothManager.isNotDiscovering()) {
            deviceList.clear();
            bluetoothManager.startDiscovery();
        }
    }

    @Override
    public void addNewDevice(BluetoothDevice remoteDevice) {
        deviceList.add(remoteDevice);
        deviceAdapter.notifyDataSetInvalidated();

        if (!connectToDevice.isEnabled()) {
            connectToDevice.setEnabled(true);
        }
    }

    private void showDevicesDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        dialogBuilder.setTitle("Select Device:");
        dialogBuilder.setNegativeButton("cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        dialogBuilder.setAdapter(deviceAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        textViewConnectionStatus.setText("Connected to: " + deviceAdapter.getItem(position).getName());
                        controlledDevice = deviceAdapter.getItem(position);
                        Log.d(MainActivity.class.getSimpleName(), "UUIDs " + controlledDevice.getUuids()[0]);

                        connectToSocket(controlledDevice);
                    }
                });
        dialogBuilder.show();
    }

    private void connectToSocket(final BluetoothDevice device) {
        final BluetoothSocket clientSocket;
        try {
            clientSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(device.getUuids()[0].toString()));
            transferSocket = clientSocket;
            clientSocket.connect();
        } catch (IOException e) {
            Log.e("BT COMM", "Bluetooth client IO exception: " + e);
        }
    }

    private void sendMessage(BluetoothSocket bluetoothSocket, String message) {
        final OutputStream outputStream;
        try {
            outputStream = bluetoothSocket.getOutputStream();
            outputStream.write(message.getBytes());
        } catch (IOException e) {
            Log.e("BT COMM", "Failed writing message on socket: " + e);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == imageButtonUp.getId()) {
            sendMessage(transferSocket, UP);
        }

        if (view.getId() == imageButtonRight.getId()) {
            sendMessage(transferSocket, RIGHT);
        }

        if (view.getId() == imageButtonLeft.getId()) {
            sendMessage(transferSocket, LEFT);
        }

        if (view.getId() == imageButtonDown.getId()) {
            sendMessage(transferSocket, DOWN);
        }

        if (view.getId() == buttonStop.getId()) {
            sendMessage(transferSocket, STOP);
        }
    }
}
