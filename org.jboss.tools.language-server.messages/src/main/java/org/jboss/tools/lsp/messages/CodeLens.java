
package org.jboss.tools.lsp.messages;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class CodeLens {

    @SerializedName("range")
    @Expose
    private Range range;
    @SerializedName("command")
    @Expose
    private Command command;
    /**
     * An data entry field that is preserved on a code lens item between
     * 
     * a [CodeLensRequest](#CodeLensRequest) and a [CodeLensResolveRequest]
     * 
     * (#CodeLensResolveRequest)
     * 
     */
    @SerializedName("data")
    @Expose
    private Object data;

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

    public CodeLens withRange(Range range) {
        this.range = range;
        return this;
    }

    /**
     * 
     * @return
     *     The command
     */
    public Command getCommand() {
        return command;
    }

    /**
     * 
     * @param command
     *     The command
     */
    public void setCommand(Command command) {
        this.command = command;
    }

    public CodeLens withCommand(Command command) {
        this.command = command;
        return this;
    }

    /**
     * An data entry field that is preserved on a code lens item between
     * 
     * a [CodeLensRequest](#CodeLensRequest) and a [CodeLensResolveRequest]
     * 
     * (#CodeLensResolveRequest)
     * 
     * @return
     *     The data
     */
    public Object getData() {
        return data;
    }

    /**
     * An data entry field that is preserved on a code lens item between
     * 
     * a [CodeLensRequest](#CodeLensRequest) and a [CodeLensResolveRequest]
     * 
     * (#CodeLensResolveRequest)
     * 
     * @param data
     *     The data
     */
    public void setData(Object data) {
        this.data = data;
    }

    public CodeLens withData(Object data) {
        this.data = data;
        return this;
    }

}
