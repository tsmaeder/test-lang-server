package org.jboss.tools.lsp.testlang.handlers;

import org.jboss.tools.lsp.base.LSPMethods;
import org.jboss.tools.lsp.ipc.RequestHandler;
import org.jboss.tools.lsp.ipc.ServiceStatus;
import org.jboss.tools.lsp.messages.CodeLensOptions;
import org.jboss.tools.lsp.messages.CompletionOptions;
import org.jboss.tools.lsp.messages.InitializeParams;
import org.jboss.tools.lsp.messages.InitializeResult;
import org.jboss.tools.lsp.messages.ServerCapabilities;
import org.jboss.tools.lsp.testlang.TestLanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handler for the extension life cycle events.
 *
 */
final public class InitHandler implements RequestHandler<InitializeParams, InitializeResult> {
	
	/** The usual Logger.*/
	private static final Logger LOGGER = LoggerFactory.getLogger(InitHandler.class);
	
	private TestLanguageServer connection;

	/**
	 * Constructor.
	 * @param connection the connection to use to send notifications to the client
	 */
	public InitHandler(TestLanguageServer connection) {
		this.connection = connection;
	}

	@Override
	public boolean canHandle(final String request) {
		return LSPMethods.INITIALIZE.getMethod().equals(request);
	}

	
	@Override
	public InitializeResult handle(InitializeParams param) {
		triggerInitialization(param.getRootPath());
		final InitializeResult result = new InitializeResult();
		final ServerCapabilities capabilities = new ServerCapabilities();
		return result.withCapabilities(
				capabilities.withTextDocumentSync(new Double(2))
				.withCompletionProvider(new CompletionOptions().withResolveProvider(Boolean.FALSE))
				.withHoverProvider(Boolean.FALSE)
				.withDefinitionProvider(Boolean.FALSE)
				.withDocumentSymbolProvider(Boolean.FALSE)
				.withWorkspaceSymbolProvider(Boolean.FALSE)
				.withReferencesProvider(Boolean.FALSE)
				.withDocumentHighlightProvider(Boolean.TRUE)
				.withDocumentFormattingProvider(Boolean.FALSE)
				.withDocumentRangeFormattingProvider(Boolean.FALSE)
				.withCodeLensProvider(new CodeLensOptions().withResolveProvider(Boolean.FALSE))
		);
	}

	private void triggerInitialization(final String root) {
		LOGGER.info("Triggering initialization from {}", root);
		this.connection.sendStatus(ServiceStatus.Starting, "Init...");
		this.connection.sendStatus(ServiceStatus.Started, "Ready");
	}
	
}
