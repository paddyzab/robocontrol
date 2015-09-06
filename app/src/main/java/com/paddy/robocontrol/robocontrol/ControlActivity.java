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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ControlActivity extends ActionBarActivity implements NewDevicesFoundListener, View.OnClickListener {

    private ImageButton imageButtonUp;
    private ImageButton imageButtonLeft;
    private ImageButton imageButtonRight;
    private ImageButton imageButtonDown;
    private Button buttonStop;
    private Button connectToDevice;
    private TextView textViewConnectionStatus;

    private BluetoothManager bluetoothManager;
    private DiscoveryBroadcastMonitor discoveryBroadcastMonitor;

    private List<BluetoothDevice> deviceList = new ArrayList<>();

    private BluetoothDevice controlledDevice;
    private DeviceAdapter deviceAdapter;
    private BluetoothSocket transferSocket;

    private final static String UP = "W";
    private final static String LEFT = "A";
    private final static String RIGHT = "D";
    private final static String DOWN = "S";
    private final static String STOP = "X";

    private final static String LASER_ON = "L";
    private final static String LASER_OFF = "Q";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_activity);

        final ToggleButton lightsButton = (ToggleButton) findViewById(R.id.buttonLights);
        final SeekBar progressBarSpeed = (SeekBar) findViewById(R.id.progressBarSpeed);

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

        progressBarSpeed.setOnSeekBarChangeListener(seekBarChangeListener);

        lightsButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sendMessage(transferSocket, LASER_OFF);
                } else {
                    sendMessage(transferSocket, LASER_ON);
                }
            }
        });

        connectToDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDevicesDialog();
            }
        });

        deviceAdapter = new DeviceAdapter(ControlActivity.this, deviceList);

        bluetoothManager = new BluetoothManager();
        discoveryBroadcastMonitor = new DiscoveryBroadcastMonitor();
    }

    @Override
    protected void onResume() {
        super.onResume();

        bluetoothManager.prepareAdapter(ControlActivity.this);
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
                ControlActivity.this);

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
                        Log.d(ControlActivity.class.getSimpleName(), "UUIDs " + controlledDevice.getUuids()[0]);

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

        if (bluetoothSocket != null) {
            final OutputStream outputStream;
            try {
                outputStream = bluetoothSocket.getOutputStream();
                outputStream.write(message.getBytes());
            } catch (IOException e) {
                Log.e("BT COMM", "Failed writing message on socket: " + e);
            }
        } else {
            Toast.makeText(this, "Connect to the Robot first...", Toast.LENGTH_LONG).show();
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

    private final SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        int selectedSpeed;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            selectedSpeed = progress;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // nop
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            sendMessage(transferSocket, String.valueOf(selectedSpeed));
        }
    };
}
