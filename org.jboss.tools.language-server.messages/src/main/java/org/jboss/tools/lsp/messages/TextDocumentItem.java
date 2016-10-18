
package org.jboss.tools.lsp.messages;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class TextDocumentItem {

    /**
     * The text document's uri.
     * 
     */
    @SerializedName("uri")
    @Expose
    private String uri;
    /**
     * The text document's language identifier
     * 
     */
    @SerializedName("languageId")
    @Expose
    private String languageId;
    /**
     * The version number of this document (it will strictly increase after each
     * 
     * change, including undo/redo).
     * 
     */
    @SerializedName("version")
    @Expose
    private Double version;
    /**
     * The content of the opened text document.
     * 
     */
    @SerializedName("text")
    @Expose
    private String text;

    /**
     * The text document's uri.
     * 
     * @return
     *     The uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * The text document's uri.
     * 
     * @param uri
     *     The uri
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    public TextDocumentItem withUri(String uri) {
        this.uri = uri;
        return this;
    }

    /**
     * The text document's language identifier
     * 
     * @return
     *     The languageId
     */
    public String getLanguageId() {
        return languageId;
    }

    /**
     * The text document's language identifier
     * 
     * @param languageId
     *     The languageId
     */
    public void setLanguageId(String languageId) {
        this.languageId = languageId;
    }

    public TextDocumentItem withLanguageId(String languageId) {
        this.languageId = languageId;
        return this;
    }

    /**
     * The version number of this document (it will strictly increase after each
     * 
     * change, including undo/redo).
     * 
     * @return
     *     The version
     */
    public Double getVersion() {
        return version;
    }

    /**
     * The version number of this document (it will strictly increase after each
     * 
     * change, including undo/redo).
     * 
     * @param version
     *     The version
     */
    public void setVersion(Double version) {
        this.version = version;
    }

    public TextDocumentItem withVersion(Double version) {
        this.version = version;
        return this;
    }

    /**
     * The content of the opened text document.
     * 
     * @return
     *     The text
     */
    public String getText() {
        return text;
    }

    /**
     * The content of the opened text document.
     * 
     * @param text
     *     The text
     */
    public void setText(String text) {
        this.text = text;
    }

    public TextDocumentItem withText(String text) {
        this.text = text;
        return this;
    }

}
