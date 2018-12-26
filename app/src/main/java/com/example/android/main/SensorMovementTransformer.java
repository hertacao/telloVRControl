package com.example.android.main;

public class SensorMovementTransformer {
    private float speed;

    public SensorMovementTransformer(float speed) {
        if(speed > 1) {
            this.speed = 1;
        } else if(speed < 0) {
            this.speed = 0.1f;
        } else {
            this.speed = speed;
        }
    }

    public float transform(MoveState moveState, float angle_diff) {
        switch(moveState) {
            case HOVER: return 1;
            case GROUND: return 0;
            case ROTATELEFT: return angle_diff;
            case ROTATERIGHT: return angle_diff;

            default:
                if(angle_diff > moveState.getAngleMAX()) {
                    angle_diff = moveState.getAngleMAX();
                } else if (angle_diff < 0) {
                    angle_diff = 0;
                }

                float normalFactor = speed*(angle_diff/moveState.getAngleMAX());
                float displacement = moveState.getMIN()
                        + normalFactor*(moveState.getMAX()-moveState.getMIN());

                return displacement;
        }
    }
}
