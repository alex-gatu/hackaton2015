package ro.endava.hackathon2015;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class BufferedOutputStream extends OutputStream {
	private ByteArrayOutputStream baos = new ByteArrayOutputStream();
	private OutputStream sink;
	private HttpConnection connection;
	private ArrayList<ByteArrayOutputStream> caches = new ArrayList<ByteArrayOutputStream>();

	public BufferedOutputStream(HttpConnection connection) {
		this.sink = baos;
		this.connection = connection;
	}

	public synchronized void flushAndSwap() throws IOException {
		if (baos == null)
			return;
		byte[] dump = baos.toByteArray();
		baos = null;
		this.connection.makeSureHeadersAreSent();
		sink = connection.exchange.getResponseBody();
		sink.write(dump);
		sink.flush();
	}

	@Override
	public synchronized void write(int b) throws IOException {
		this.write(new byte[] { (byte) b }, 0, 1);
	}

	@Override
	public synchronized void write(byte[] b, int off, int len)
			throws IOException {
		if (caches.size() == 0) {
			sink.write(b, off, len);
		} else {
			caches.get(caches.size() - 1).write(b, off, len);
		}
	}

	@Override
	public void flush() throws IOException {
		sink.flush();
	}

	public synchronized void print(Object text) throws IOException {
		write(String.valueOf(text).getBytes());
	}


	public synchronized byte[] getCache() throws IOException {
		if (caches.size() == 0)
			throw new IOException(
					"BufferedOutputStream has not cache created - use newCache");
		return caches.get(caches.size() - 1).toByteArray();
	}

	public synchronized void closeCache() throws IOException {
		byte[] data = getCache();
		deleteCache();
		write(data, 0, data.length);
	}

	public synchronized void deleteCache() {
		caches.remove(caches.size() - 1);
	}

	public void flushAllCache() throws IOException {
		while (caches.size() > 0)
			closeCache();
	}
}