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

import com.example.android.main.sensor.SensorHandler;
import com.example.android.main.translater.MovementTranslator;
import com.example.android.main.translater.SimpleMovementTranslator;

public class MainActivity extends AppCompatActivity {
    private SensorHandler sensorHandler;
    private MovementTranslator translator;

    private TextView mTextStatus;

    private TextView mTextSensorYaw;
    private TextView mTextSensorPitch;
    private TextView mTextSensorRoll;
    private TextView mTextMoveStatus;
    private TextView mTextMoveDisplacement;

    private Button button;
    private Button left;
    private Button right;
    private Button forward;
    private Button back;
    private Button land;
    private Button takeOff;

    private UAV drone = new UAV(this);
    boolean UAVconnected = false;
    boolean UAVonAir = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //THIS METHOD IS CALLED EVERYTIME, for example killing app/change orientation => this is called again
        //hence it may be bad to put the following code here, need futher investigation

        super.onCreate(savedInstanceState);
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

        button = findViewById(R.id.button);

        left = findViewById(R.id.button_left);
        right = findViewById(R.id.button_right);
        forward = findViewById(R.id.button_forward);
        back = findViewById(R.id.button_back);
        land = findViewById(R.id.button_land);
        takeOff = findViewById(R.id.button_takeOff);

        // SensorHandler computing everything related to Sensors
        sensorHandler = new SensorHandler(this);

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
                        connectSuccess(); // temporary
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


    }
    /*private void exec(int i) {
        switch (i) {
            case 0: sensorHandler.calibrate(); mTextStatus.setText(TextArray[mIfCounter]);
                button.setText(ButtonTextArray[mIfCounter]); //takeoff();
            case 1: sensorHandler.resetOffset();  mTextStatus.setText(TextArray[mIfCounter]);
                button.setText(ButtonTextArray[mIfCounter]) //drone.back(30);
            case 2: sensorHandler.calibrate(); //drone.right(30); //land();
            case 3: sensorHandler.resetOffset(); //drone.forward(30);
            case 4: sensorHandler.calibrate(); //drone.left(30);
            case 5: sensorHandler.resetOffset(); //land();
        }
    }*/

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

        // Unregister all com.example.android.main.sensor listeners in this callback so they don't
        // continue to use resources when the app is stopped.
        sensorHandler.unregisterSensor();
    }

    public void update(Map<String, Float> sensorMap, MoveState moveState, float angle_diff) {
        this.updateSensorValue(sensorMap);
        this.updateMoveState(moveState);
        float displacement = translator.translate(moveState, angle_diff);
        this.updateDisplacement(displacement);

        if(UAVconnected && UAVonAir) {
            this.moveDrone(moveState, displacement);
        }
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

    public void connectError() {
        mTextStatus.setText("Connection failed");
        button.setText("Reconnect");
    }

    public void connectSuccess() {
        UAVconnected = true;
        mTextStatus.setText("Connection successful");
        button.setText("You shall...                      lift off!");
        button.setTag("connected");
    }
    private void takeoff() {
        drone.takeoff();
        sensorHandler.start();
        UAVonAir = true;
        mTextStatus.setText("See I'm flying");
        button.setText("Land");
        button.setTag("on air");
    }

    private void land() {
        drone.land();
        sensorHandler.stop();
        UAVonAir = false;
        mTextStatus.setText("I'm down to earth");
        button.setText("You shall rise...                  again!");
        button.setTag("connected");
    }

    public int getDroneYaw() {
        return drone.getYaw();
    }

    private void moveDrone(MoveState moveState, float displacement) {
        switch (moveState) {
            case GROUND: {}
            case FORWARD:
                drone.forward((int) displacement);
            case BACKWARD:
                drone.back((int) displacement);
            //case ROTATERIGHT: drone.rotRight((int) displacement);
            //case ROTATELEFT: drone.rotLeft((int) displacement);
            case LEFT:
                drone.left((int) displacement);
            case RIGHT:
                drone.right((int) displacement);
            default: {} //drone.hover();
        }
    }
}