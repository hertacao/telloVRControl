/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.tiltspot;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // System sensor manager instance.
    private SensorManager mSensorManager;

    // Accelerometer and magnetometer sensors, as retrieved from the sensor manager.
    private Sensor mSensorAccelerometer;
    private Sensor mSensorMagnetometer;
    private String sensorName;

    private float[] mAccelerometerData = new float[3];
    private float[] mMagnetometerData = new float[3];
    private float azimuthOffset;
    private float azimuth;
    private float pitch;
    private float roll;

    private boolean start = true;
    private State state;

    private TextView mTextSensorAzimuth;
    private TextView mTextSensorPitch;
    private TextView mTextSensorRoll;
    private TextView mTextSensor;

    private UAVInteraction drone;
    private ConnErrorDialog connErrorMessage;

    // Very small values for the accelerometer (on all three axes) should
    // be interpreted as 0. This value is the amount of acceptable
    // non-zero drift.
    private static final float VALUE_DRIFT = 0.05f;
    private static final float HYSTERESIS = 0.05f;
    private static final float OFFSETFROMMIDDLE = 0.2f;

    protected enum State {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT,
        ROTATELEFT,
        ROTATERIGHT,
        HOVER
    }

    //methods


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            drone = new UAVInteraction();
            state = State.HOVER;
        } catch (Exception e) {
            //idea of this is to close the app if we dont have connection
            connErrorMessage = new ConnErrorDialog();
            //TO DO:    close app
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Lock the orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mTextSensorAzimuth = (TextView) findViewById(R.id.value_azimuth);
        mTextSensorPitch = (TextView) findViewById(R.id.value_pitch);
        mTextSensorRoll = (TextView) findViewById(R.id.value_roll);
        mTextSensor = (TextView) findViewById(R.id.label_sensor);

        // Get accelerometer and magnetometer sensors from the sensor manager.
        // The getDefaultSensor() method returns null if the sensor
        // is not available on the device.
        mSensorManager = (SensorManager) getSystemService(
                Context.SENSOR_SERVICE);

        mSensorAccelerometer = mSensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);
        mSensorMagnetometer = mSensorManager.getDefaultSensor(
                Sensor.TYPE_MAGNETIC_FIELD);

        //mGyroscope = mSensorManager.getDefaultSensor(TYPE_GYROSCOPE);
    }


    /**
     * Listeners for the sensors are registered in this callback so that
     * they can be unregistered in onStop().
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Listeners for the sensors are registered in this callback and
        // can be unregistered in onStop().
        //
        // Check to ensure sensors are available before registering listeners.
        // Both listeners are registered with a "normal" amount of delay
        // (SENSOR_DELAY_NORMAL).
        if (mSensorAccelerometer != null) {
            mSensorManager.registerListener(this, mSensorAccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorMagnetometer != null) {
            mSensorManager.registerListener(this, mSensorMagnetometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        //TO DO: minimalize the app


    }

    @Override
    protected void onStop() {

        //====== LAND THE UAV ======

        drone.land();

        super.onStop();

        // Unregister all sensor listeners in this callback so they don't
        // continue to use resources when the app is stopped.
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        int sensorType = sensorEvent.sensor.getType();
        //sensorName = sensorEvent.sensor.getName();

        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                mAccelerometerData = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagnetometerData = sensorEvent.values.clone();
                break;
            default:
                //mTextSensor.setText(sensorName);
                return;
        }

        float[] rotationMatrix = new float[9];
        boolean rotationOK = SensorManager.getRotationMatrix(rotationMatrix,
                null, mAccelerometerData, mMagnetometerData);

        float orientationValues[] = new float[3];
        if (rotationOK) {
            SensorManager.getOrientation(rotationMatrix, orientationValues);
        }

        if (start) {
            azimuthOffset = orientationValues[0];
            start = false;
        }

        azimuth = orientationValues[0] - azimuthOffset;
        pitch = orientationValues[1];
        roll = Math.abs(orientationValues[2]);

        mTextSensorAzimuth.setText(getResources().getString(R.string.value_format, azimuth));
        mTextSensorPitch.setText(getResources().getString(R.string.value_format, pitch));
        mTextSensorRoll.setText(getResources().getString(R.string.value_format, roll));


        //PITCH 0,3

        processData();
    }

    private void processData() {
        //TO DO: yet to implement rotation
        if (state == State.HOVER) {
            if (roll < (1.57 - OFFSETFROMMIDDLE)) {
                drone.forward();
                mTextSensor.setText("forward");
                state = State.FORWARD;

            } else if (roll > (1.57 + OFFSETFROMMIDDLE)) {
                drone.backward();
                mTextSensor.setText("backward");
                state = State.BACKWARD;

            } else if (pitch < -OFFSETFROMMIDDLE) {
                drone.right();
                mTextSensor.setText("right");
                state = State.RIGHT;

            } else if (pitch > OFFSETFROMMIDDLE) {
                drone.left();
                mTextSensor.setText("left");
                state = State.LEFT;

            } else {
                drone.hover();
                mTextSensor.setText("hover");
                state = State.HOVER;

            }
        } else { //HYSTERESIS

            if (roll < (1.57 - OFFSETFROMMIDDLE - HYSTERESIS)) {
                drone.forward();
                mTextSensor.setText("forward");
                state = State.FORWARD;

            } else if (roll > (1.57 + OFFSETFROMMIDDLE - HYSTERESIS)) {
                drone.backward();
                mTextSensor.setText("backward");
                state = State.BACKWARD;

            } else if (pitch < (-OFFSETFROMMIDDLE - HYSTERESIS)) {
                drone.right();
                mTextSensor.setText("right");
                state = State.RIGHT;

            } else if (pitch > (OFFSETFROMMIDDLE - HYSTERESIS)) {
                drone.left();
                mTextSensor.setText("left");
                state = State.LEFT;

            }else{
                drone.hover();
                mTextSensor.setText("hover");
                state = State.HOVER;
            }

        }

    }

    /**
     * Must be implemented to satisfy the SensorEventListener interface;
     * unused in this app.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}