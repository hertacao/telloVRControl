package com.example.android.main.translator;

import com.example.android.main.MainActivity;
import com.example.android.main.MoveState;

import static java.lang.StrictMath.abs;

public class LinearMovementTranslator extends AbstractMovementTranslator {

    public LinearMovementTranslator(MainActivity activity, float speed) {
        super(activity, speed);
    }

    public float translate(MoveState moveState, float angle) {
        switch(moveState) {
            case HOVER: return 1;
            case GROUND: return 0;
            case ROTATELEFT: return computeLeftRot(angle);
            case ROTATERIGHT: return computeRightRot(angle);

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
