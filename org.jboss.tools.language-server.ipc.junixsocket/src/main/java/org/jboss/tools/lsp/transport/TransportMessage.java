package org.jboss.tools.lsp.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adopted from org.eclipse.wst.jsdt.chromium.internal.transport.Message
 */
public class TransportMessage {

	/** The usual Logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(TransportMessage.class);

	private static final String HEADER_TERMINATOR = "\r\n";

	private static final String FIELD_SEPARATOR = ":";

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
	public TransportMessage(final String content) {
		this.content = content;
	}

	/**
	 * @return the message content.
	 */
	public String getContent() {
		return content;
	}

	void send(final OutputStream outputStream, final Charset charset) throws IOException {
		final StringBuilder transportMessageBuider = new StringBuilder();
		if (!charset.name().equalsIgnoreCase("UTF-8")) {
			transportMessageBuider.append(getHeaderField(CONTENT_TYPE, CT_VSCODE + ";charset=" + charset.name()));
		}
		transportMessageBuider.append(getHeaderField(CONTENT_LENGTH, String.valueOf(content.getBytes(charset).length)));
		transportMessageBuider.append(HEADER_TERMINATOR);
		transportMessageBuider.append(content);
		final String transportMessage = transportMessageBuider.toString();
		LOGGER.debug("Sending transport message:\n{}", transportMessage);
		outputStream.write(transportMessage.getBytes(charset));
	}

	private static String getHeaderField(final String name, final String value) {
		return new StringBuilder().append(name).append(FIELD_SEPARATOR).append(value)
				.append(HEADER_TERMINATOR).toString();
	}

	/**
	 * Reads data from the given channel and returns a {@link TransportMessage}.
	 * 
	 * @param inputStream
	 *            the inputStream to read from
	 * @param charset
	 *            the character set to apply when converting incoming bytes into
	 *            a character sequence
	 * @return the {@link TransportMessage} read from the channel
	 * @throws IOException
	 */
	public static TransportMessage fromStream(final InputStream inputStream, final Charset charset) throws IOException {
		final Map<String, String> headers = new LinkedHashMap<>();
		int contentLength = 0;
		final LineReader reader = new LineReader(inputStream);
		// read headers
		while (true) {
			String line = reader.readLine(charset);
			if (line == null) {
				LOGGER.debug("End of stream");
				return null;
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
			} else {
				headers.put(name, trimmedValue);
			}
		}
		// Read payload if applicable
		final byte[] contentBytes = new byte[contentLength];
		int totalRead = 0;
		while (totalRead < contentLength) {
			int readBytes = reader.read(contentBytes, totalRead, contentLength - totalRead);
			if (readBytes == -1) {
				// End-of-stream (browser closed?)
				LOGGER.debug("End of stream while reading content");
				return null;
			}
			totalRead += readBytes;
		}
		final String contentString = new String(contentBytes, charset);
		return new TransportMessage(contentString);
	}

}
