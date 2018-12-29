package com.example.android.main.translater;

import com.example.android.main.MainActivity;
import com.example.android.main.MoveState;

public class SimpleMovementTranslator extends AbstractMovementTranslator {

    public SimpleMovementTranslator(MainActivity activity, float speed) {
        super(activity, speed);
    }

    @Override
    public float translate(MoveState moveState, float angle) {
        switch (moveState) {
            case HOVER: return 1;
            case GROUND: return 0;
            case ROTATELEFT: return computeRot(angle);
            case ROTATERIGHT: return computeRot(angle);

            default: return 100 * speed;
        }
    }
}
