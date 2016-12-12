
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
    private Integer line;
    /**
     * Character offset on a line in a document (zero-based).
     * 
     */
    @SerializedName("character")
    @Expose
    private Integer character;

    /**
     * Line position in a document (zero-based).
     * 
     * @return
     *     The line
     */
    public Integer getLine() {
        return line;
    }

    /**
     * Line position in a document (zero-based).
     * 
     * @param line
     *     The line
     */
    public void setLine(Integer line) {
        this.line = line;
    }

    public Position withLine(Integer line) {
        this.line = line;
        return this;
    }

    public Position withLine(int line) {
    	return withLine(new Integer(line));
    }
    
    /**
     * Character offset on a line in a document (zero-based).
     * 
     * @return
     *     The character
     */
    public Integer getCharacter() {
        return character;
    }

    /**
     * Character offset on a line in a document (zero-based).
     * 
     * @param character
     *     The character
     */
    public void setCharacter(Integer character) {
        this.character = character;
    }

    public Position withCharacter(Integer character) {
        this.character = character;
        return this;
    }

    public Position withCharacter(int character) {
    	return withCharacter(new Integer(character));
    }

	@Override
	public String toString() {
		return "Position [line=" + line + ", character=" + character + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((character == null) ? 0 : character.hashCode());
		result = prime * result + ((line == null) ? 0 : line.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (character == null) {
			if (other.character != null)
				return false;
		} else if (!character.equals(other.character))
			return false;
		if (line == null) {
			if (other.line != null)
				return false;
		} else if (!line.equals(other.line))
			return false;
		return true;
	}

    
}
