package io.escriba.server;

public enum Method {
	PUT(10), GET(20);

	public final byte code;

	Method(int code) {
		this.code = (byte) code;
	}
}
