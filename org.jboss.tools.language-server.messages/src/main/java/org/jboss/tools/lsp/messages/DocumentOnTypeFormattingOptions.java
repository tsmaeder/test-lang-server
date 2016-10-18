
package org.jboss.tools.lsp.messages;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * Format document on type options
 * 
 */
@Generated("org.jsonschema2pojo")
public class DocumentOnTypeFormattingOptions {

    /**
     * A character on which formatting should be triggered, like `}`.
     * 
     */
    @SerializedName("firstTriggerCharacter")
    @Expose
    private String firstTriggerCharacter;
    /**
     * More trigger characters.
     * 
     */
    @SerializedName("moreTriggerCharacter")
    @Expose
    private List<String> moreTriggerCharacter = new ArrayList<String>();

    /**
     * A character on which formatting should be triggered, like `}`.
     * 
     * @return
     *     The firstTriggerCharacter
     */
    public String getFirstTriggerCharacter() {
        return firstTriggerCharacter;
    }

    /**
     * A character on which formatting should be triggered, like `}`.
     * 
     * @param firstTriggerCharacter
     *     The firstTriggerCharacter
     */
    public void setFirstTriggerCharacter(String firstTriggerCharacter) {
        this.firstTriggerCharacter = firstTriggerCharacter;
    }

    public DocumentOnTypeFormattingOptions withFirstTriggerCharacter(String firstTriggerCharacter) {
        this.firstTriggerCharacter = firstTriggerCharacter;
        return this;
    }

    /**
     * More trigger characters.
     * 
     * @return
     *     The moreTriggerCharacter
     */
    public List<String> getMoreTriggerCharacter() {
        return moreTriggerCharacter;
    }

    /**
     * More trigger characters.
     * 
     * @param moreTriggerCharacter
     *     The moreTriggerCharacter
     */
    public void setMoreTriggerCharacter(List<String> moreTriggerCharacter) {
        this.moreTriggerCharacter = moreTriggerCharacter;
    }

    public DocumentOnTypeFormattingOptions withMoreTriggerCharacter(List<String> moreTriggerCharacter) {
        this.moreTriggerCharacter = moreTriggerCharacter;
        return this;
    }

}
