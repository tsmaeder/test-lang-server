package org.jboss.tools.lsp.testlang;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;

public interface LocationConsumer {
    boolean accept(TextDocumentIdentifier document, Range range, String text);
}
