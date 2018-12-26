package sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.android.main.MainActivity;
import com.example.android.main.MoveState;

import java.util.HashMap;
import java.util.Map;


public class SensorHandler implements SensorEventListener {
    private MainActivity mContext;

    // Very small values for the accelerometer (on all three axes) should
    // be interpreted as 0. This value is the amount of acceptable
    // non-zero drift.
    // private static final float VALUE_DRIFT = 0.05f;      don't need this BS, my code is much better ofc
    private static final float HYSTERESIS = 0.05f;
    private static final float FORWARD_OFFSET = 0.3f;
    private static final float BACKWARD_OFFSET = 0.4f;
    private static final float ROTATION_OFFSET = 0.2f;
    private static final float SIDEMOVE_OFFSET = 0.35f;

    private static final float CALIBRATION_OFFSET = 0.2f;

    private static float yaw_Offset = 0.0f;
    private static float pitch_Offset = 0.0f;
    private static float roll_Offset = 0.0f;

    private float lastYaw;

    // System sensor manager instance.
    private SensorManager mSensorManager;

    // Strategy that is being used to compute yaw, pitch, roll
    private SensorStrategy sensorStrategy;

    // Sensor yaw, pitch roll, according to landscape mode
    private Map<String, Float> landscapeSensorMap = new HashMap<>();

    // current Movement State
    private MoveState moveState = MoveState.GROUND;

    //
    private float angle_diff;

    public SensorHandler(MainActivity mContext) {
        this.mContext = mContext;

        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        if(mSensorManager != null) {
            sensorStrategy = new RotationVector(mSensorManager);
        } else {
            System.out.println("ERROR: no SensorManager");
        }
    }

    public void registerSensor() {
        // Check to ensure sensors are available before registering listeners.
        // Both listeners are registered with a "normal" amount of delay
        for(Sensor sensor : sensorStrategy.getSensor()) {
            if (sensor != null) {
                mSensorManager.registerListener(this, sensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    public void unregisterSensor() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // only execute when not on ground
        Map<String, Float> sensorMap = this.sensorStrategy.onSensorChanged(sensorEvent);
        this.transformOrientationToLandscape(sensorMap);

        computeState();
        mContext.update(landscapeSensorMap, moveState, angle_diff);
    }

    public void start() {
        this.calibrate();
        this.moveState = MoveState.HOVER;
    }

    public void stop() {
        this.resetOffset();
        this.moveState = MoveState.GROUND;
    }

    public void calibrate() {
        yaw_Offset = landscapeSensorMap.get("yaw");
        lastYaw = yaw_Offset;
        
        if (landscapeSensorMap.get("pitch") > CALIBRATION_OFFSET) {
            pitch_Offset = CALIBRATION_OFFSET;
        } else if (landscapeSensorMap.get("pitch") < -CALIBRATION_OFFSET) {
            pitch_Offset = -CALIBRATION_OFFSET;
        } else {
            pitch_Offset = landscapeSensorMap.get("pitch");
        }

        if (landscapeSensorMap.get("roll") > CALIBRATION_OFFSET) {
            roll_Offset = CALIBRATION_OFFSET;
        } else if (landscapeSensorMap.get("roll") < -CALIBRATION_OFFSET) {
            roll_Offset = -CALIBRATION_OFFSET;
        } else {
            roll_Offset = landscapeSensorMap.get("roll");
        }
    }

    private void resetOffset() {
        yaw_Offset = 0.0f;
        pitch_Offset = 0.0f;
        roll_Offset = 0.0f;
    }

    private void computeState() {

        //params:  (tuneable above)
        //          Offset from the middle positon - how far you have to tilt until drone begin to fly somewhere
        //          Hysteresis - so it doesn't flicker from forward to hover if the tilt is on the edge


//        ConnErrorDialog die = new ConnErrorDialog();
//        Bundle args = new Bundle();
//        die.onCreateDialog(args);

        //following code is maybe a little bit dumb, dunno how to do it better
        //TO DO: yet to implement rotation

        float yaw = landscapeSensorMap.get("yaw");
        float pitch = landscapeSensorMap.get("pitch");
        float roll = landscapeSensorMap.get("roll");

        float hysteresis;
        if (moveState == MoveState.HOVER||moveState == MoveState.GROUND) {
            hysteresis = 0;
        } else {
            hysteresis = HYSTERESIS;
        }

        //HYSTERESIS
        //TO DO: (!!!) still flickers from forward to left etc. (!!!) opt. solution - you have to hover between changing states - especially for testing wouldnt be bad
        //I think this code should be written better. :D

        if (pitch > (FORWARD_OFFSET - hysteresis)) {
            moveState = MoveState.FORWARD;
            angle_diff = Math.abs(pitch) - FORWARD_OFFSET;

        } else if (pitch < -(BACKWARD_OFFSET - hysteresis)) {
            moveState = MoveState.BACKWARD;
            angle_diff = Math.abs(pitch) - BACKWARD_OFFSET;

        /*} else if (yaw < lastYaw - ROTATION_OFFSET) {
            moveState = MoveState.ROTATELEFT;
            angle_diff = Math.abs(yaw) - ROTATION_OFFSET;
            //lastYaw = yaw;

        } else if (yaw > lastYaw + ROTATION_OFFSET) {
            moveState = MoveState.ROTATERIGHT;
            angle_diff = Math.abs(yaw) - ROTATION_OFFSET;
            //lastYaw = yaw;
         */

        } else if (roll < -(SIDEMOVE_OFFSET - hysteresis)) {
            moveState = MoveState.RIGHT;
            angle_diff = Math.abs(roll) - SIDEMOVE_OFFSET;

        } else if (roll > (SIDEMOVE_OFFSET - hysteresis)) {
            moveState = MoveState.LEFT;
            angle_diff = Math.abs(roll) - SIDEMOVE_OFFSET;

        } else {
            moveState = MoveState.HOVER;
            angle_diff = 0;
        }

    }

    private void transformOrientationToLandscape(Map<String, Float> sensorMap) {
        double yaw = sensorMap.get("yaw");
        double pitch = sensorMap.get("roll")+(Math.PI/2);
        double roll = sensorMap.get("pitch");

        yaw = (yaw - yaw_Offset) % (2*Math.PI);
        pitch = (pitch - pitch_Offset) % (2*Math.PI);
        roll = (roll - roll_Offset) % (2*Math.PI);

        this.landscapeSensorMap.put("yaw", (float) yaw);
        this.landscapeSensorMap.put("pitch", (float) pitch);
        this.landscapeSensorMap.put("roll", (float) roll);
    }

    public void setMoveState(MoveState moveState) {
        this.moveState = moveState;
    }

    /**
     * Must be implemented to satisfy the SensorEventListener interface;
     * unused in this app.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
