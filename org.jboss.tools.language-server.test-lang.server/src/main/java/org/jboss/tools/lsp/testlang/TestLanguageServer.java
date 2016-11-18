package org.jboss.tools.lsp.testlang;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.tools.lsp.base.LSPMethods;
import org.jboss.tools.lsp.base.LSPServer;
import org.jboss.tools.lsp.base.NotificationMessage;
import org.jboss.tools.lsp.ext.StatusReport;
import org.jboss.tools.lsp.ipc.MessageType;
import org.jboss.tools.lsp.ipc.RequestHandler;
import org.jboss.tools.lsp.ipc.ServiceStatus;
import org.jboss.tools.lsp.messages.LogMessageParams;
import org.jboss.tools.lsp.messages.ShowMessageParams;
import org.jboss.tools.lsp.testlang.handlers.DocumentSavedHandler;
import org.jboss.tools.lsp.testlang.handlers.ExitHandler;
import org.jboss.tools.lsp.testlang.handlers.InitHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Language Server Protocol implementation for the 'test-lang'.
 */
public class TestLanguageServer extends LSPServer {

	/** The usual Logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(TestLanguageServer.class);
	
	private final DocumentManager documentManager; 

	/**
	 * Launcher for the command-line.
	 * @param args the command line arguments (none is expected)
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		final TestLanguageServer server = new TestLanguageServer();
		server.start();
		while (true) {
			Thread.sleep(TimeUnit.MINUTES.toMillis(1));
		}
	}
	
	/**
	 * Default constructor.
	 */
	public TestLanguageServer() {
		this.documentManager = new DefaultDocumentManager();
	}
	
	/**
	 * Constructor with a custom {@link DocumentManager}.
	 * @param documentManager the custom document manager to use.
	 */
	public TestLanguageServer(final DocumentManager documentManager) {
		this.documentManager = documentManager;
	}
	
	/**
	 * @return the document manager associated with this server.
	 */
	public DocumentManager getDocumentManager() {
		return this.documentManager;
	}
	

	/**
	 * Sends the given <code>log message notification</code> back to the client as a notification
	 * 
	 * @param type the type of message
	 * @param msg
	 *            The message to send back to the client
	 */
	public void sendLogMessageNotification(final MessageType type, final String msg) {
		NotificationMessage<LogMessageParams> message = new NotificationMessage<>();
		message.setMethod(LSPMethods.WINDOW_LOGMESSAGE.getMethod());
		message.setParams(new LogMessageParams().withMessage(msg).withType(Double.valueOf(type.getType())));
		send(message);
	}

	/**
	 * Sends the given <code>show message notification</code> back to the client as a notification
	 * 
	 * @param type the type of message
	 * @param msg
	 *            The message to send back to the client
	 */
	public void sendShowMessageNotification(final MessageType type, final String msg) {
		final NotificationMessage<ShowMessageParams> message = new NotificationMessage<>();
		message.setMethod(LSPMethods.WINDOW_SHOWMESSAGE.getMethod());
		message.setParams(new ShowMessageParams().withMessage(msg).withType(type.getType()));
		send(message);
	}

	/**
	 * Sends a status to the client to be presented to users
	 * @param serverStatus the {@link ServiceStatus}
	 * @param status
	 *            The status to send back to the client
	 */
	public void sendStatus(final ServiceStatus serverStatus, final String status) {
		final NotificationMessage<StatusReport> message = new NotificationMessage<>();
		message.setMethod(LSPMethods.LANGUAGE_STATUS.getMethod());
		message.setParams(new StatusReport().withMessage(status).withType(serverStatus.name()));
		send(message);
	}

	/**
	 * Start the language server, in a separate, daemon {@link Thread}.
	 */
	public void start() {
		new Thread(() -> {
			try {
				LOGGER.info("Starting 'test' language server");
				final List<RequestHandler<?, ?>> handlers = new ArrayList<>();
				handlers.add(new ExitHandler());
				handlers.add(new InitHandler(this));
				handlers.add(new DocumentSavedHandler(this));
				connect(handlers);
			} catch (IOException e) {
				LOGGER.error("Failed to start 'test' language server", e);
			}
		}, "test-language-server-main").start();
	}

	/**
	 * Stops the server.
	 */
	public void stop() {
		super.shutdown();
	}

}
