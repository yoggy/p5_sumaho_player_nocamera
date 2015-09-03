package net.sabamiso.p5_sumaho_player.sensor;

import net.sabamiso.p5_sumaho_player.net.UDPEventClient;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;

public class MagneticFieldSensor extends SensorBase {
	public MagneticFieldSensor(Activity activity, UDPEventClient udp_event_client) {
		super(activity, udp_event_client, Sensor.TYPE_MAGNETIC_FIELD);
	}

	@Override
	public synchronized void onSensorChanged(SensorEvent evt) {
		udp_event_client.sendMagneticFieldEvent(evt.values[0], evt.values[1], evt.values[2]);
	}
}
