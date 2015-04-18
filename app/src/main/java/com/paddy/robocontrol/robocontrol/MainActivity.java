package com.paddy.robocontrol.robocontrol;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;


public class MainActivity extends ActionBarActivity {

    ImageButton imageButtonUp;
    ImageButton imageButtonLeft;
    ImageButton imageButtonRight;
    ImageButton imageButtonDown;

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

        imageButtonUp.setOnTouchListener(printingOnTouchListener);
        imageButtonLeft.setOnTouchListener(printingOnTouchListener);
        imageButtonRight.setOnTouchListener(printingOnTouchListener);
        imageButtonDown.setOnTouchListener(printingOnTouchListener);
    }

    View.OnTouchListener printingOnTouchListener = new View.OnTouchListener() {

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
