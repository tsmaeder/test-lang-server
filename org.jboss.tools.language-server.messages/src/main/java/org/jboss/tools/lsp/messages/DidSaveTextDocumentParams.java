
package org.jboss.tools.lsp.messages;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class DidSaveTextDocumentParams {

    @SerializedName("textDocument")
    @Expose
    private TextDocumentIdentifier textDocument;

    /**
     * 
     * @return
     *     The textDocument
     */
    public TextDocumentIdentifier getTextDocument() {
        return textDocument;
    }

    /**
     * 
     * @param textDocument
     *     The textDocument
     */
    public void setTextDocument(TextDocumentIdentifier textDocument) {
        this.textDocument = textDocument;
    }

    public DidSaveTextDocumentParams withTextDocument(TextDocumentIdentifier textDocument) {
        this.textDocument = textDocument;
        return this;
    }

}
