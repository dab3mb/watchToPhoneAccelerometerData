package com.example.accelv3;

import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;

// Imports needed for this specific app:
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends WearableActivity implements SensorEventListener {
    // Create our views/variables
    private TextView xvalue;
    private TextView yvalue;
    private TextView zvalue;
    private long timeofAction;
    private Button startButton;
    private DataClient dataClient;
    public boolean started = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Enables Always-on
        setAmbientEnabled();

        // View Objects
        xvalue = (TextView) findViewById(R.id.XVALUE);
        yvalue = (TextView) findViewById(R.id.YVALUE);
        zvalue = (TextView) findViewById(R.id.ZVALUE);


        // This is for communicating with phone
        dataClient = Wearable.getDataClient(this);



        // Accelerometer Sensor Code
        // https://developer.android.com/reference/android/widget/TextView & https://developer.android.com/reference/android/hardware/SensorManager
        SensorManager mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE); // This is kinda like the "mom" class, it allows us to access sensor related settings
        Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // This is our Accelerometer Sensor, the SensorManager assigns it
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL); // Our SensorManager then registers our class (MainActivity) as the listener!


        // Button code - https://developer.android.com/reference/android/widget/Button & https://stackoverflow.com/questions/11169360/android-remove-button-dynamically
        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                if (!started) {
                    //ViewGroup layout = (ViewGroup) startButton.getParent(); // Finds the current view group object button is in
                    //layout.removeView(startButton); // Removes the buttons
                    started = true;
                    startButton.setBackgroundColor(0000);
                    startButton.setText("\n \n \n Tap to stop recording");
                } else if (started){
                    sendDataToPhone();
                    started = false;

                }
            }
        });


    }
    List<String> arrayToSend = new ArrayList<String>();
    float timeOfPreviousAction = 0;
    @Override
    /**
     * When ever there is a change in values, this function gets called by the system
     * Changes our TextView objects to display our accel's x,y, and z values
     */
    public void onSensorChanged(SensorEvent event) {
        if (started) {
            //Log.d("TIMESTAMP", String.valueOf(timeofAction));
            timeofAction = event.timestamp;
            //if (timeofAction - timeOfPreviousAction >= ((float) 1)){ // Every millisecond?
            xvalue.setText(String.valueOf("X = " + (event.values[0]) + " m/s^2"));
            yvalue.setText(String.valueOf("Y = " + (event.values[1]) + " m/s^2"));
            zvalue.setText(String.valueOf("Z = " + (event.values[2]) + " m/s^2"));
            arrayToSend.add(String.valueOf(event.values[0]));
            arrayToSend.add(String.valueOf(event.values[1]));
            arrayToSend.add(String.valueOf(event.values[2]));
            arrayToSend.add(String.valueOf(timeofAction));
            //sendDataToPhone((float) event.values[0], (float) event.values[1], (float) event.values[2], timeofAction);
            //    timeOfPreviousAction = timeofAction;
            //}
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // We don't really care about this, we're only concerned with the data changing
    }


    // Sending Data To Phone
    // https://developer.android.com/training/wearables/data-layer/data-items.html
    // float x, float y, float z,float time
    public void sendDataToPhone() {
        //String[] arrayToSend = {String.valueOf(x),String.valueOf(y), String.valueOf(z), String.valueOf(time)};
        Log.d("DATA_SENT", "sendingDataToPhone... ");
        PutDataMapRequest dataRequest = PutDataMapRequest.create("/accelData"); // Make dataMap
        dataRequest.getDataMap().putStringArray("ThisIsTheKey", arrayToSend.toArray(new String[0])); // Put x,y,z in array
        PutDataRequest putDataReq = dataRequest.asPutDataRequest();
        putDataReq.setUrgent();
        Task<DataItem> putDataTask = dataClient.putDataItem(putDataReq);
    }
}
