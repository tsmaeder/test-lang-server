package org.jboss.tools.lsp.ext;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageServer;

import java.util.concurrent.CompletableFuture;

public interface ExtendedLanguageServer extends LanguageServer {
    @JsonRequest
    CompletableFuture<String> getDocument(String uri);
}
