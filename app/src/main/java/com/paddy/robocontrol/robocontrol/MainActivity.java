package com.paddy.robocontrol.robocontrol;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends ActionBarActivity implements NewDevicesFoundListener {

    ImageButton imageButtonUp;
    ImageButton imageButtonLeft;
    ImageButton imageButtonRight;
    ImageButton imageButtonDown;
    Button connectToDevice;
    TextView textViewConnectionStatus;

    BluetoothManager bluetoothManager;
    DiscoveryBroadcastMonitor discoveryBroadcastMonitor;

    List<BluetoothDevice> deviceList = new ArrayList<>();

    BluetoothDevice controledDevice;
    DeviceAdapter deviceAdapter;
    BluetoothSocket transferSocket;

    String UP = "1";
    String LEFT = "2";
    String RIGHT = "3";
    String DOWN = "4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageButtonUp = (ImageButton) findViewById(R.id.imageButtonUp);
        imageButtonLeft = (ImageButton) findViewById(R.id.imageButtonLeft);
        imageButtonRight = (ImageButton) findViewById(R.id.imageButtonRight);
        imageButtonDown = (ImageButton) findViewById(R.id.imageButtonDown);
        connectToDevice = (Button) findViewById(R.id.buttonConnectToDevice);
        textViewConnectionStatus = (TextView) findViewById(R.id.textViewConnectionStatus);

        imageButtonUp.setOnTouchListener(printingOnTouchListener);
        imageButtonLeft.setOnTouchListener(printingOnTouchListener);
        imageButtonRight.setOnTouchListener(printingOnTouchListener);
        imageButtonDown.setOnTouchListener(printingOnTouchListener);

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

        if (bluetoothManager.isBonded(controledDevice)) {
            connectToSocket(controledDevice);
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
                        controledDevice = deviceAdapter.getItem(position);
                        Log.d(MainActivity.class.getSimpleName(), "UUIDs " + controledDevice.getUuids()[0]);

                        connectToSocket(controledDevice);
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

    final View.OnTouchListener printingOnTouchListener = new View.OnTouchListener() {

        Rect rect = null;

        @Override
        public boolean onTouch(View view, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                rect = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                Log.d(MainActivity.class.getSimpleName(), "++++ Started: " + view.getId());
                notifyMoveTranslator(view.getId());

            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (rect != null) {
                    if (!rect.contains(view.getLeft() + (int) event.getX(), view.getTop() + (int) event.getY())) {
                        Log.d(MainActivity.class.getSimpleName(), "---- Exit: " + view.getId());
                        //Touch left view boundaries do not notify
                    } else {
                        Log.d(MainActivity.class.getSimpleName(), "++++ InView: " + view.getId());
                        notifyMoveTranslator(view.getId());
                    }
                }
            }
            return false;
        }
    };

    //TODO here we will send event to the Robot
    private void notifyMoveTranslator(int viewId) {

        if (viewId == imageButtonUp.getId()) {
            sendMessage(transferSocket, UP);
        }

        if (viewId == imageButtonRight.getId()) {
            sendMessage(transferSocket, RIGHT);
        }

        if (viewId == imageButtonLeft.getId()) {
            sendMessage(transferSocket, LEFT);
        }

        if (viewId == imageButtonDown.getId()) {
            sendMessage(transferSocket, DOWN);
        }
    }
}
