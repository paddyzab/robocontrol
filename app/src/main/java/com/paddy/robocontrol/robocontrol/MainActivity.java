package com.paddy.robocontrol.robocontrol;

import android.bluetooth.BluetoothDevice;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements NewDevicesFoundListener {

    ImageButton imageButtonUp;
    ImageButton imageButtonLeft;
    ImageButton imageButtonRight;
    ImageButton imageButtonDown;
    Button connectToDevice;

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

        imageButtonUp.setOnTouchListener(printingOnTouchListener);
        imageButtonLeft.setOnTouchListener(printingOnTouchListener);
        imageButtonRight.setOnTouchListener(printingOnTouchListener);
        imageButtonDown.setOnTouchListener(printingOnTouchListener);

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

        Log.d(MainActivity.class.getSimpleName(), "new bt device: " + remoteDevice.getName());
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
