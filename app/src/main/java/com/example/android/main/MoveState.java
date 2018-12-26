package com.example.android.main;

public enum MoveState {
    FORWARD (20.0f, 500.0f, (float) Math.PI/2),
    BACKWARD (20.0f, 500.0f, (float) Math.PI/2),
    LEFT (20.0f, 500.0f, (float) Math.PI/2),
    RIGHT (20.0f, 500.0f, (float) Math.PI/2),
    ROTATELEFT (1.0f, 360.0f, 0.0f),
    ROTATERIGHT (1.0f, 360.0f, 0.0f),
    HOVER (0.0f, 0.0f, 0.0f),
    GROUND (0.0f, 0.0f, 0.0f);

    MoveState(float MIN, float MAX, float angleMAX) {
        this.MIN = MIN;
        this.MAX = MAX;
        this.angleMAX = angleMAX;
    }

    public float MIN;
    public float MAX;
    public float angleMAX;

    public float getMIN(){ return this.MIN; }
    public float getMAX(){ return this.MAX; }
    public float getAngleMAX(){ return this.angleMAX; }
}
