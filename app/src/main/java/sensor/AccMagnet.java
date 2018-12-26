package sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccMagnet implements SensorStrategy {
    // Accelerometer and magnetometer sensors, as retrieved from the sensor manager.
    private Sensor mSensorAccelerometer;
    private Sensor mSensorMagnetometer;

    // Sensor Values
    // Acceleration force along the x,y,z axis (including gravity), m/s^2, -78,45 to +78,45
    private float[] mAccelerometerData = new float[3];
    // Geomagnetic field strength along the x,y,z axis, μT, -2000 to +2000
    private float[] mMagnetometerData = new float[3];

    // SensorMap
    private Map<String, Float> sensorMap = new HashMap<>();

    public AccMagnet(SensorManager mSensorManager) {
        // Get accelerometer and magnetometer sensors from the sensor manager.
        // The getDefaultSensor() method returns null if the sensor
        // is not available on the device.
        mSensorAccelerometer = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER);
        mSensorMagnetometer = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_MAGNETIC_FIELD);
    }

    public List<Sensor> getSensor() {
        return Arrays.asList(mSensorAccelerometer, mSensorMagnetometer);
    }

    public Map<String, Float> onSensorChanged (SensorEvent sensorEvent) {
        // Check to ensure sensors are available before registering listeners.
        // Both listeners are registered with a "normal" amount of delay
        int sensorType = sensorEvent.sensor.getType();

        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                mAccelerometerData = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagnetometerData = sensorEvent.values.clone();
                break;
            default:
                //mTextMoveStatus.setText(sensorName);
                //return;
        }
        return computeRotation();
    }

    private Map<String, Float> computeRotation() {
        float[] rotationMatrix = new float[9];
        float[] orientationValues = new float[3];

        boolean rotationOK = SensorManager.getRotationMatrix(rotationMatrix,
                null, mAccelerometerData, mMagnetometerData);

        if (rotationOK) {
            SensorManager.getOrientation(rotationMatrix, orientationValues);
        } else {
            System.out.println("Free Fall detected?");
            sensorMap.put("yaw", (float)0.0);
            sensorMap.put("pitch", (float)0.0);
            sensorMap.put("roll", (float)0.0);
        }

        float yaw = orientationValues[0];
        float pitch = orientationValues[1];
        float roll = orientationValues[2];

        sensorMap.put("yaw", yaw);
        sensorMap.put("pitch", pitch);
        sensorMap.put("roll", roll);

        return sensorMap;
    }
}
