/*
 * Copyright 2012 MicroStrain Inc. All Rights Reserved.
 *
 * Distributed under the Simplified BSD License.
 * See file license.txt
 *
 */

package org.cnc.mombot.sensorcloud;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * Provides a wrapper for java's DataInputStream so parse data using XDR specifications.
 * 
 *
 */
public class XDRInStream {

	private DataInputStream dataStream;
	private int bytesRead;

	private final static int MAX_BYTES = Integer.getInteger("hep.io.xdr.sanityCheck",100000).intValue();

	/**
	 * @param in  input stream to read data from
	 */
	public XDRInStream(InputStream in) {
		dataStream = new DataInputStream(in);
		bytesRead = 0;
	}

	private void pad() throws IOException {
		int off =  4 - bytesRead % 4;
		if (off != 4) {
			dataStream.skipBytes( off );
			bytesRead += off;
		}
	}

	/**
	 * Reads a 4 byte integer from the underlying stream
	 * 
	 * @return Parsed integer
	 * 
	 * @throws IOException
	 */
	public int readInt() throws IOException {
		return dataStream.readInt();
	}

	/**
	 * Reads an 8 byte value from the underlying stream.
	 * The Java Long data type is equivalent to the XDR Hyper.
	 * 
	 * @return Hyper value as a Long
	 * @throws IOException
	 */
	public long readHyper() throws IOException {
		return dataStream.readLong();
	}


	/**
	 * Reads a single point precision value from the underlying stream
	 * 
	 * @return Float value 
	 * 
	 * @throws IOException
	 */
	public float readFloat() throws IOException {
		return dataStream.readFloat();
	}


	/**
	 * Reads an integer from the underlying stream and returns that number of bytes
	 * 
	 * @return Byte array
	 * 
	 * @throws IOException
	 */
	public byte [] readOpaque() throws IOException {
		int i = readInt();

		if (i > MAX_BYTES) {
	         throw new IOException("Opaque length failed sanity check: " + i);
		}

		return readBytes(i);
	}

	/**
	 * Has the same effect as readOpaque() but returns the byte array as an ASCII string
	 * 
	 * @return US-ASCII String
	 * 
	 * @throws IOException
	 */
	public String readString() throws IOException {
		int i = readInt();

		if (i > MAX_BYTES) {
	         throw new IOException("String length failed sanity check: " + i);
		}

		return new String( readBytes(i), "US-ASCII" );
	}

	/**
	 * Reads a specified number of bytes from the underlying stream
	 * 
	 * @param i  number of bytes to be read
	 * @return  Byte array
	 * 
	 * @throws IOException
	 */
	public byte [] readBytes (int i) throws IOException {
		bytesRead += i;
		byte [] bytes = new byte[i];
		dataStream.readFully( bytes );
		pad();
		return bytes;
	}

	/**
	 * Skips a specified number of bytes from the underlying stream
	 * 
	 * @param i  number of bytes to be skipped
	 * 
	 * @throws IOException
	 */
	public void skip (int i) throws IOException {
		bytesRead += i;
		dataStream.skipBytes(i);
		pad();
	}
}
