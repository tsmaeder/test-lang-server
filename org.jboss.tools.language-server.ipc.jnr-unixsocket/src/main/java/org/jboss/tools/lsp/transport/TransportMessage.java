package org.jboss.tools.lsp.transport;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * Adopted from org.eclipse.wst.jsdt.chromium.internal.transport.Message
 *
 */
public class TransportMessage {

	/** The usual Logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(TransportMessage.class);

	private static final String HEADER_TERMINATOR = "\r\n";

	private static String FIELD_SEPARATOR = ":";

	private static final String CT_VSCODE = "application/vscode-jsonrpc";

	private static final String CONTENT_TYPE = "Content-Type";
	private static final String CONTENT_LENGTH = "Content-Length";

	private final String content;

	/**
	 * Constructor.
	 * 
	 * @param content
	 *            the message content
	 */
	public TransportMessage(String content) {
		this.content = content;
	}

	/**
	 * @return the message content.
	 */
	public String getContent() {
		return content;
	}

	public void send(final ByteChannel channel, final Charset charset) throws IOException {
		if (!charset.name().equalsIgnoreCase("UTF-8")) {
			writeHeaderField(CONTENT_TYPE, CT_VSCODE + ";charset=" + charset.name(), channel, charset);
		}
		final String contentLength = String.valueOf(content.getBytes(charset).length);
		LOGGER.debug("Sending a message with content length={}", contentLength);
		writeHeaderField(CONTENT_LENGTH, contentLength, channel, charset);
		channel.write(bufferize(HEADER_TERMINATOR, charset));
		channel.write(bufferize(content, charset));
	}

	private static void writeHeaderField(final String name, final String value, final ByteChannel channel,
			final Charset charset) throws IOException {
		final String header = new StringBuilder().append(name).append(FIELD_SEPARATOR).append(value)
				.append(HEADER_TERMINATOR).toString();
		channel.write(bufferize(header, charset));
	}

	private static ByteBuffer bufferize(final String content, final Charset charset) {
		final byte[] contentBytes = content.getBytes(charset);
		final ByteBuffer buffer = ByteBuffer.allocate(contentBytes.length).put(contentBytes);
		buffer.flip();
		return buffer;
	}

	@Override
	public String toString() {
		return this.content.toString();
	}

	/**
	 * Reads data from the given channel and returns a {@link TransportMessage}.
	 * 
	 * @param channel
	 *            the channel to read from
	 * @param charset
	 *            the character set to apply when converting incoming bytes into
	 *            a character sequence
	 * @return one or more {@link TransportMessage} read from the channel
	 * @throws IOException
	 */
	public static List<TransportMessage> fromChannel(final ByteChannel channel, final Charset charset)
			throws IOException {
		final List<TransportMessage> transportMessages = new ArrayList<>();
		final Map<String, String> headers = new LinkedHashMap<>();
		int contentLength = 0;
		final LineReader reader = new LineReader(channel);
		do {
			// read headers
			while (true) {
				String line = reader.readLine(charset);
				if (line == null) {
					LOGGER.debug("No data in stream for now");
					return transportMessages;
				}
				if (line.length() == 0) {
					break; // end of headers
				}
				int semiColonPos = line.indexOf(':');
				if (semiColonPos == -1) {
					LOGGER.error("Bad header line: {}", line);
					return null;
				}
				String name = line.substring(0, semiColonPos);
				String value = line.substring(semiColonPos + 1);
				String trimmedValue = value.trim();
				if (CONTENT_LENGTH.equals(name)) {
					contentLength = Integer.valueOf(trimmedValue.trim());
					LOGGER.debug("Incoming message with content length={}", contentLength);
				} else {
					headers.put(name, trimmedValue);
				}
			}
			// Read payload if applicable
			final ByteBuffer contentBuffer = ByteBuffer.allocate(contentLength);
			contentBuffer.clear();
			int totalRead = 0;
			while (totalRead < contentLength) {
				int readBytes = reader.read(contentBuffer, totalRead, contentLength - totalRead);
				if (readBytes == -1) {
					// End-of-stream (browser closed?)
					LOGGER.debug("End of stream while reading content");
					return null;
				}
				totalRead += readBytes;
			}
			LOGGER.debug("Done with reading {} bytes", totalRead);
			// contentBuffer.position(totalRead);
			contentBuffer.flip();
			final byte[] content = new byte[totalRead];
			contentBuffer.get(content, 0, totalRead);
			// final String contentString = new String(contentBuffer.array(),
			// charset);
			// FIXME: problem here: message received is "Content-Length:113" as
			// if
			// the read buffer was not consumed before..
			final String message = new String(content, StandardCharsets.UTF_8);
			LOGGER.debug("Transported message: '{}' (content length={})", message, content.length);
			transportMessages.add(new TransportMessage(message));
		} while (reader.hasRemaining());
		return transportMessages;
	}

}
