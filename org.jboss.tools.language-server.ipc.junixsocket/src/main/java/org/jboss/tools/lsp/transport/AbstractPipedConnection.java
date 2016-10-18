package org.jboss.tools.lsp.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for piped connections.
 */
public abstract class AbstractPipedConnection implements Connection {

	/** The usual Logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPipedConnection.class);

	private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	private class ReaderThread extends Thread {

		private final InputStream stream;

		public ReaderThread(InputStream input) {
			super("reader-" + role);
			this.stream = input;
		}

		@Override
		public void run() {
			startDispatcherThread();
			startWriterThread();
			while (true) {
				TransportMessage message;
				try {
					LOGGER.debug("Waiting for message in {}'s incoming queue...", role);
					message = TransportMessage.fromStream(stream, DEFAULT_CHARSET);
					if (message == null) {
						// Stream disconnected exit reader thread
						LOGGER.error("Empty message read");
						break;
					}
					LOGGER.debug("Adding message to {}'s incoming queue: {}", role, message.getContent());
					inboundQueue.add(message);
				} catch (IOException e) {
					LOGGER.error("Error while receiving message (" + role + " side)", e);
					break;
				}
			}
			LOGGER.warn("Stopping {}'s read thread.", role);
		}

		private void startWriterThread() {
			try {
				WriterThread writerThread = new WriterThread(connectWriteChannel());
				writerThread.setDaemon(true);
				writerThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Must be called by implementers to start dispatching of incoming
		 * messages.
		 */
		protected void startDispatcherThread() {
			DispatcherThread dispatcherThread;
			dispatcherThread = new DispatcherThread();
			dispatcherThread.setDaemon(true);
			dispatcherThread.start();
		}
	}

	private class WriterThread extends Thread {

		private final OutputStream stream;

		public WriterThread(OutputStream output) {
			super("writer-" + role);
			this.stream = output;
		}

		@Override
		public void run() {
			while (true) {
				try {
					LOGGER.debug("Waiting for message in {}'s outgoing queue...", role);
					final TransportMessage message = outboundQueue.take();
					LOGGER.info("Sending message: {}", message.getContent());
					message.send(stream, DEFAULT_CHARSET);
				} catch (InterruptedException e) {
					break;// exit
				} catch (IOException e) {
					LOGGER.error("Error while sending message (" + role + " side)", e);
				}
			}
			LOGGER.warn("Stopping {}'s write thread.", role);
		}
	}

	/**
	 * Dispatches the messages received
	 */
	private class DispatcherThread extends Thread {
		public DispatcherThread() {
			super();
		}

		@Override
		public void run() {
			TransportMessage message;
			try {
				while (true) {
					message = inboundQueue.take();
					if (listener != null) {
						try {
							LOGGER.debug("Dispatch incoming message in {}: {}", role, message.getContent());
							listener.messageReceived(message);
						} catch (Exception e) {
							LOGGER.debug("Exception on incoming message dispatcher", e);
						}
					}
				}
			} catch (InterruptedException e) {
				// stop the dispatcher thread
			}
		}
	}

	private final String role;

	private MessageListener listener;

	protected final BlockingQueue<TransportMessage> inboundQueue = new LinkedBlockingQueue<>();
	protected final BlockingQueue<TransportMessage> outboundQueue = new LinkedBlockingQueue<>();

	/**
	 * Constructor
	 * @param role the connection role, to be used when naming the threads used to read or write from the underlying channels
	 */
	public AbstractPipedConnection(final String role) {
		this.role = role;
	}

	public String getRole() {
		return this.role;
	}

	@Override
	public void setMessageListener(MessageListener listener) {
		if (this.listener != null && this.listener == listener) {
			throw new IllegalStateException("Can not set listener multiple times");
		}
		this.listener = listener;
	}

	@Override
	public void start() throws IOException {
		final ReaderThread readerThread = new ReaderThread(connectReadChannel());
		readerThread.setDaemon(true);
		readerThread.start();
	}

	public abstract InputStream connectReadChannel() throws IOException;

	public abstract OutputStream connectWriteChannel() throws IOException;

}
