package com.example.android.main.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RotationVector implements SensorStrategy {
    // Accelerometer and magnetometer sensors, as retrieved from the com.example.android.main.sensor manager.
    private Sensor mSensorRotationVector;

    // Sensor Values, Rotation vector component along the x,y,z axis (x,y,z * sin(θ/2)), unitless, -1.0 to +1.0
    private float[] mRotationVectorData = new float[4];

    // SensorMap
    private Map<String, Float> sensorMap = new HashMap<>();

    public RotationVector(SensorManager mSensorManager) {
        this.mSensorRotationVector = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ROTATION_VECTOR);
    }

    public List<Sensor> getSensor() {
        return Arrays.asList(mSensorRotationVector);
    }

    @Override
    public Map<String, Float> onSensorChanged(SensorEvent sensorEvent) {
        // Check to ensure sensors are available before registering listeners.
        // Both listeners are registered with a "normal" amount of delay
        int sensorType = sensorEvent.sensor.getType();

        switch (sensorType) {
            case Sensor.TYPE_ROTATION_VECTOR:
                mRotationVectorData = sensorEvent.values.clone();
            default:
                //mTextMoveStatus.setText(sensorName);
                //return;
        }
        return computeRotation();
    }

    private Map<String, Float> computeRotation()
    {
        float[] rotationMatrix = new float[9];
        float[] orientationValues = new float[3];

        SensorManager.getRotationMatrixFromVector(rotationMatrix, mRotationVectorData);
        SensorManager.getOrientation(rotationMatrix, orientationValues);

        float yaw = orientationValues[0]; //Yaw
        float pitch = orientationValues[1]; //Pitch
        float roll = orientationValues[2]; //Roll

        sensorMap.put("yaw", yaw);
        sensorMap.put("pitch", pitch);
        sensorMap.put("roll", roll);

        return sensorMap;
    }
}