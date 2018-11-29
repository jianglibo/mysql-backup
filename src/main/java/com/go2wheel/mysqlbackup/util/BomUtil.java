package com.go2wheel.mysqlbackup.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BomUtil {

	public static final byte EF = (byte) 0b1110_1111;
	public static final byte BB = (byte) 0b1011_1011;
	public static final byte BF = (byte) 0b1011_1111;

	public static final byte FF = (byte) 0b1111_1111;
	public static final byte FE = (byte) 0b1111_1110;

	public static class BytesAndCharset {

		private byte[] bytes;
		private Charset charset;

		public BytesAndCharset(byte[] bytes, Charset charset) {
			super();
			this.bytes = bytes;
			this.charset = charset;
		}

		public byte[] getBytes() {
			return bytes;
		}

		public void setBytes(byte[] bytes) {
			this.bytes = bytes;
		}

		public Charset getCharset() {
			return charset;
		}

		public void setCharset(Charset charset) {
			this.charset = charset;
		}

		public String toString() {
			if (charset != null) {
				return new String(bytes, charset);
			} else {
				return new String(bytes);
			}
		}
	}

	public static BytesAndCharset removeBom(byte[] bytes) {
		int len = bytes.length;
		Charset charset = StandardCharsets.UTF_8;
		if (len > 3) {
			if (bytes[0] == EF && bytes[1] == BB && bytes[2] == BF) { // UTF-8
				bytes = Arrays.copyOfRange(bytes, 3, len);
				charset = StandardCharsets.UTF_8;
			} else if (bytes[0] == FF && bytes[1] == FE) { // UTF-16 (LE)
				bytes = Arrays.copyOfRange(bytes, 2, len);
				charset = StandardCharsets.UTF_16LE;
			} else if (bytes[0] == FE && bytes[1] == FF) { // UTF-16 (BE)
				bytes = Arrays.copyOfRange(bytes, 2, len);
				charset = StandardCharsets.UTF_16BE;
			}
		}
		return new BytesAndCharset(bytes, charset);
	}
}
