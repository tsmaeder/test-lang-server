package org.jboss.tools.lsp.testlang.handlers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jboss.tools.lsp.base.LSPMethods;
import org.jboss.tools.lsp.ipc.MessageType;
import org.jboss.tools.lsp.ipc.RequestHandler;
import org.jboss.tools.lsp.messages.DidSaveTextDocumentParams;
import org.jboss.tools.lsp.testlang.DocumentManager;
import org.jboss.tools.lsp.testlang.TestLanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RequestHandler} for when a document is saved. This handler will parse
 * the file and look for:
 * <ul>
 * <li>a line starting with {@code ERROR}</li> and send a <a href=
 * "https://github.com/Microsoft/language-server-protocol/blob/master/protocol.md#publishdiagnostics-notification">publish
 * diagnostic notification</a> with a message based on the line content.
 * <li>a line starting with {@code NOTIFY}</li> and send a <a href=
 * "https://github.com/Microsoft/language-server-protocol/blob/master/protocol.md#showmessage-notification">ShowMessage
 * notification<a/> with a message based on the line content.
 * </ul>
 */
public class DocumentSavedHandler implements RequestHandler<DidSaveTextDocumentParams, Object> {

	/** The usual Logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentSavedHandler.class);

	private final TestLanguageServer testLanguageServer;

	protected static final Pattern showMessagePattern = Pattern
			.compile("window/showMessage:(?<type>\\w+):(?<message>.+)");

	/**
	 * Constructor
	 * 
	 * @param testLanguageServer
	 *            the parent {@link TestLanguageServer}
	 */
	public DocumentSavedHandler(final TestLanguageServer testLanguageServer) {
		this.testLanguageServer = testLanguageServer;
	}

	@Override
	public boolean canHandle(final String request) {
		return LSPMethods.DOCUMENT_SAVED.getMethod().equals(request);
	}

	/**
	 * Handles content in the file
	 */
	@Override
	public Object handle(final DidSaveTextDocumentParams documentChangeParams) {
		LOGGER.info("Handling document saved");
		final DocumentManager documentManager = testLanguageServer.getDocumentManager();
		final String documentUri = documentChangeParams.getTextDocument().getUri();
		try {
			final List<String> lines = documentManager.getContent(documentUri);
			LOGGER.info("Document saved: \n{}", lines.stream().collect(Collectors.joining("\n")));
			// iterate on lines with a counter
			lines.stream().map(line -> showMessagePattern.matcher(line)).filter(matcher -> matcher.matches())
					.forEach(matcher -> {
						final MessageType type = MessageType.from(matcher.group(1));
						final String message = matcher.group(2).trim();
						LOGGER.info("Found line starting with the '{}' keyword:\n{} (type={})",
								LSPMethods.WINDOW_SHOWMESSAGE.getMethod(), message, type.toString().toLowerCase());
						this.testLanguageServer.sendShowMessageNotification(type, message);
					});
		} catch (IOException | URISyntaxException e) {
			LOGGER.error("Failed to read document content at " + documentUri, e);
		}
		return null;
	}

}
