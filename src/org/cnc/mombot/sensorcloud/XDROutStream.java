/*
 * Copyright 2012 MicroStrain Inc. All Rights Reserved.
 *
 * Distributed under the Simplified BSD License.
 * See file license.txt
 *
 */

package org.cnc.mombot.sensorcloud;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Output stream wrapper for writing xdr data
 * Only a subset of the XDR standard has been implemented here as needed to operate with SensorCloud
 * 
 * 
 * ref: http://tools.ietf.org/html/rfc4506
 *
 */
public class XDROutStream {

	private DataOutputStream dataStream;

	/**
	 * @param out  output stream to write xdr data too  
	 */
	public XDROutStream (OutputStream out) {
		dataStream = new DataOutputStream( out );
	}

	private void pad() throws IOException {
		for (int i = 0; i < dataStream.size() % 4; i++) {
			dataStream.writeByte(0);
		}
	}

	/**
	 * Writes a 4 byte integer to the underlying stream
	 * 
	 * @param v  integer value
	 * 
	 * @throws IOException
	 */
	public void writeInt (int v) throws IOException {
		dataStream.writeInt(v);
	}


	/**
	 * Writes a long value as an 8 byte hyper to the underlying stream
	 * 
	 * @param v  long value
	 * 
	 * @throws IOException
	 */
	public void writeHyper (long v) throws IOException {
		dataStream.writeLong(v);
	}

	/**
	 * Write a single percision float value to the underlying stream
	 * 
	 * @param v  float value
	 * 
	 * @throws IOException
	 */
	public void writeFloat (float v) throws IOException {
		dataStream.writeFloat(v);
	}

	/**
	 * Writes a single byte value to the underlying stream
	 * 
	 * @param b  byte value
	 * 
	 * @throws IOException
	 */
	public void writeByte (int b) throws IOException {
		dataStream.writeByte(b);
	}

	/**
	 * Writes a string of arbitrary bytes to the underlying stream
	 * 
	 * @param b  array of bytes
	 * 
	 * @throws IOException
	 */
	public void writeOpaque (byte [] b) throws IOException {
		writeInt( b.length );
		dataStream.write(b);
		pad();
	}

	/**
	 * Writes a string to the underlying stream
	 * 
	 * @param s  string
	 * 
	 * @throws IOException
	 */
	public void writeString (String s) throws IOException {
		this.writeOpaque(s.getBytes());
	}

	/**
	 * Closes the underlying stream
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		dataStream.close();
	}
}
