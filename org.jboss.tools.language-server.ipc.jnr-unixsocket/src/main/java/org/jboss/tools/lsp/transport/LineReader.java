// Copyright (c) 2010 The Chromium Authors. All rights reserved.
// This program and the accompanying materials are made available
// under the terms of the Eclipse Public License v1.0 which accompanies
// this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html

package org.jboss.tools.lsp.transport;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * A helper reader class that allows to read LF-terminated lines and fixed-sized
 * blocks of bytes in turn. To keep it fast it stores the data inside its
 * internal buffer.
 * 
 * Copied from Eclipse JSDT project.
 */
class LineReader {

	/** The usual Logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(LineReader.class);

	private static final byte LF_BYTE = '\n';
	private static final byte CR_BYTE = '\r';

	/** The source channel. */
	private final ByteChannel channel;

	/** Internal main buffer. Should be kept in 'read' (flipped) state. */
	private final ByteBuffer buffer;

	/**
	 * Constructor
	 * 
	 * @param channel
	 *            the channel to read from
	 */
	LineReader(final ByteChannel channel) {
		this.channel = channel;
		this.buffer = ByteBuffer.allocate(128);
		this.buffer.flip();
	}

	/**
	 * Reads data from the channel and writes it in the given
	 * {@link ByteBuffer}.
	 * 
	 * @param targetBuffer
	 *            the buffer in which to write.
	 * @param remaining
	 *            the number of remaining characters to read
	 * @return The number of bytes read, possibly zero, or -1 if the channel has
	 *         reached end-of-stream
	 * @throws IOException
	 *             if something went wrong while reading from the channel.
	 */
	public int read(final ByteBuffer targetBuffer, final int position, final int remaining) throws IOException {
		if (!this.buffer.hasRemaining()) {
			this.buffer.clear();
			LOGGER.debug("No remaining data avaiable. Reading again from the underlying channel...");
			final int bytesRead = this.channel.read(buffer);
			buffer.flip();
			if(bytesRead == -1) {
				throw new IllegalStateException("Reached end of stream while still expecting " + remaining + " bytes");
			}
			return read(targetBuffer, position, remaining);
		}
		//buffer.array() buffer.position()
		final int length = Math.min(remaining, buffer.remaining());
		buffer.get(targetBuffer.array(), targetBuffer.position(), length);
		targetBuffer.position(targetBuffer.position() + length);
		// targetBuffer.position(targetBuffer.position() + length);
		LOGGER.debug("Reading '{}'", new String(targetBuffer.array(), StandardCharsets.UTF_8));
		// should we flip here ?
		// buffer.flip();
		return length;
	}

	/**
	 * Method has similar semantics to {@link BufferedReader#readLine()} method.
	 * 
	 * @param charset
	 * @return the line that was read, or <code>null</code> if the end of stream
	 *         was reached.
	 * @throws IOException
	 */
	public String readLine(Charset charset) throws IOException {
		ByteBuffer lineBuffer = ByteBuffer.allocate(1024);
		while (channel != null) {
			// copy the content of the buffer into another buffer and until a
			// line break is reached
			if (buffer.hasRemaining()) {
				boolean lineEndFound = false;
				int pos;
				findingLineEnd: for (pos = buffer.position(); pos < buffer.limit(); pos++) {
					if (buffer.get(pos) == LF_BYTE) {
						lineEndFound = true;
						break findingLineEnd;
					}
				}
				int chunkLen = pos - buffer.position();
				if (chunkLen > 0) {
					// allocate a new bytebuffer if the remaining size is not
					// enough to copy all recent bytes
					if (lineBuffer.remaining() < chunkLen) {
						int newSize = Math.max(lineBuffer.capacity() * 2, lineBuffer.position() + chunkLen);
						ByteBuffer newLineBuffer = ByteBuffer.allocate(newSize);
						lineBuffer.flip();
						newLineBuffer.put(lineBuffer);
						lineBuffer = newLineBuffer;
					}
					// copy from 'buffer' to 'lineBuffer'
					buffer.get(lineBuffer.array(), lineBuffer.position(), chunkLen);
					lineBuffer.position(lineBuffer.position() + chunkLen);
				}
				if (lineEndFound) {
					// Shift position.
					buffer.get();
					break;
				}
			}

			// fill the buffer with data from the socket channel
			assert !buffer.hasRemaining();
			buffer.clear();
			// socket channel in a non-blocking mode will not read more data
			// than what is immediately available in the input buffer.
			int readRes = this.channel.read(buffer);
			LOGGER.debug("Received {} characters", readRes);
			if (readRes <= 0) {
				if (lineBuffer.position() == 0) {
					return null;
				}
				throw new IOException("End of stream while expecting line end");
			}
			buffer.position(readRes);
			buffer.flip();
		}
		if (lineBuffer.position() > 0 && lineBuffer.get(lineBuffer.position() - 1) == CR_BYTE) {
			lineBuffer.position(lineBuffer.position() - 1);
		}
		return new String(lineBuffer.array(), 0, lineBuffer.position(), charset);
	}

	/**
	 * @return <code>true</code> if the buffer associated with the underlying
	 *         channel has remaining data to be processed, <code>false</code>
	 *         otherwise.
	 */
	public boolean hasRemaining() {
		return this.buffer.hasRemaining();
	}
}
