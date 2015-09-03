package net.sabamiso.p5_sumaho_player.sensor;

import java.util.List;

import net.sabamiso.p5_sumaho_player.net.UDPEventClient;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public abstract class SensorBase implements SensorEventListener {
	Activity activity;
	UDPEventClient udp_event_client;
	float [] values;
	int sensor_type;
	SensorManager sensor_manager;

	public SensorBase(Activity activity, UDPEventClient udp_event_client, int sensor_type) {
		this.activity = activity;
		this.udp_event_client = udp_event_client;
		this.sensor_type = sensor_type;
	}
	
	public void resume() {
		sensor_manager = (SensorManager)activity.getSystemService(Context.SENSOR_SERVICE);		
		List<Sensor> sensors = sensor_manager.getSensorList(sensor_type);
		if (sensors.size() > 0) {
			Sensor sensor = sensors.get(0);
			sensor_manager.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_UI);
		}
	}

	public void pause() {
		if (sensor_manager != null) {
			sensor_manager.unregisterListener(this);
			sensor_manager = null;
		}
	}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {		
	}

	public abstract void onSensorChanged(SensorEvent event);
}
