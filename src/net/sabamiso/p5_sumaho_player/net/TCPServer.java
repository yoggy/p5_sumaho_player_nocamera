package net.sabamiso.p5_sumaho_player.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import p5_sumaho.Payload;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.protobuf.InvalidProtocolBufferException;

class SumahoServerThread extends Thread {
	TCPServer parent;
	ServerSocket server_socket;
	Socket peer_socket;
	boolean break_flag = false;

	
	public SumahoServerThread(TCPServer parent,
			ServerSocket server_socket) {
		this.parent = parent;
		this.server_socket = server_socket;
	}

	public String getPeerName() {
		if (peer_socket == null) return null;
		
		try {
			InetAddress addr = peer_socket.getInetAddress();
			return addr.getHostAddress();
		}
		catch(Exception e) {
			return null;
		}
	}
	
	@Override
	public void run() {
		while (!break_flag) {
			try {
				peer_socket = server_socket.accept();
				readLoop(peer_socket);

			} catch (IOException e) {
				peer_socket = null;
			}
		}
	}

	public void finish() {
		if (server_socket != null) {
			break_flag = true;
			try {
				server_socket.close();
			} catch (IOException e) {
			}

			try {
				this.join();
			} catch (InterruptedException e) {
			}
			server_socket = null;
		}
	}

	void readLoop(Socket socket) {
		byte[] header_buf = new byte[4];
		byte[] payload_size_buf = new byte[4];
		int payload_size;
		byte[] payload;

		// setup stream
		InputStream is = null;
		OutputStream os = null;
		try {
			is = socket.getInputStream();
			os = socket.getOutputStream();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// read loop
		while (!break_flag) {
			try {
				// read header (4bytes)
				is.read(header_buf);
				if (header_buf[0] != 'S' || header_buf[1] != 'M'
						|| header_buf[2] != 'H' || header_buf[3] != '1') {
					break;
				}

				// read payload size (4bytes)
				is.read(payload_size_buf);
				ByteBuffer bb = ByteBuffer.wrap(payload_size_buf).order(
						ByteOrder.LITTLE_ENDIAN);
				payload_size = bb.getInt();

				// read payload
				payload = new byte[payload_size];
				int read_size = 0;
				while (true) {
					int s = is.read(payload, read_size, payload.length
							- read_size);
					if (s <= 0)
						break;
					read_size += s;
					if (read_size >= payload.length)
						break;
				}

				// decode
				boolean rv = decode(payload, read_size);
				if (rv == false)
					break; // close connection if failed decoding

			} catch (Exception e) {
				break;
			}

			try {
				sleep(parent.getReadIntervalTime());
			} catch (InterruptedException e) {
			}
		}

		try {
			is.close();
			os.close();
			socket.close();
		} catch (Exception e) {
		}
	}

	private boolean decode(byte[] payload, int size) {
		// deserialize
		Payload.Image img;
		try {
			img = Payload.Image.parseFrom(payload);
		} catch (InvalidProtocolBufferException e) {
			System.err
					.println("InvalidProtocolBufferException : payload.length="
							+ payload.length);
			e.printStackTrace();
			return false;
		}

		try {
			// decode to Bitmap
			byte[] jpeg_data = img.getJpeg().toByteArray();
			Bitmap bmp = BitmapFactory.decodeByteArray(jpeg_data, 0,
					jpeg_data.length);
			parent.setBitmap(bmp);

			// set update time
			parent.setLastUpdateTime(System.currentTimeMillis());
		} catch (Exception e) {
		}

		return true;
	}

}

public class TCPServer {
	int listen_port;

	SumahoServerThread thread;
	UpdateBitmapListener listener;
	
	Bitmap bitmap;
	long last_update_time;

	int timeout = 1000;
	int read_interval_time = 1;

	public TCPServer(int listen_port) {
		this.listen_port = listen_port;
	}

	public void setUpdateBitmapListener(UpdateBitmapListener listener) {
		this.listener = listener;
	}
	
	public void setTimeout(int ms) {
		this.timeout = ms;
	}

	public void setReadIntervalTime(int ms) {
		this.read_interval_time = ms;
	}

	public int getReadIntervalTime() {
		return this.read_interval_time;
	}

	public boolean isActive() {
		long t = getLastUpdateTime();
		if (t == 0)
			return false;

		long diff = System.currentTimeMillis() - t;
		if (diff > timeout)
			return false;

		return true;
	}
	
	public String getPeerName() {
		if (isActive() == false) {
			return null;
		}
		
		return thread.getPeerName();
	}

	protected void setLastUpdateTime(Long time) {
		last_update_time = time;
	}

	public long getLastUpdateTime() {
		return last_update_time;
	}

	public int getLietenPort() {
		return listen_port;
	}

	public boolean start() {
		ServerSocket s;
		try {
			s = new ServerSocket(listen_port, 5);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		thread = new SumahoServerThread(this, s);
		thread.start();

		return true;
	}

	public void stop() {
		if (thread != null) {
			thread.finish();
			thread = null;
		}
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	protected void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
		if (listener != null) {
			listener.updateBitmap(bitmap);
		}
	}

}
