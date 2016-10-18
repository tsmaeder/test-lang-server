/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.jboss.tools.lsp.testlang;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.tools.lsp.base.LSPMethods;
import org.jboss.tools.lsp.base.LSPServer;
import org.jboss.tools.lsp.base.Message;
import org.jboss.tools.lsp.base.MessageJSONHandler;
import org.jboss.tools.lsp.base.NotificationMessage;
import org.jboss.tools.lsp.base.RequestMessage;
import org.jboss.tools.lsp.messages.DidSaveTextDocumentParams;
import org.jboss.tools.lsp.messages.InitializeParams;
import org.jboss.tools.lsp.messages.TextDocumentIdentifier;
import org.jboss.tools.lsp.transport.TransportMessage;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import jnr.enxio.channels.NativeSelectorProvider;
import jnr.unixsocket.UnixServerSocketChannel;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;

/**
 * Simulates the behavior of an IDE that supports the Language Server Protocol.
 * This fake IDE creates 2 named pipes during its own start-up, then sets their
 * locations in system properties. The {@link TestLanguageServer} will connect
 * on these named pipes (as a client in a traditional client/server
 * communication), and the it will received requests/messages from this
 * {@link FakeTestLangClient} (acting as a server to process incoming requests).
 */
public class FakeTestLangClient extends ExternalResource {

	/** The usual Logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FakeTestLangClient.class);

	private final Gson gson = new GsonBuilder().registerTypeAdapter(Message.class, new MessageJSONHandler()).create();;

	private List<Message> messageQueue = new ArrayList<>();
	private List<String> expectedMessageTypes = new ArrayList<>();

	private UnixServerSocketChannel serverToIdeServerSocketChannel;
	private UnixServerSocketChannel ideToServerServerSocketChannel;

	private final File serverToIdeSocketFile;
	private final File ideToServerSocketFile;

	private Future<UnixSocketChannel> serverToIdeSocketChannelFuture;

	private Future<UnixSocketChannel> ideToServerSocketChannelFuture;

	private UnixSocketChannel serverToIdeSocketChannel;

	private UnixSocketChannel ideToServerSocketChannel;

	private static final String CONTENT_LENGTH = "Content-Length";
	private static final String HEADER_TERMINATOR = "\r\n";

	private static String FIELD_SEPARATOR = ":";

	private PrintWriter channelWriter;

	private CountDownLatch allMessagesReceivedLatch;

	/**
	 * Constructor
	 * 
	 * @param serverToIdeSocketFile
	 * @param ideToServerSocketFile
	 */
	public FakeTestLangClient(final File serverToIdeSocketFile, final File ideToServerSocketFile) {
		this.serverToIdeSocketFile = serverToIdeSocketFile;
		this.ideToServerSocketFile = ideToServerSocketFile;
	}

	@Override
	protected void before() throws Throwable {
		this.serverToIdeServerSocketChannel = createServerSocketChannel(this.serverToIdeSocketFile);
		this.ideToServerServerSocketChannel = createServerSocketChannel(this.ideToServerSocketFile);
		// set system properties so the 'test lang' server can look-up the Unix
		// socket location
		System.setProperty(LSPServer.STDIN_PIPE_NAME, serverToIdeSocketFile.getAbsolutePath());
		System.setProperty(LSPServer.STDOUT_PIPE_NAME, ideToServerSocketFile.getAbsolutePath());
		final ExecutorService pool = Executors.newFixedThreadPool(2);
		serverToIdeSocketChannelFuture = pool.submit(() -> {
			return getSocketChannel(this.serverToIdeServerSocketChannel);
		});
		ideToServerSocketChannelFuture = pool.submit(() -> {
			return getSocketChannel(this.ideToServerServerSocketChannel);
		});
		// use a separate thread to process incoming responses from the 'test'
		// language server
		final Thread incomingResponsesThread = new Thread(() -> {
			try {
				while (serverToIdeSocketChannel == null || !serverToIdeSocketChannel.isConnected()) {
					Thread.sleep(50);
				}
			} catch (InterruptedException e) {
				LOGGER.error("Error while waiting for 'IN' channel to be ready", e);
			}
			LOGGER.info("Client is now ready to receive messages from LS server on {}", serverToIdeSocketFile.getAbsolutePath());

			try (final Selector selector = NativeSelectorProvider.getInstance().openSelector()) {
				serverToIdeSocketChannel.configureBlocking(false);
				serverToIdeSocketChannel.register(selector, SelectionKey.OP_READ, "read");
				while (true) {
					int readyChannels = selector.select();
					if (readyChannels == 0) {
						continue;
					}
					final Set<SelectionKey> selectedKeys = selector.selectedKeys();
					final Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
					while (keyIterator.hasNext()) {
						final SelectionKey key = keyIterator.next();
						if (key.isAcceptable()) {
							// a connection was accepted by a
							// ServerSocketChannel.
						} else if (key.isConnectable()) {
							// a connection was established with a remote
							// server.
						} else if (key.isReadable()) {
							// a channel is ready for reading
							if (key.attachment().equals("read")) {
								final List<TransportMessage> messages = TransportMessage
										.fromChannel((ByteChannel) key.channel(), StandardCharsets.UTF_8);
								if (messages != null) {
									messages.forEach(message -> {
										LOGGER.debug("Received message: {}", message);
										messageReceived(message);
									});
								} else {
									LOGGER.info("No incoming message for now...");
								}
							}
						} else if (key.isWritable()) {
							// a channel is ready for writing
						}
						keyIterator.remove();
					}
				}
			} catch (IOException e) {
				LOGGER.error("Error while reading or writing to channels", e);
			} finally {
				LOGGER.warn("Client is shutdown.");
			}

		}, "incoming-responses-thread");
		// no need to wait for the thread termination to stop the JVM
		incomingResponsesThread.setDaemon(true);
		incomingResponsesThread.start();
	}

	private static UnixServerSocketChannel createServerSocketChannel(final File socketFile) throws IOException {
		if (socketFile.exists()) {
			LOGGER.debug("Removing previous {} file", socketFile);
			socketFile.delete();
		}
		final UnixSocketAddress address = new UnixSocketAddress(socketFile);
		final UnixServerSocketChannel channel = UnixServerSocketChannel.open();
		channel.configureBlocking(false);
		channel.socket().bind(address);
		LOGGER.info("Created socket address at " + socketFile.getAbsolutePath());
		return channel;
	}

	private static UnixSocketChannel getSocketChannel(final UnixServerSocketChannel serverSocketChannel) {
		try (final Selector selector = NativeSelectorProvider.getInstance().openSelector()) {
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, null);
			// new SocketActor(this.serverSocketInChannel));
			while (selector.select() > 0) {
				final Set<SelectionKey> keys = selector.selectedKeys();
				final Iterator<SelectionKey> iterator = keys.iterator();
				while (iterator.hasNext()) {
					// final SelectionKey selectionKey = iterator.next();
					// final SocketActor socketActor = (SocketActor)
					// selectionKey.attachment();
					final UnixSocketChannel socketInChannel = serverSocketChannel.accept();
					// iterator.remove();
					return socketInChannel;
				}
			}
		} catch (IOException ex) {
			LOGGER.error(ex.getMessage(), ex);
		}
		return null;
	}

	/**
	 * Blocks until both connections are established.
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void waitForConnections() throws InterruptedException, ExecutionException {
		LOGGER.info("Waiting for connections on the 'IN' and 'OUT' server socket channels...");
		this.serverToIdeSocketChannel = serverToIdeSocketChannelFuture.get();
		LOGGER.info("New connection established on the 'IN' channel");
		this.ideToServerSocketChannel = ideToServerSocketChannelFuture.get();
		LOGGER.info("New connection established on the 'OUT' channel");
		this.channelWriter = new PrintWriter(Channels.newOutputStream(this.ideToServerSocketChannel));
	}

	@Override
	protected void after() {
		try {
			if (this.serverToIdeServerSocketChannel != null) {
				LOGGER.info("Closing channel {}", this.serverToIdeServerSocketChannel.toString());
				this.serverToIdeServerSocketChannel.close();
			}
			if (this.ideToServerServerSocketChannel != null) {
				LOGGER.info("Closing channel {}", this.ideToServerServerSocketChannel.toString());
				this.ideToServerServerSocketChannel.close();
			}
			if (this.channelWriter != null) {
				this.channelWriter.close();
			}
		} catch (IOException e) {
			LOGGER.error("Failed to close socket channel", e);
		}
	}

	/**
	 * Sends the Initialize request
	 * (https://github.com/Microsoft/language-server-protocol/blob/master/protocol.md#initialize-request)
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void sendInitializeRequest() throws InterruptedException, IOException {
		final RequestMessage<InitializeParams> initRequestMessage = new RequestMessage<>();
		initRequestMessage.setMethod(LSPMethods.INITIALIZE.getMethod());
		initRequestMessage.setParams(new InitializeParams().withRootPath(System.getProperty("java.io.tmpdir")));
		// initRequestMessage.setParams(null);
		final String initRequestJsonMessage = gson.toJson(initRequestMessage);
		sendMessage(initRequestJsonMessage);
	}

	/**
	 * Sends a "textDocument/didSave" notification to the server.
	 * 
	 * @param documentUri
	 *            the URI of the document that was saved
	 */
	public void sendDidSaveNotification(final String documentUri) {
		final NotificationMessage<DidSaveTextDocumentParams> didSaveDocumentMessage = new NotificationMessage<>();
		didSaveDocumentMessage.setMethod(LSPMethods.DOCUMENT_SAVED.getMethod());
		didSaveDocumentMessage.setParams(
				new DidSaveTextDocumentParams().withTextDocument(new TextDocumentIdentifier().withUri(documentUri)));
		final String initRequestJsonMessage = gson.toJson(didSaveDocumentMessage);
		sendMessage(initRequestJsonMessage);
	}

	private void sendMessage(final String content) {
		final int contentLength = content.getBytes(StandardCharsets.UTF_8).length;
		final String transportMessage = new StringBuilder()
				.append(headerField(CONTENT_LENGTH, Integer.toString(contentLength))).append(HEADER_TERMINATOR)
				.append(content).toString();
		LOGGER.info("Sending message \n{} ({} chars)", transportMessage, transportMessage.length());
		this.channelWriter.print(transportMessage);
		this.channelWriter.flush();
	}

	private static String headerField(final String headerName, final String headerValue) {
		return new StringBuilder().append(headerName).append(FIELD_SEPARATOR).append(headerValue)
				.append(HEADER_TERMINATOR).toString();
	}

	// @Override
	private void messageReceived(final TransportMessage transportMessage) {
		try {
			LOGGER.info("Received server response: {} (length={})", transportMessage.getContent(),
					transportMessage.getContent().length());
			final String jsonMessage = transportMessage.getContent().trim();
			final Message message = gson.fromJson(jsonMessage, Message.class);
			this.messageQueue.add(message);
			countDownLatchOnAllMessagesReceived();
		} catch (JsonSyntaxException e) {
			LOGGER.error("Error while reading the transport message: ", e.getMessage());
			throw e;
		}
	}

	/**
	 * Blocks until the sequence of response with the given types is met
	 * 
	 * @param timeoutInMillis
	 *            the timeout in milliseconds
	 * @param messageTypes
	 *            the expected types of messages to received
	 * @throws InterruptedException
	 */
	public void waitforResponses(final long timeoutInMillis, final String... messageTypes) throws InterruptedException {
		this.allMessagesReceivedLatch = new CountDownLatch(1);
		this.expectedMessageTypes.clear();
		Stream.of(messageTypes).forEach(t -> this.expectedMessageTypes.add(t));
		countDownLatchOnAllMessagesReceived();
		allMessagesReceivedLatch.await(timeoutInMillis, TimeUnit.MILLISECONDS);
	}

	/**
	 * 
	 */
	private synchronized void countDownLatchOnAllMessagesReceived() {
		if (this.allMessagesReceivedLatch != null) {
			LOGGER.debug("Verifying messages received...");
			if (verifyReceivedMessages(this.messageQueue, this.expectedMessageTypes)) {
				LOGGER.info("Releasing latch after all expected messages were received");
				allMessagesReceivedLatch.countDown();
			} else {
				LOGGER.debug("Not all messages received yet. Only got {} out of {} expected.", this.messageQueue.size(),
						expectedMessageTypes.size());
			}
		}
	}

	/**
	 * @return true when all expected messages have been received.
	 */
	public boolean verifyMessages() {
		return verifyReceivedMessages(this.messageQueue, this.expectedMessageTypes);
	}

	/**
	 * Verifies that the types of the message received match the expectations.
	 * If so, the latch can be released.
	 * 
	 * @param messageQueue
	 * @param expectedMessageTypes
	 */
	private static boolean verifyReceivedMessages(final List<Message> messageQueue,
			final List<String> expectedMessageTypes) {
		return messageQueue.stream().filter(m -> m instanceof NotificationMessage)
				.map(m -> ((NotificationMessage<?>) m).getMethod()).collect(Collectors.toList())
				.equals(expectedMessageTypes);
	}

}
