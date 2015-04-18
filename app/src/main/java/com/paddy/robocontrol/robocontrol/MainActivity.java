package com.paddy.robocontrol.robocontrol;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
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
import java.util.ArrayList;
import java.util.List;


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

    int UP = 1;
    int LEFT = 2;
    int RIGHT = 3;
    int DOWN = 4;

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

        if (!connectToDevice.isEnabled()) {
            connectToDevice.setEnabled(true);
        }
    }

    private void showDevicesDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
                MainActivity.this);
        final DeviceAdapter deviceAdapter = new DeviceAdapter(MainActivity.this, deviceList);

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
                    public void onClick(DialogInterface dialog, int which) {
                        textViewConnectionStatus.setText("Connected to: " + deviceAdapter.getItem(which).getName());
                    }
                });
        dialogBuilder.show();
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
            // send UP
        }

        if (viewId == imageButtonRight.getId()) {
            // send RIGHT
        }

        if (viewId == imageButtonLeft.getId()) {
            // send LEFT
        }

        if (viewId == imageButtonDown.getId()) {
            // send DOWN
        }
    }
}
