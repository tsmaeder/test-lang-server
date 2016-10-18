
package org.jboss.tools.lsp.messages;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class DocumentHighlight {

    @SerializedName("range")
    @Expose
    private Range range;
    /**
     * The highlight kind, default is [text](#DocumentHighlightKind.Text).
     * 
     */
    @SerializedName("kind")
    @Expose
    private Double kind;

    /**
     * 
     * @return
     *     The range
     */
    public Range getRange() {
        return range;
    }

    /**
     * 
     * @param range
     *     The range
     */
    public void setRange(Range range) {
        this.range = range;
    }

    public DocumentHighlight withRange(Range range) {
        this.range = range;
        return this;
    }

    /**
     * The highlight kind, default is [text](#DocumentHighlightKind.Text).
     * 
     * @return
     *     The kind
     */
    public Double getKind() {
        return kind;
    }

    /**
     * The highlight kind, default is [text](#DocumentHighlightKind.Text).
     * 
     * @param kind
     *     The kind
     */
    public void setKind(Double kind) {
        this.kind = kind;
    }

    public DocumentHighlight withKind(Double kind) {
        this.kind = kind;
        return this;
    }

}
