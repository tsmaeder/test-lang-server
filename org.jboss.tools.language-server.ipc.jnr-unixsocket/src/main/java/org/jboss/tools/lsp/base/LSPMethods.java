package org.jboss.tools.lsp.base;

import java.lang.reflect.Type;
import java.util.List;

import org.jboss.tools.lsp.ext.StatusReport;
import org.jboss.tools.lsp.messages.CodeLens;
import org.jboss.tools.lsp.messages.CodeLensParams;
import org.jboss.tools.lsp.messages.CompletionList;
import org.jboss.tools.lsp.messages.DidChangeTextDocumentParams;
import org.jboss.tools.lsp.messages.DidChangeWatchedFilesParams;
import org.jboss.tools.lsp.messages.DidCloseTextDocumentParams;
import org.jboss.tools.lsp.messages.DidOpenTextDocumentParams;
import org.jboss.tools.lsp.messages.DidSaveTextDocumentParams;
import org.jboss.tools.lsp.messages.DocumentFormattingParams;
import org.jboss.tools.lsp.messages.DocumentHighlight;
import org.jboss.tools.lsp.messages.DocumentRangeFormattingParams;
import org.jboss.tools.lsp.messages.DocumentSymbolParams;
import org.jboss.tools.lsp.messages.Hover;
import org.jboss.tools.lsp.messages.InitializeParams;
import org.jboss.tools.lsp.messages.InitializeResult;
import org.jboss.tools.lsp.messages.Location;
import org.jboss.tools.lsp.messages.LogMessageParams;
import org.jboss.tools.lsp.messages.PublishDiagnosticsParams;
import org.jboss.tools.lsp.messages.ReferenceParams;
import org.jboss.tools.lsp.messages.ShowMessageParams;
import org.jboss.tools.lsp.messages.TextDocumentIdentifier;
import org.jboss.tools.lsp.messages.TextDocumentPositionParams;
import org.jboss.tools.lsp.messages.WorkspaceSymbolParams;

public enum LSPMethods {

	INITIALIZE("initialize",InitializeParams.class,InitializeResult.class),
	EXIT("exit",Object.class, Object.class),
	SHUTDOWN("shutdown",Object.class, Object.class),
	DOCUMENT_COMPLETION("textDocument/completion",TextDocumentPositionParams.class, CompletionList.class),
	DOCUMENT_OPENED("textDocument/didOpen",DidOpenTextDocumentParams.class, Object.class),
	DOCUMENT_CLOSED("textDocument/didClose",DidCloseTextDocumentParams.class, Object.class),
	DOCUMENT_CHANGED("textDocument/didChange",DidChangeTextDocumentParams.class, Object.class),
	DOCUMENT_SAVED("textDocument/didSave",DidSaveTextDocumentParams.class, Object.class),
	DOCUMENT_HOVER("textDocument/hover", TextDocumentPositionParams.class, Hover.class),
	DOCUMENT_DEFINITION("textDocument/definition", TextDocumentPositionParams.class, Location.class),
	DOCUMENT_REFERENCES("textDocument/references", ReferenceParams.class, List.class),
	DOCUMENT_SYMBOL("textDocument/documentSymbol", DocumentSymbolParams.class, List.class),
	DOCUMENT_HIGHLIGHT("textDocument/documentHighlight", TextDocumentPositionParams.class, DocumentHighlight.class),
	DOCUMENT_FORMATTING("textDocument/formatting", DocumentFormattingParams.class, List.class),
	DOCUMENT_RANGE_FORMATTING("textDocument/rangeFormatting", DocumentRangeFormattingParams.class, List.class),
	DOCUMENT_CODELENS("textDocument/codeLens", CodeLensParams.class, List.class),
	DOCUMENT_DIAGNOSTICS("textDocument/publishDiagnostics", PublishDiagnosticsParams.class, Object.class),
	CODELENS_RESOLVE("codeLens/resolve",CodeLens.class, CodeLens.class),
	WORKSPACE_CHANGED_FILES("workspace/didChangeWatchedFiles", DidChangeWatchedFilesParams.class, Object.class),
	WORKSPACE_SYMBOL("workspace/symbol", WorkspaceSymbolParams.class, List.class),
	WINDOW_LOGMESSAGE("window/logMessage",LogMessageParams.class,Object.class),
	WINDOW_SHOWMESSAGE("window/showMessage",ShowMessageParams.class,Object.class),
	//extensions
	LANGUAGE_STATUS("language/status", StatusReport.class, Object.class),
	CLASSFILECONTENTS("java/ClassFileContents",TextDocumentIdentifier.class, String.class);
	
	private final String method;
	private final Type requestType;
	private final Type resultType;
	
	LSPMethods(String method, Type request, Type result ) {
		this.method = method;
		this.requestType = request;
		this.resultType = result;
	}

	/**
	 * @return the resultType
	 */
	public Type getResultType() {
		return resultType;
	}

	/**
	 * @return the requestType
	 */
	public Type getRequestType() {
		return requestType;
	}

	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}
	
	public static LSPMethods fromMethod(String method){
		LSPMethods[] values = LSPMethods.values();
		for (LSPMethods lspmethod : values) {
			if(lspmethod.getMethod().equals(method))
			return lspmethod;
		}
		return null;
	}
	
}
