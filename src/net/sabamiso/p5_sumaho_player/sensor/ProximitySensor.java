package net.sabamiso.p5_sumaho_player.sensor;

import net.sabamiso.p5_sumaho_player.net.UDPEventClient;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;

public class ProximitySensor extends SensorBase {
	public ProximitySensor(Activity activity, UDPEventClient udp_event_client) {
		super(activity, udp_event_client,Sensor.TYPE_PROXIMITY);
	}

	@Override
	public void onSensorChanged(SensorEvent evt) {
		udp_event_client.sendProximityEvent(evt.values[0]);
	}
}
