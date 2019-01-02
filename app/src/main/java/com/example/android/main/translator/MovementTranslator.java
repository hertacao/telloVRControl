package com.example.android.main.translator;

import com.example.android.main.MoveState;

public interface MovementTranslator {
    float translate(MoveState moveState, float angle);
}
