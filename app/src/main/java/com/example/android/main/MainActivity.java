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

package com.example.android.main;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Map;

import sensor.SensorHandler;


public class MainActivity extends AppCompatActivity {
    private SensorHandler sensorHandler;
    private SensorMovementTransformer transformer;

    private TextView mTextSensorYaw;
    private TextView mTextSensorPitch;
    private TextView mTextSensorRoll;
    private TextView mTextMoveStatus;
    private TextView mTextMoveDisplacement;

    private Button button;

    private UAV drone = new UAV();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //THIS METHOD IS CALLED EVERYTIME, for example killing app/change orientation => this is called again
        //hence it may be bad to put the following code here, need futher investigation

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Lock the orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Link TextView to xml representation
        mTextSensorYaw = findViewById(R.id.value_yaw);
        mTextSensorPitch = findViewById(R.id.value_pitch);
        mTextSensorRoll = findViewById(R.id.value_roll);
        mTextMoveStatus = findViewById(R.id.label_movestate);
        mTextMoveDisplacement = findViewById(R.id.label_displacement);

        button = findViewById(R.id.button);

        // SensorHandler computing everything related to Sensors
        sensorHandler = new SensorHandler(this);

        // Sensor to Movement Transformer
        transformer = new SensorMovementTransformer(0.3f);

        // Single Button
        button.setTag("Connect");
        button.setText("Connect");
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String status = (String) v.getTag();
                try {
                    drone.connect();
                    sensorHandler.calibrate();

                    /*if (status.equals("Connect")) {
                        drone.connect();
                        sensorHandler.calibrate();
                        button.setText("You shall...                      lift off!");
                        v.setTag("Connected");
                    } else if (status.equals("Connected")) {
                        takeoff();
                        button.setText("Land");
                        v.setTag("Up");
                    } else if (status.equals("Up")) {
                        land();
                        button.setText("You shall rise...                  again!");
                        v.setTag("Connected");
                    }*/
                } catch (Exception e) {
                    button.setText("Reconnect");
                    //idea of this is to close the app (or something..) if we dont have connection
                    System.out.println(e.getMessage());
                    //connErrorMessage = new ConnErrorDialog();
                    //TO DO:    close app
                }
            }
        });
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

        sensorHandler.registerSensor();
    }

    @Override
    protected void onStop() {
        //====== LAND THE UAV ======

        //drone.land();

        super.onStop();

        // Unregister all sensor listeners in this callback so they don't
        // continue to use resources when the app is stopped.
        sensorHandler.unregisterSensor();
    }

    public void update(Map<String, Float> sensorMap, MoveState moveState, float angle_diff) {
        this.updateSensorValue(sensorMap);
        this.updateMoveState(moveState);
        float displacement = transformer.transform(moveState, angle_diff);
        this.updateDisplacement(displacement);

        //this.moveDrone(moveState, displacement);
    }

    private void updateSensorValue(Map<String, Float> sensorMap) {
        mTextSensorYaw.setText(getResources().getString(R.string.value_format, sensorMap.get("yaw")));
        mTextSensorPitch.setText(getResources().getString(R.string.value_format, sensorMap.get("pitch")));
        mTextSensorRoll.setText(getResources().getString(R.string.value_format, sensorMap.get("roll")));
    }

    private void updateMoveState(MoveState moveState) {
        mTextMoveStatus.setText(moveState.toString());
    }

    private void updateDisplacement(float displacement) {
        mTextMoveDisplacement.setText(String.valueOf(displacement));
    }

    private void takeoff() {
        drone.takeoff();
        sensorHandler.start();
    }

    private void land() {
        drone.land();
        sensorHandler.stop();
    }

    private void moveDrone(MoveState moveState, float displacement) {
        switch (moveState) {
            case FORWARD:
                drone.forward((int) displacement);
            case BACKWARD:
                drone.backward((int) displacement);
                //case ROTATERIGHT: drone.rotRight();
                //case ROTATELEFT: drone.rotLeft();
            case LEFT:
                drone.left((int) displacement);
            case RIGHT:
                drone.right((int) displacement);
        }
    }
}