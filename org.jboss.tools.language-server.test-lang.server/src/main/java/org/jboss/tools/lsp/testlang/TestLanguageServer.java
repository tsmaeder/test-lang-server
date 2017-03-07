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

import java.io.IOException;
import java.nio.channels.Channels;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.lsp4j.CodeLensOptions;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.jboss.tools.lsp.ext.ExtendedLanguageClient;
import org.jboss.tools.lsp.ext.ServiceStatus;
import org.jboss.tools.lsp.ext.StatusReport;
import org.jboss.tools.lsp.testlang.handlers.TestTextDocumentService;
import org.jboss.tools.lsp.testlang.handlers.TestWorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jnr.unixsocket.UnixSocketChannel;

/**
 * The Language Server Protocol implementation for the 'test-lang'.
 */
public class TestLanguageServer implements LanguageServer {

	/** The usual Logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(TestLanguageServer.class);

	private final DocumentManager documentManager;
	private final TextDocumentService textDocumentService;
	private final WorkspaceService workspaceService;

	private ExtendedLanguageClient languageClient;

	private Future<?> processor;

	public static final String STDOUT_PIPE_NAME = "STDOUT_PIPE_NAME";
	public static final String STDIN_PIPE_NAME = "STDIN_PIPE_NAME";

	/**
	 * Launcher for the command-line.
	 * 
	 * @param args
	 *            the command line arguments (none is expected)
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws ExecutionException
	 */
	public static void main(String[] args) throws InterruptedException, IOException, ExecutionException {
		new TestLanguageServer().start().get();
	}

	public Future<?> start() throws IOException {
		final String stdInName = Utils.getEnvVarOrSysProp(STDIN_PIPE_NAME);
		final String stdOutName = Utils.getEnvVarOrSysProp(STDOUT_PIPE_NAME);
		LOGGER.info("Starting the 'test-lang' server with Unix socket files at {} and {}", stdInName, stdOutName);
		UnixSocketChannel readChannel = Utils.createChannel(stdInName);
		UnixSocketChannel writeChannel = Utils.createChannel(stdOutName);
		LOGGER.info("Create Launcher");
		Launcher<ExtendedLanguageClient> launcher = Launcher.createLauncher(this, ExtendedLanguageClient.class,
				Channels.newInputStream(readChannel), Channels.newOutputStream(writeChannel));
		connect(launcher.getRemoteProxy());
		LOGGER.info("Start listening for messages");
		processor= launcher.startListening();
		return CompletableFuture.completedFuture(new Object());
	}

	/**
	 * Default constructor.
	 */
	public TestLanguageServer() {
		this(new DocumentManager());
	}

	/**
	 * Constructor with a custom {@link DocumentManager}.
	 * 
	 * @param documentManager
	 *            the custom document manager to use.
	 */
	public TestLanguageServer(final DocumentManager documentManager) {
		this.documentManager = documentManager;
		this.textDocumentService= new TestTextDocumentService(this);
		this.workspaceService= new TestWorkspaceService();
	}

	/**
	 * @return the document manager associated with this server.
	 */
	public DocumentManager getDocumentManager() {
		return this.documentManager;
	}

	/**
	 * Sends the given <code>log message notification</code> back to the client
	 * as a notification
	 * 
	 * @param type
	 *            the type of message
	 * @param msg
	 *            The message to send back to the client
	 */
	public void sendLogMessageNotification(final MessageType type, final String msg) {
		languageClient.logMessage(new MessageParams(type, msg));
	}

	/**
	 * Sends the given <code>show message notification</code> back to the client
	 * as a notification
	 * 
	 * @param type
	 *            the type of message
	 * @param msg
	 *            The message to send back to the client
	 */
	public void sendShowMessageNotification(final MessageType type, final String msg) {
		languageClient.showMessage(new MessageParams(type, msg));
	}

	public CompletableFuture<MessageActionItem> sendShowMessageRequest(final MessageType type, final String msg, String command) {
		return languageClient.showMessageRequest(new ShowMessageRequestParams(Arrays.asList(new MessageActionItem(command))));
	}

	public void publishDiagnostics(String uri, List<Diagnostic> diagnostics) {
		languageClient.publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
	}

	
	public void sendStatus(final ServiceStatus serverStatus, final String status) {
		languageClient.statusEvent(new StatusReport(serverStatus, status));
	}

	public void connect(ExtendedLanguageClient client) {
		this.languageClient = client;
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		triggerInitialization(params.getRootUri());
		final InitializeResult result = new InitializeResult();
		final ServerCapabilities capabilities = new ServerCapabilities();
		capabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental);
		capabilities.setCompletionProvider(new CompletionOptions(false, Collections.emptyList()));
		capabilities.setHoverProvider(Boolean.TRUE);
		capabilities.setDocumentSymbolProvider(Boolean.FALSE);
		capabilities.setWorkspaceSymbolProvider(Boolean.FALSE);
		capabilities.setReferencesProvider(Boolean.FALSE);
		capabilities.setDocumentHighlightProvider(Boolean.FALSE);
		capabilities.setDocumentFormattingProvider(Boolean.FALSE);
		capabilities.setDocumentRangeFormattingProvider(Boolean.FALSE);
		capabilities.setCodeLensProvider(new CodeLensOptions(Boolean.FALSE));
		capabilities.setCodeActionProvider(Boolean.TRUE);
		
		capabilities.setExecuteCommandProvider(new ExecuteCommandOptions(Arrays.asList("TestCommand")));

		result.setCapabilities(capabilities);
		return CompletableFuture.completedFuture(result);

	}

	private void triggerInitialization(final String root) {
		LOGGER.info("Triggering initialization from {}", root);
		sendStatus(ServiceStatus.Starting, "Init...");
		sendStatus(ServiceStatus.Started, "Ready");
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		return CompletableFuture.supplyAsync(()-> {
			if (processor != null) {
				return processor.cancel(true);
			}
			return Boolean.TRUE;
		});
	}

	@Override
	public void exit() {
		LOGGER.info("Exiting Test Language Server");
		System.exit(0);
	}

	@Override
	public TextDocumentService getTextDocumentService() {
		return textDocumentService;
	}

	@Override
	public WorkspaceService getWorkspaceService() {
		return workspaceService;
	}

}
