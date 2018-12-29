package com.example.android.main.translater;

import com.example.android.main.MainActivity;

import static java.lang.StrictMath.abs;

abstract class AbstractMovementTranslator implements MovementTranslator {
    private MainActivity activity;
    float speed;

    AbstractMovementTranslator(MainActivity activity, float speed) {
        this.activity = activity;
        if(speed > 1) {
            this.speed = 1;
        } else if(speed < 0) {
            this.speed = 0.1f;
        } else {
            this.speed = speed;
        }
    }

    float computeRot(float angle) {
        int droneYaw = activity.getDroneYaw();
        float rotDiff = abs(angle) - abs(droneYaw);
        if (rotDiff > 0) {
            return rotDiff;
        } else {
            return 0;
        }
    }
}
