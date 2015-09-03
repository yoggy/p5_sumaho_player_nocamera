package net.sabamiso.p5_sumaho_player.sensor;

import net.sabamiso.p5_sumaho_player.net.UDPEventClient;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;

public class LightSensor extends SensorBase {
	public LightSensor(Activity activity, UDPEventClient udp_event_client) {
		super(activity, udp_event_client, Sensor.TYPE_LIGHT);
	}

	@Override
	public void onSensorChanged(SensorEvent evt) {
		udp_event_client.sendLightEvent(evt.values[0]);
	}
}
