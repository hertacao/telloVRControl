package com.example.android.main.translater;

import com.example.android.main.MainActivity;
import com.example.android.main.MoveState;

import static java.lang.StrictMath.abs;

public class LinearMovementTranslator extends AbstractMovementTranslator {
    private MainActivity activity;
    private float speed;

    public LinearMovementTranslator(MainActivity activity, float speed) {
        super(activity, speed);
    }

    public float translate(MoveState moveState, float angle) {
        switch(moveState) {
            case HOVER: return 1;
            case GROUND: return 0;
            case ROTATELEFT: return computeRot(angle);
            case ROTATERIGHT: return computeRot(angle);

            default:
                if(angle > moveState.getAngleMAX()) {
                    angle = moveState.getAngleMAX();
                } else if (angle < 0) {
                    angle = 0;
                }

                float normalFactor = speed*(angle/moveState.getAngleMAX());

                return moveState.getMIN()
                        + normalFactor*(moveState.getMAX()-moveState.getMIN());
        }
    }
}
