package net.sabamiso.p5_sumaho_player.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

import p5_sumaho.Payload;

public class UDPEventClient extends Thread {
	
	String peer_name;
	int peer_port;
	DatagramSocket socket;
	
	Queue<byte []> queue = new LinkedList<byte []>();
	
	boolean exit_flag = false;
		
	public UDPEventClient() {
	}

	public void setPeerName(String val) {
		this.peer_name = val;
	}
	
	public void setPeerPort(int val) {
		this.peer_port = val;
	}

	public synchronized void finish() {
		try {
			exit_flag = true;
			queue.notify();
			this.join();
		} catch (Exception e) {
		}
	}
	
	@Override
	public void run() {
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}

		try {
			while(!exit_flag) {
				synchronized(queue) {
					queue.wait();
					while(true) {
						byte [] payload = queue.poll();
						if (exit_flag == true) break;
						if (payload == null) break;
						
						InetAddress addr;
						try {
							addr = InetAddress.getByName(peer_name);
							DatagramPacket packet = new DatagramPacket(payload, payload.length,
									addr, peer_port);
							socket.send(packet);
						} catch (UnknownHostException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			} 
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			if (socket != null) {
				socket.close();
				socket = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void send(byte [] payload) {
		synchronized(queue) {
			queue.offer(payload);
			queue.notify();
		}
	}

	public void sendTouchEvent(int type, int id, float x, float y) {
		Payload.EventType evt_type = Payload.EventType.newBuilder().setId(Event.TYPE_TOUCH).build();
		
		// build packet
		Payload.EventTouch.Builder builder = Payload.EventTouch.newBuilder();
		builder.setType(type);
		builder.setId(id);
		builder.setX(x);
		builder.setY(y);
		Payload.EventTouch evt_body = builder.build();

		// write to byte array
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
		try {
			evt_type.writeDelimitedTo(bos);
			evt_body.writeDelimitedTo(bos);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		send(bos.toByteArray());
	}	

	public void sendGravityEvent(float x, float y, float z) {
		Payload.EventType evt_type = Payload.EventType.newBuilder().setId(Event.TYPE_GRAVITY).build();
		
		// build packet
		Payload.EventGravity.Builder builder = Payload.EventGravity.newBuilder();
		builder.setX(x);
		builder.setY(y);
		builder.setZ(z);
		Payload.EventGravity evt_body = builder.build();

		// write to byte array
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
		try {
			evt_type.writeDelimitedTo(bos);
			evt_body.writeDelimitedTo(bos);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		send(bos.toByteArray());
	}
	
	public void sendMagneticFieldEvent(float x, float y, float z) {
		Payload.EventType evt_type = Payload.EventType.newBuilder().setId(Event.TYPE_MAGNETIC_FIELD).build();
		
		// build packet
		Payload.EventMagneticField.Builder builder = Payload.EventMagneticField.newBuilder();
		builder.setX(x);
		builder.setY(y);
		builder.setZ(z);
		Payload.EventMagneticField evt_body = builder.build();

		// write to byte array
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
		try {
			evt_type.writeDelimitedTo(bos);
			evt_body.writeDelimitedTo(bos);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		send(bos.toByteArray());
	}
	
	public void sendProximityEvent(float val) {
		Payload.EventType evt_type = Payload.EventType.newBuilder().setId(Event.TYPE_PROXIMITY).build();
		
		// build packet
		Payload.EventProximity.Builder builder = Payload.EventProximity.newBuilder();
		builder.setVal(val);
		Payload.EventProximity evt_body = builder.build();

		// write to byte array
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
		try {
			evt_type.writeDelimitedTo(bos);
			evt_body.writeDelimitedTo(bos);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		send(bos.toByteArray());
	}

	public void sendLightEvent(float val) {
		Payload.EventType evt_type = Payload.EventType.newBuilder().setId(Event.TYPE_LIGHT).build();
		
		// build packet
		Payload.EventLight.Builder builder = Payload.EventLight.newBuilder();
		builder.setVal(val);
		Payload.EventLight evt_body = builder.build();

		// write to byte array
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
		try {
			evt_type.writeDelimitedTo(bos);
			evt_body.writeDelimitedTo(bos);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		send(bos.toByteArray());
	}	
}
