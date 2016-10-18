package org.jboss.tools.lsp.transport.jnr;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jboss.tools.lsp.transport.Connection;
import org.jboss.tools.lsp.transport.TransportMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jnr.enxio.channels.NativeSelectorProvider;
import jnr.unixsocket.UnixSocket;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;

/**
 * {@link Connection} implementation using the {@code jnr-unixsocket} library.
 */
public class UnixSocketConnection implements Connection {

	/** The usual Logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(UnixSocketConnection.class);

	private static String OS = System.getProperty("os.name").toLowerCase();

	private final String readFileName;
	private final String writeFileName;

	// used for windows
	private RandomAccessFile writeFile;
	private RandomAccessFile readFile;
	// used on POSIX
	private UnixSocket writeSocket;
	private UnixSocket readSocket;

	private UnixSocketChannel readChannel;

	private UnixSocketChannel writeChannel;

	private MessageListener listener;

	/**
	 * Constructor.
	 * 
	 * @param readFileName
	 *            the file to use to read incoming data
	 * @param writeFileName
	 *            the file to use to write outgoing data.
	 */
	public UnixSocketConnection(final String readFileName, final String writeFileName) {
		this.readFileName = readFileName;
		this.writeFileName = writeFileName;
	}

	@Override
	public void send(final TransportMessage transportMessage) {
		if (transportMessage != null) {
			try {
				LOGGER.info("Sending message: {}", transportMessage.getContent());
				transportMessage.send(this.writeChannel, StandardCharsets.UTF_8);
			} catch (IOException e) {
				LOGGER.error("Failed to send message", e);
			}
		}
	}

	@Override
	public void close() {
		try {
			if (writeFile != null) {
				writeFile.close();
			}
			if (readFile != null) {
				readFile.close();
			}
			if (readSocket != null) {
				readSocket.getChannel().close();
			}
			if (writeSocket != null) {
				writeSocket.getChannel().close();
			}
		} catch (IOException e) {
			// TODO: handle exception
		}
	}

	private UnixSocketChannel connectReadChannel() throws IOException {
		final File rFile = new File(readFileName);
		// FIXME: see if this is needed on Windows (or use sockets instead of
		// named pipes)
		// if (isWindows()) {
		// readFile = new RandomAccessFile(rFile, "rwd");
		// return readFile.getChannel();
		// }
		LOGGER.info("Connecting read channel to socket at {}", rFile);
		final UnixSocketAddress address = new UnixSocketAddress(rFile);
		final UnixSocketChannel channel = UnixSocketChannel.open(address);
		channel.configureBlocking(false);
		return channel;
	}

	private UnixSocketChannel connectWriteChannel() throws IOException {
		final File wFile = new File(writeFileName);
		// if (isWindows()) {
		// writeFile = new RandomAccessFile(wFile, "rwd");
		// return writeFile.getChannel();
		// }
		LOGGER.info("Connecting write channel to socket at {}", wFile);
		final UnixSocketAddress address = new UnixSocketAddress(wFile);
		final UnixSocketChannel channel = UnixSocketChannel.open(address);
		channel.configureBlocking(false);
		return channel;
	}

	private static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	@Override
	public void start() throws IOException {
		new Thread(() -> {
			try (final Selector selector = NativeSelectorProvider.getInstance().openSelector()) {
				this.readChannel = connectReadChannel();
				this.readChannel.register(selector, SelectionKey.OP_READ, "read");
				this.writeChannel = connectWriteChannel();
				// writeChannel.register(selector, SelectionKey.OP_READ,
				// "write");
				while (true) {
					int readyChannels = selector.select();
					if (readyChannels == 0) {
						continue;
					}
					final Set<SelectionKey> selectedKeys = selector.selectedKeys();
					final Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
					while (keyIterator.hasNext()) {
						final SelectionKey key = keyIterator.next();
						LOGGER.debug("Channel with key attachment '{}' is readable", key.attachment());
						// a channel is ready for reading
						if (key.attachment().equals("read")) {
							final List<TransportMessage> messages = TransportMessage
									.fromChannel((ByteChannel) key.channel(), StandardCharsets.UTF_8);
							if (messages != null) {
								messages.forEach(message -> {
									LOGGER.debug("Received message: {}", message);
									listener.messageReceived(message);
								});
							} else {
								LOGGER.warn("No more message to process.");
							}
						}
						keyIterator.remove();
					}
				}
			} catch (IOException e) {
				LOGGER.error("Error while reading or writing to channels", e);
			} finally {
				LOGGER.warn("Server is shutdown.");
			}

		}, "server-channels").start();

	}

	@Override
	public void setMessageListener(final MessageListener listener) {
		this.listener = listener;
	}

}
