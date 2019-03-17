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

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Map;

import com.example.android.main.sensor.DirectionDetector;
import com.example.android.main.translator.MovementTranslator;
import com.example.android.main.translator.SimpleMovementTranslator;

public class MainActivity extends AppCompatActivity {
    private static final int COMMAND_FREQUENCY = 1;
    private static Context context;

    private DirectionDetector directionDetector;
    private MovementTranslator translator;

    private static TextView mTextStatus;

    private TextView mTextSensorYaw;
    private TextView mTextSensorPitch;
    private TextView mTextSensorRoll;
    private TextView mTextMoveStatus;
    private TextView mTextMoveDisplacement;
    private static TextView mTextOther;

    private static Button button;
    private Button left;
    private Button right;
    private Button rot_left;
    private Button rot_right;
    private Button forward;
    private Button back;
    private Button land;
    private Button takeOff;

    private UAV drone = new UAV();
    static boolean UAVconnected = false;
    boolean UAVonAir = false;

    private int updateCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.context = getApplicationContext();
        setContentView(R.layout.activity_main);

        // Lock the orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mTextStatus = findViewById(R.id.label_status);

        // Link TextView to xml representation
        mTextSensorYaw = findViewById(R.id.value_yaw);
        mTextSensorPitch = findViewById(R.id.value_pitch);
        mTextSensorRoll = findViewById(R.id.value_roll);
        mTextMoveStatus = findViewById(R.id.label_movestate);
        mTextMoveDisplacement = findViewById(R.id.label_displacement);
        mTextOther = findViewById(R.id.label_other);

        button = findViewById(R.id.button);

        left = findViewById(R.id.button_left);
        right = findViewById(R.id.button_right);
        rot_left = findViewById(R.id.button_rotleft);
        rot_right = findViewById(R.id.button_rotright);
        forward = findViewById(R.id.button_forward);
        back = findViewById(R.id.button_back);
        land = findViewById(R.id.button_land);
        takeOff = findViewById(R.id.button_takeOff);

        // DirectionDetector computing everything related to Sensors
        directionDetector = new DirectionDetector(this);

        // Sensor to Movement Transformer
        translator = new SimpleMovementTranslator(this,0.5f);

        // Single Button
        button.setText("Connect");
        button.setTag("connect");
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String status = (String) v.getTag();
                switch(status) {
                    case "connect":
                        mTextStatus.setText("Connecting...");
                        drone.connect();
                        break;
                    case "connected": takeoff(); break;
                    case "on air": land(); break;
                    }
                }
            }
        );

        left.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drone.left(30);
            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drone.right(30);
            }
        });

        rot_left.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drone.rotLeft(90);
            }
        });

        rot_right.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drone.rotRight(90);
            }
        });

        forward.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drone.forward(30);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drone.back(30);
            }
        });

        land.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                land();
            }
        });

        takeOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                takeoff();
            }
        });

        mTextOther.setText(String.valueOf(this.getDroneYaw()));
    }

    public static Context getAppContext() {
        return MainActivity.context;
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

        directionDetector.registerSensor();
    }

    @Override
    protected void onStop() {
        //====== LAND THE UAV ======

        //drone.land();

        super.onStop();

        // Unregister all com.example.android.main.sensor listeners in this callback so they don't
        // continue to use resources when the app is stopped.
        directionDetector.unregisterSensor();
    }

    public void update(Map<String, Float> sensorMap, MoveState moveState, float angle_diff) {
        this.updateSensorValue(sensorMap);
        this.updateMoveState(moveState);
        float displacement = translator.translate(moveState, angle_diff);
        this.updateDisplacement(displacement);

        if(UAVconnected && UAVonAir) {
            this.moveDrone(moveState, displacement);
            //this.updateCounter = 0;
        }

        //updateCounter = (updateCounter+1)%COMMAND_FREQUENCY;
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

    public void updateOther(String string) {
        mTextOther.setText(string);
    }

    public static void connectError() {
        mTextStatus.setText("Connection failed");
        button.setText("Reconnect");
        Log.i("UAV:", "connection failed");
    }

    public static void connectSuccess() {
        UAVconnected = true;
        mTextStatus.setText("Connection successful");
        button.setText("You shall...                      lift off!");
        button.setTag("connected");
    }
    private void takeoff() {
        drone.takeoff();
        directionDetector.start();
        UAVonAir = true;
        mTextStatus.setText("See I'm flying");
        button.setText("Land");
        button.setTag("on air");
    }

    private void land() {
        drone.land();
        directionDetector.stop();
        UAVonAir = false;
        mTextStatus.setText("I'm down to earth");
        button.setText("You shall rise...                  again!");
        button.setTag("connected");
    }

    public int getDroneYaw() {
        return drone.getYaw();
    }

    private void moveDrone(MoveState moveState, float displacement) {
        if (!drone.isBusy()) {
            drone.setBusy(true);
            drone.move(moveState, displacement);
        }
    }

    public static TextView getmTextOther() {
        return mTextOther;
    }
}