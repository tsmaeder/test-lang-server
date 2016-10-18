
package org.jboss.tools.lsp.messages;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class TextDocumentContentChangeEvent {

    @SerializedName("range")
    @Expose
    private Range range;
    /**
     * The length of the range that got replaced.
     * 
     */
    @SerializedName("rangeLength")
    @Expose
    private Double rangeLength;
    /**
     * The new text of the document.
     * 
     */
    @SerializedName("text")
    @Expose
    private String text;

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

    public TextDocumentContentChangeEvent withRange(Range range) {
        this.range = range;
        return this;
    }

    /**
     * The length of the range that got replaced.
     * 
     * @return
     *     The rangeLength
     */
    public Double getRangeLength() {
        return rangeLength;
    }

    /**
     * The length of the range that got replaced.
     * 
     * @param rangeLength
     *     The rangeLength
     */
    public void setRangeLength(Double rangeLength) {
        this.rangeLength = rangeLength;
    }

    public TextDocumentContentChangeEvent withRangeLength(Double rangeLength) {
        this.rangeLength = rangeLength;
        return this;
    }

    /**
     * The new text of the document.
     * 
     * @return
     *     The text
     */
    public String getText() {
        return text;
    }

    /**
     * The new text of the document.
     * 
     * @param text
     *     The text
     */
    public void setText(String text) {
        this.text = text;
    }

    public TextDocumentContentChangeEvent withText(String text) {
        this.text = text;
        return this;
    }

}
