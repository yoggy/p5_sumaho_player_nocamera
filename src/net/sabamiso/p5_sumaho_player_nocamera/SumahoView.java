package net.sabamiso.p5_sumaho_player_nocamera;

import net.sabamiso.p5_sumaho_player.net.Event;
import net.sabamiso.p5_sumaho_player.net.TCPServer;
import net.sabamiso.p5_sumaho_player.net.UDPEventClient;
import net.sabamiso.p5_sumaho_player.net.UpdateBitmapListener;
import net.sabamiso.p5_sumaho_player.sensor.GravitySensor;
import net.sabamiso.p5_sumaho_player.sensor.LightSensor;
import net.sabamiso.p5_sumaho_player.sensor.MagneticFieldSensor;
import net.sabamiso.p5_sumaho_player.sensor.ProximitySensor;
import net.sabamiso.utils.Config;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class SumahoView extends View implements
		UpdateBitmapListener, Runnable {

	TCPServer tcp_server;
	private Bitmap bitmap;
	Rect bitmap_draw_rect;

	UDPEventClient udp_thread;

	GravitySensor gravity_sensor;
	MagneticFieldSensor magnetic_field_sensor;
	LightSensor light_sensor;
	ProximitySensor proximity_sensor;
	
	Paint p_default;
	Paint p_text_center;
	Paint p_text_edge;

	Handler handler = new Handler();

	Config cf;
	
	public SumahoView(Context context) {
		super(context);
		cf = Config.getInstance();
		
		setBackgroundColor(Color.BLACK);

		p_default = new Paint();

		p_text_center = new Paint();
		p_text_center.setColor(Color.WHITE);
		p_text_center.setTypeface(Typeface.DEFAULT_BOLD);

		p_text_edge = new Paint();
		p_text_edge.setColor(Color.BLACK);
		p_text_edge.setTypeface(Typeface.DEFAULT_BOLD);		
	}

	public boolean getDebug() {
		return cf.getBoolean("debug_mode");
	}

	public void setDebug(boolean val) {
		cf.getBoolean("debug_mode", val);

		handler.post(new Runnable() {
			@Override
			public void run() {
				invalidate();
			}
		});
	}

	public void start() {
		tcp_server = new TCPServer(cf.getInt("tcp_listen_port_for_image", 23401));
		tcp_server.setUpdateBitmapListener(this);
		boolean rv = tcp_server.start();

		if (rv == false) {
			Toast.makeText(
					getContext(),
					"listen port failed...port="
							+ tcp_server.getLietenPort(), Toast.LENGTH_LONG)
					.show();
		}

		udp_thread = new UDPEventClient();
		udp_thread.setPeerPort(cf.getInt("udp_peer_port_for_send_event",  23402));
		udp_thread.start();

		// sensor
		Activity activity = (Activity)getContext();

		gravity_sensor = new GravitySensor(activity, udp_thread);
		gravity_sensor.resume();
		
		magnetic_field_sensor = new MagneticFieldSensor(activity, udp_thread);
		magnetic_field_sensor.resume();
		
		light_sensor = new LightSensor(activity, udp_thread);
		light_sensor.resume();

		proximity_sensor = new ProximitySensor(activity, udp_thread);
		proximity_sensor.resume();

		handler.post(this);
	}

	public void stop() {
		handler.removeCallbacks(this);
		
		gravity_sensor.pause();
		gravity_sensor = null;
		
		magnetic_field_sensor.pause();
		magnetic_field_sensor = null;

		light_sensor.pause();
		light_sensor = null;

		proximity_sensor.pause();
		proximity_sensor = null;

		if (udp_thread != null) {
			udp_thread.finish();
		}
		if (tcp_server != null) {
			tcp_server.stop();
		}
	}

	@Override
	public void updateBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;

		handler.post(new Runnable() {
			@Override
			public void run() {
				invalidate();
			}
		});
	}

	@Override
	public void onDraw(Canvas canvas) {
		drawBitmap(canvas);
		drawConnectionStatus(canvas);
		drawDebugInfo(canvas);
	}

	void drawBitmap(Canvas canvas) {
		if (bitmap == null)
			return;
		if (bitmap.getWidth() == 0 || bitmap.getHeight() == 0)
			return;

		//
		int screen_w = getWidth();
		int screen_h = getHeight();
		float screen_aspect = (float) screen_w / (float) screen_h;

		int bitmap_w = bitmap.getWidth();
		int bitmap_h = bitmap.getHeight();
		float bitmap_aspect = (float) bitmap_w / (float) bitmap_h;

		int x, y, w, h;
		if (screen_aspect >= bitmap_aspect) {
			w = (int) (screen_h * bitmap_aspect);
			h = screen_h;
			x = (screen_w - w) / 2;
			y = 0;
		} else {
			w = screen_w;
			h = (int) (screen_w / bitmap_aspect);
			x = 0;
			y = (screen_h - h) / 2;
		}

		Rect src = new Rect(0, 0, bitmap_w, bitmap_h);
		Rect dst = new Rect(x, y, x + w, y + h);
		bitmap_draw_rect = dst;
		
		canvas.drawBitmap(bitmap, src, dst, p_default);

	}

	private void drawConnectionStatus(Canvas canvas) {
		if (!tcp_server.isActive()) {
			long t = System.currentTimeMillis() / 1000;
			if (t % 2 < 1) {
				drawText(canvas, 10, getHeight() - 40, 32, Color.RED,
						"client is not connected...");
			}
		}
	}

	private void drawDebugInfo(Canvas canvas) {
		if (tcp_server.isActive() && !getDebug())
			return;

		String addr = getIpAddress();
		int size = 28;
		
		drawText(canvas, 10, 30 + size * 0, size, Color.WHITE,
				"p5_sumaho_player");
		drawText(canvas, 30, 30 + size * 1, size, Color.WHITE, "ip address = "
				+ addr);
		drawText(canvas, 30, 30 + size * 2, size, Color.WHITE, "listen port = "
				+ tcp_server.getLietenPort());
		
		if (cf.getBoolean("enable_camera") == true) {
			String url = "";
			if (addr.contains("ERROR")) {
				url = addr;
			}
			else {
				int port = cf.getInt("tcp_listen_port_for_camera");
				url = "http://" + addr + ":" + port + "/camera.jpg";
			}
			drawText(canvas, 30, 30 + size * 3, size, Color.WHITE, "url = "
					+ url);
		}
	}

	public void drawText(Canvas canvas, float x, float y, int size, int color,
			String msg) {
		p_text_center.setColor(color);
		p_text_center.setTextSize(size);
		p_text_edge.setTextSize(size);

		for (int dy = -2; dy <= 2; dy += 2) {
			for (int dx = -2; dx <= 2; dx += 2) {
				canvas.drawText(msg, x + dx, y + dy, p_text_edge);
			}
		}

		canvas.drawText(msg, x, y, p_text_center);
	}

	private String getIpAddress() {
		WifiManager wifi_manager = (WifiManager) getContext().getSystemService(
				Context.WIFI_SERVICE);
		WifiInfo info = wifi_manager.getConnectionInfo();

		int ip = info.getIpAddress();
		if (ip == 0) {
			return "ERROR: please check wifi connection...";
		}

		String ip_str = "" + ((ip >> 0) & 0xFF) + "." + ((ip >> 8) & 0xFF)
				+ "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);

		return ip_str;
	}

	@Override
	public boolean onTouchEvent(MotionEvent evt) {
		int action = evt.getAction() & MotionEvent.ACTION_MASK;
		int pointer_idx = (evt.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

		switch (action) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:
			sendTouchEvent(Event.TOUCH_DOWN, pointer_idx, evt.getX(), evt.getY());
			break;
		case MotionEvent.ACTION_MOVE:
			sendTouchEvent(Event.TOUCH_MOVE, pointer_idx, evt.getX(), evt.getY());
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			sendTouchEvent(Event.TOUCH_UP, pointer_idx, evt.getX(), evt.getY());
			break;
		}
		return true;
	}

	@Override
	public void run() {
		// redraw
		invalidate();
		
		// check peer name
		String peer_name = tcp_server.getPeerName();
		udp_thread.setPeerName(peer_name);
		
		handler.postDelayed(this, 500);
	}

	void sendTouchEvent(int type, int id, float x, float y) {
		if (bitmap_draw_rect == null) {
			return;
		}
		
		// convert to normalized bitmap image coordinate
		float px = (x - bitmap_draw_rect.left) / (bitmap_draw_rect.right - bitmap_draw_rect.left);
		float py = (y - bitmap_draw_rect.top) / (bitmap_draw_rect.bottom - bitmap_draw_rect.top);

		udp_thread.sendTouchEvent(type, id, px, py);		
	}
}
