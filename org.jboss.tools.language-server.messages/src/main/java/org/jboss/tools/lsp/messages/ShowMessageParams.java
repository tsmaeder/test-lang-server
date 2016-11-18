
package org.jboss.tools.lsp.messages;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class ShowMessageParams {

    /**
     * The message type. See {
     * 
     */
    @SerializedName("type")
    @Expose
    private Integer type;
    /**
     * The actual message
     * 
     */
    @SerializedName("message")
    @Expose
    private String message;

    /**
     * The message type. See {
     * 
     * @return
     *     The type
     */
    public Integer getType() {
        return type;
    }

    /**
     * The message type. See {
     * 
     * @param type
     *     The type
     */
    public void setType(Integer type) {
        this.type = type;
    }

    public ShowMessageParams withType(int type) {
        this.type = type;
        return this;
    }

    /**
     * The actual message
     * 
     * @return
     *     The message
     */
    public String getMessage() {
        return message;
    }

    /**
     * The actual message
     * 
     * @param message
     *     The message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    public ShowMessageParams withMessage(String message) {
        this.message = message;
        return this;
    }

}
