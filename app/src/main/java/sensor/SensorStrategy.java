package sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

import java.util.List;
import java.util.Map;

public interface SensorStrategy {
    List<Sensor> getSensor();
    Map<String, Float> onSensorChanged(SensorEvent sensorEvent);
}
