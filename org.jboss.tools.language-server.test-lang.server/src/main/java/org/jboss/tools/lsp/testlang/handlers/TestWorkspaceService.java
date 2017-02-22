package org.jboss.tools.lsp.testlang.handlers;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestWorkspaceService implements WorkspaceService {
	/** The usual Logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(TestWorkspaceService.class);


	public TestWorkspaceService() {
	}

	@Override
	public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
		LOGGER.info("Executing command {}", params.getCommand());
		return CompletableFuture.completedFuture(params.getCommand()+" was executed");
	}
	
	@Override
	public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		// TODO Auto-generated method stub

	}

}
