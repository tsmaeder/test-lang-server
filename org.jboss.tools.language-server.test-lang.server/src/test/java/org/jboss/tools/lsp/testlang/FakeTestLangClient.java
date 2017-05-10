/*******************************************************************************
 * Copyright (c) 2016, 2017 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.jboss.tools.lsp.testlang;

import jnr.unixsocket.UnixServerSocketChannel;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.messages.NotificationMessage;
import org.eclipse.lsp4j.jsonrpc.messages.RequestMessage;
import org.eclipse.lsp4j.services.LanguageServer;
import org.jboss.tools.lsp.ext.ExtendedLanguageClient;
import org.jboss.tools.lsp.ext.StatusReport;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Simulates the behavior of an IDE that supports the Language Server Protocol.
 * This fake IDE creates 2 named pipes during its own start-up, then sets their
 * locations in system properties. The {@link TestLanguageServer} will connect
 * on these named pipes (as a client in a traditional client/server
 * communication), and the it will received requests/messages from this
 * {@link FakeTestLangClient} (acting as a server to process incoming requests).
 */
public class FakeTestLangClient extends ExternalResource implements ExtendedLanguageClient {

	/** The usual Logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FakeTestLangClient.class);

	private List<String> expectedMessageTypes = new ArrayList<>();

	private Future<Future<?>> connected;

	private final File fromServerAddress;
	private final File toServerAddress;

	private CountDownLatch allMessagesReceived;
	private LanguageServer languageServer;

	private UnixSocketChannel fromServer;
	private UnixSocketChannel toServer;

	/**
	 * Constructor
	 * 
	 * @param serverToIdeSocketFile
	 * @param ideToServerSocketFile
	 */
	public FakeTestLangClient(final File serverToIdeSocketFile, final File ideToServerSocketFile) {
		this.fromServerAddress = serverToIdeSocketFile;
		this.toServerAddress = ideToServerSocketFile;
	}

	@Override
	protected void before() throws Throwable {
		LOGGER.info("before test method");

		// set system properties so the 'test lang' server can look-up the Unix
		// socket location
		System.setProperty(TestLanguageServer.STDOUT_PIPE_NAME, fromServerAddress.getAbsolutePath());
		System.setProperty(TestLanguageServer.STDIN_PIPE_NAME, toServerAddress.getAbsolutePath());
		UnixServerSocketChannel fromServerSocket = listen(fromServerAddress);
		UnixServerSocketChannel toServerSocket = listen(toServerAddress);
		final ExecutorService pool = Executors.newFixedThreadPool(3);
		Future<UnixSocketChannel> fromServerFuture = pool.submit(() -> {
			try {
				return fromServerSocket.accept();
			} finally {
				fromServerSocket.close();
			}
		});
		Future<UnixSocketChannel> toServerFuture = pool.submit(() -> {
			try {
				return toServerSocket.accept();
			} finally {
				toServerSocket.close();
			}
		});

		connected = pool.submit(() -> {
			fromServer = fromServerFuture.get();
			toServer = toServerFuture.get();

			InputStream in = Channels.newInputStream(fromServer);
			OutputStream out = Channels.newOutputStream(toServer);

			Function<MessageConsumer, MessageConsumer> logMessages = (handler) -> {
				return (message) -> {
					if (message instanceof NotificationMessage) {
						handleMessageReceived(((NotificationMessage) message).getMethod());
					} else if (message instanceof RequestMessage) {
						handleMessageReceived(((RequestMessage) message).getMethod());
					}
					handler.consume(message);
				};
			};
			Launcher<LanguageServer> launcher = Launcher.createLauncher(FakeTestLangClient.this, LanguageServer.class,
					in, out, Executors.newCachedThreadPool(), logMessages);
			languageServer = launcher.getRemoteProxy();
			LOGGER.info("Created client launcher");
			return launcher.startListening();
		});
	}

	private static UnixServerSocketChannel listen(final File socketFile) throws IOException {
		socketFile.delete();
		final UnixSocketAddress address = new UnixSocketAddress(socketFile);
		UnixServerSocketChannel channel = UnixServerSocketChannel.open();
		channel.socket().bind(address);
		LOGGER.info("Created socket address at " + socketFile.getAbsolutePath());
		return channel;
	}

	/**
	 * Blocks until both connections are established.
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void waitForConnections() throws InterruptedException, ExecutionException {
		LOGGER.info("waiting for connection from server");
		connected.get();
		LOGGER.info("connected");
	}

	@Override
	protected void after() {
		try {
			languageServer.shutdown().get();
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error("Failed to stop server", e);
		}
		closeChannel(fromServer);
		closeChannel(toServer);
	}

	private void closeChannel(UnixSocketChannel channel) {
		try {
			if (channel != null) {
				LOGGER.info("Closing channel {}", channel);
				channel.close();
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
		InitializeParams params = new InitializeParams();
		params.setRootPath(System.getProperty("java.io.tmpdir"));
		languageServer.initialize(params);
	}

	/**
	 * Sends a "textDocument/didSave" notification to the server.
	 * 
	 * @param documentUri
	 *            the URI of the document that was saved
	 */
	public void sendDidSaveNotification(final String documentUri) {
		DidSaveTextDocumentParams params = new DidSaveTextDocumentParams();
		params.setTextDocument(new TextDocumentIdentifier(documentUri));
		languageServer.getTextDocumentService().didSave(params);
	}

	/**
	 * Set the client to expect a set of message types
	 * 
	 * @param messageTypes
	 *            the expected types of messages to received
	 * @throws InterruptedException
	 */
	public void expectMessages(final String... messageTypes) throws InterruptedException {
		synchronized (expectedMessageTypes) {
			allMessagesReceived = new CountDownLatch(1);
			expectedMessageTypes.clear();
			expectedMessageTypes.addAll(Arrays.asList(messageTypes));
		}
	}

	/**
	 * Wait for the expected message types to arrive, but at most the given time
	 * 
	 * @param messageTypes
	 *            the expected types of messages to received
	 * @throws InterruptedException
	 */
	public boolean waitForMessages(final long timeoutInMillis) throws InterruptedException {
		if (allMessagesReceived != null) {
			return allMessagesReceived.await(timeoutInMillis, TimeUnit.MILLISECONDS);
		}
		return true;
	}

	private void handleMessageReceived(String method) {
		LOGGER.info("Received message of type '{}'", method);
		synchronized (expectedMessageTypes) {
			expectedMessageTypes.remove(method);
			if (allMessagesReceived != null && expectedMessageTypes.isEmpty()) {
				allMessagesReceived.countDown();
			}
		}
	}

	@Override
	public void telemetryEvent(Object object) {
		// TODO Auto-generated method stub

	}

	@Override
	public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showMessage(MessageParams messageParams) {
		// TODO Auto-generated method stub

	}

	@Override
	public CompletableFuture<Void> showMessageRequest(ShowMessageRequestParams requestParams) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void logMessage(MessageParams message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void statusEvent(StatusReport status) {
		// TODO Auto-generated method stub

	}
}
