
package org.jboss.tools.lsp.messages;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * Position in a text document expressed as zero-based line and character offset.
 * 
 * The Position namespace provides helper functions to work with
 * 
 * [Position](#Position) literals.
 * 
 */
@Generated("org.jsonschema2pojo")
public class Position {

    /**
     * Line position in a document (zero-based).
     * 
     */
    @SerializedName("line")
    @Expose
    private Double line;
    /**
     * Character offset on a line in a document (zero-based).
     * 
     */
    @SerializedName("character")
    @Expose
    private Double character;

    /**
     * Line position in a document (zero-based).
     * 
     * @return
     *     The line
     */
    public Double getLine() {
        return line;
    }

    /**
     * Line position in a document (zero-based).
     * 
     * @param line
     *     The line
     */
    public void setLine(Double line) {
        this.line = line;
    }

    public Position withLine(Double line) {
        this.line = line;
        return this;
    }

    public Position withLine(int line) {
    	return withLine(new Double(line));
    }
    
    /**
     * Character offset on a line in a document (zero-based).
     * 
     * @return
     *     The character
     */
    public Double getCharacter() {
        return character;
    }

    /**
     * Character offset on a line in a document (zero-based).
     * 
     * @param character
     *     The character
     */
    public void setCharacter(Double character) {
        this.character = character;
    }

    public Position withCharacter(Double character) {
        this.character = character;
        return this;
    }

    public Position withCharacter(int character) {
    	return withCharacter(new Double(character));
    }

}
