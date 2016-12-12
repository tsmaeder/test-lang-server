
package org.jboss.tools.lsp.messages;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Range {

    /**
     * Position in a text document expressed as zero-based line and character offset.
     * 
     * The Position namespace provides helper functions to work with
     * 
     * [Position](#Position) literals.
     * 
     */
    @SerializedName("start")
    @Expose
    private Position start;
    /**
     * Position in a text document expressed as zero-based line and character offset.
     * 
     * The Position namespace provides helper functions to work with
     * 
     * [Position](#Position) literals.
     * 
     */
    @SerializedName("end")
    @Expose
    private Position end;

    /**
     * Position in a text document expressed as zero-based line and character offset.
     * 
     * The Position namespace provides helper functions to work with
     * 
     * [Position](#Position) literals.
     * 
     * @return
     *     The start
     */
    public Position getStart() {
        return start;
    }

    /**
     * Position in a text document expressed as zero-based line and character offset.
     * 
     * The Position namespace provides helper functions to work with
     * 
     * [Position](#Position) literals.
     * 
     * @param start
     *     The start
     */
    public void setStart(Position start) {
        this.start = start;
    }

    public Range withStart(Position start) {
        this.start = start;
        return this;
    }

    /**
     * Position in a text document expressed as zero-based line and character offset.
     * 
     * The Position namespace provides helper functions to work with
     * 
     * [Position](#Position) literals.
     * 
     * @return
     *     The end
     */
    public Position getEnd() {
        return end;
    }

    /**
     * Position in a text document expressed as zero-based line and character offset.
     * 
     * The Position namespace provides helper functions to work with
     * 
     * [Position](#Position) literals.
     * 
     * @param end
     *     The end
     */
    public void setEnd(Position end) {
        this.end = end;
    }

    public Range withEnd(Position end) {
        this.end = end;
        return this;
    }

	@Override
	public String toString() {
		return "Range [start=" + start + ", end=" + end + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
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
		Range other = (Range) obj;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		return true;
	}

	/**
	 * @return <code>true</code> if the given {@code position} is part of this {@link Range}, <code>false</code> otherwise.
	 * @param position the position to check
	 */
	public boolean includes(final Position position) {
		// assume 'start' and 'end' are on the same line
		return (this.start.getLine().equals(position.getLine()) && this.start.getCharacter() <= position.getCharacter()
				&& this.end.getCharacter() >= position.getCharacter());
	}

    
}
