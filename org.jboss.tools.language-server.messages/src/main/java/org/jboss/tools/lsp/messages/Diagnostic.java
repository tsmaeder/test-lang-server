
package org.jboss.tools.lsp.messages;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Diagnostic {

    @SerializedName("range")
    @Expose
    private Range range;
    /**
     * The diagnostic's severity. Can be omitted. If omitted it is up to the
     * 
     * client to interpret diagnostics as error, warning, info or hint.
     * 
     */
    @SerializedName("severity")
    @Expose
    private Double severity;
    /**
     * The diagnostic's code. Can be omitted.
     * 
     */
    @SerializedName("code")
    @Expose
    private Object code;
    /**
     * A human-readable string describing the source of this
     * 
     * diagnostic, e.g. 'typescript' or 'super lint'.
     * 
     */
    @SerializedName("source")
    @Expose
    private String source;
    /**
     * The diagnostic's message.
     * 
     */
    @SerializedName("message")
    @Expose
    private String message;

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

    public Diagnostic withRange(Range range) {
        this.range = range;
        return this;
    }

    /**
     * The diagnostic's severity. Can be omitted. If omitted it is up to the
     * 
     * client to interpret diagnostics as error, warning, info or hint.
     * 
     * @return
     *     The severity
     */
    public Double getSeverity() {
        return severity;
    }

    /**
     * The diagnostic's severity. Can be omitted. If omitted it is up to the
     * 
     * client to interpret diagnostics as error, warning, info or hint.
     * 
     * @param severity
     *     The severity
     */
    public void setSeverity(Double severity) {
        this.severity = severity;
    }

    public Diagnostic withSeverity(Double severity) {
        this.severity = severity;
        return this;
    }

    /**
     * The diagnostic's code. Can be omitted.
     * 
     * @return
     *     The code
     */
    public Object getCode() {
        return code;
    }

    /**
     * The diagnostic's code. Can be omitted.
     * 
     * @param code
     *     The code
     */
    public void setCode(Object code) {
        this.code = code;
    }

    public Diagnostic withCode(Object code) {
        this.code = code;
        return this;
    }

    /**
     * A human-readable string describing the source of this
     * 
     * diagnostic, e.g. 'typescript' or 'super lint'.
     * 
     * @return
     *     The source
     */
    public String getSource() {
        return source;
    }

    /**
     * A human-readable string describing the source of this
     * 
     * diagnostic, e.g. 'typescript' or 'super lint'.
     * 
     * @param source
     *     The source
     */
    public void setSource(String source) {
        this.source = source;
    }

    public Diagnostic withSource(String source) {
        this.source = source;
        return this;
    }

    /**
     * The diagnostic's message.
     * 
     * @return
     *     The message
     */
    public String getMessage() {
        return message;
    }

    /**
     * The diagnostic's message.
     * 
     * @param message
     *     The message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    public Diagnostic withMessage(String message) {
        this.message = message;
        return this;
    }

}
